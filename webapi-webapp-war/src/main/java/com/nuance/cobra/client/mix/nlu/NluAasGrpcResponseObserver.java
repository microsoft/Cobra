package com.nuance.cobra.client.mix.nlu;

import com.google.gson.GsonBuilder;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.ResponseObserver;
import com.nuance.cobra.client.response.InterpretationResponse;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.cobra.utils.krypton.KryptonConverter;
import com.nuance.rpc.nlu.interpret.v1.Metrics;
import com.nuance.rpc.nlu.v1.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nuance.cobra.utils.krypton.KryptonConverter.grpcMessageToJsonString;

class NluAasGrpcResponseObserver extends ResponseObserver<InterpretResponse> {
    private Status responseStatus;
    private InterpretResult responseResult;
    private final NluAasGrpcClient eventOwner;
    private Map<String, Any> metadata;

    public NluAasGrpcResponseObserver(NluAasGrpcClient eventOwner) {
        this.eventOwner = eventOwner;
    }

    @Override
    public void onNext(InterpretResponse response) {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(getSessionId(), true));

        if (eventOwner.isInterpretationDone) {
            return;
        }

        responseStatus = response.getStatus();
        responseResult = response.getResult();
        metadata = response.getMetadataMap();

        var logLine = String.format("Received Mix NLU as a service gRPC response: StatusCode=%s, StatusMessage=%s, StatusMessageDetails=%s, Result=%n%s",
                responseStatus.getCode(), responseStatus.getMessage(), responseStatus.getDetails(), responseResult);

        if (responseStatus.getCode() == 200) {
            logger.debug(logLine);
        } else {
            logger.error(logLine);
        }
    }

    @Override
    public void errorHandler(Throwable e) {

        logger.debug("onError()", e);

        if (e.getMessage().contains("Resolver returned status code 404")) {
            completeTransaction(e, ExternalResponseErrorCode.NLUAAS_GRPC_RESOURCE_ACCESS_ERROR);
        } else {
            completeTransaction(e, ExternalResponseErrorCode.INTERPRETATION_COMMAND_GENERIC_ERROR);
        }
    }
    @Override
    protected void completeTransaction(Throwable e, ExternalResponseErrorCode responseCode){
        MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
        MixUtils.invalidateTokenIfUnauthorized(logger, e, eventOwner.params.getCobraConfig());

        eventOwner.sendErrorResponse(responseCode);
    }


    @Override
    public void onCompleted() {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(getSessionId(), true));

        if (eventOwner.isInterpretationDone) {
            return;
        }

        logger.debug("onCompleted()");

        int statusCode = responseStatus.getCode();

        if (statusCode == 200) { // Success
            List<InterpretationResponse.Interpretation> interpretations = convertMixInterpretationsToCobraFormat();
            InterpretationResponse.Interpretation[] interpretationArray = interpretations.toArray(new InterpretationResponse.Interpretation[0]);

            var response = new InterpretationResponse(ResponseResult.ResponseStatus.SUCCESS, interpretationArray, true);
            if (eventOwner.params.getCobraConfig().getBoolean(WebApiConfig.Property.INTERPRETATION_VERBOSE_RESULT.key)) {
                var verboseResults = KryptonConverter.getVerboseResultFromGRPC(responseResult);
                addMetadata(verboseResults);
                response.setVerboseResult(verboseResults);
            }
            eventOwner.sendResponse(response);

        } else if (statusCode == 404 || // Error: A resource, such as a model or word list, does not exist or could not be accessed.
                (statusCode == 400 && responseStatus.getDetails().contains("status code 404"))) { // Changes in NLUaaS errors
            eventOwner.sendErrorResponse(ExternalResponseErrorCode.NLUAAS_GRPC_RESOURCE_ACCESS_ERROR);

        } else if (statusCode == 400) { // Error: your client app sent a malformed or unsupported request.
            eventOwner.sendErrorResponse(ExternalResponseErrorCode.NLUAAS_GRPC_MALFORMED_OR_UNSUPPORTED_REQUEST_ERROR);

        } else if (statusCode == 401 || statusCode == 403) { // Error: your client has not authenticated properly or permission was denied.
            eventOwner.sendErrorResponse(ExternalResponseErrorCode.NLUAAS_GRPC_AUTH_OR_PERMISSION_ERROR);

        } else if (statusCode == 415) { // Error: Unsupported resource type.
            eventOwner.sendErrorResponse(ExternalResponseErrorCode.NLUAAS_GRPC_RESOURCE_TYPE_ERROR);

        } else if (statusCode >= 500 && statusCode <= 511) { // Error: Internal error.
            eventOwner.sendErrorResponse(ExternalResponseErrorCode.NLUAAS_GRPC_INTERNAL_ERROR);

        } else {
            eventOwner.sendErrorResponse(ExternalResponseErrorCode.INTERPRETATION_COMMAND_GENERIC_ERROR);
        }
    }

    private void addMetadata(Map<String, Object> verboseResults) {
        if(metadata != null && !metadata.isEmpty()) {
            var metadataJson = new HashMap<>();
            var gson = new GsonBuilder().create();
            for (Map.Entry<String, Any> pair : metadata.entrySet()) {
                try {
                    metadataJson.put(pair.getKey(), gson.fromJson(grpcMessageToJsonString(pair.getValue().unpack(Metrics.class)), Map.class));
                } catch (InvalidProtocolBufferException e) {
                    logger.error("Failed to unpack metrics GRPC message", e);
                }
            }
            verboseResults.put("metadata", metadataJson);
        }
    }

    private List<InterpretationResponse.Interpretation> convertMixInterpretationsToCobraFormat() {
        List<InterpretationResponse.Interpretation> cobraInterpretations = new ArrayList<>();

        // get single-intent interpretations and sort them
        List<SingleIntentInterpretation> siInterpretations = new ArrayList<>();
        for (Interpretation interpretation : responseResult.getInterpretationsList()) {
            if (interpretation.getInterpretationUnionCase().equals(Interpretation.InterpretationUnionCase.SINGLE_INTENT_INTERPRETATION)) {
                siInterpretations.add(interpretation.getSingleIntentInterpretation());
            }
        }

        var useFullyFormattedLiteralForm = false;
        boolean hasAsrResults = false;

        // Verify if the interpretation being done, is using asr results
        try {
            hasAsrResults = eventOwner.params.getInput().hasAsrResult();
        }
        catch(ExceptionWithExternalResponseErrorCode e){
            logger.error("Failed to retrieve INTERPRETATION_USE_FULLY_FORMATTED_LITERAL value {}", e.getMessage());
        }

        /*
         *  Fetch a default value for useFullyFormattedLiteralForm in the config
         *  If no default is present fall back to the input type, i.e hasAsrResults
         */
        try {
            useFullyFormattedLiteralForm = eventOwner.params.getCobraConfig().getBoolean(WebApiConfig.Property.INTERPRETATION_USE_FULLY_FORMATTED_LITERAL.key);
        }
        catch(Exception e){
            useFullyFormattedLiteralForm = hasAsrResults;
        }

        // convert to cobra format
        for (SingleIntentInterpretation siInterpretation : siInterpretations) {
            List<InterpretationResponse.Concept> cobraConcepts = new ArrayList<>();

            Map<String, SingleIntentEntityList> entities = siInterpretation.getEntitiesMap();
            for (String entityName : siInterpretation.getEntitiesMap().keySet()) {
                SingleIntentEntityList entitiesForName = entities.get(entityName);
                cobraConcepts.addAll(eventOwner.parseEntities(useFullyFormattedLiteralForm, entityName, entitiesForName));
            }
            InterpretationResponse.Concept[] conceptArray = cobraConcepts.toArray(new InterpretationResponse.Concept[0]);

            int confidence = (int) (siInterpretation.getConfidence() * 100);
            String literal = useFullyFormattedLiteralForm ? responseResult.getFormattedLiteral() : responseResult.getLiteral();
            String intent = siInterpretation.getIntent();
            String origin = siInterpretation.getOrigin().name();

            cobraInterpretations.add(new InterpretationResponse.Interpretation(conceptArray, confidence, literal, intent, origin));
        }

        return cobraInterpretations;
    }

    @Override
    public boolean isDone() {
        return eventOwner.isInterpretationDone;
    }

    @Override
    protected String getSessionId() {
        return eventOwner.getSessionId();
    }
}
