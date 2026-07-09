package com.nuance.cobra.client.mix.nlu;

import com.google.protobuf.util.JsonFormat;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.InterpretationClient;
import com.nuance.cobra.client.InterpretationClients;
import com.nuance.cobra.client.NluClientUtils;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.mix.grpc.ManagedChannelManager;
import com.nuance.cobra.client.response.InterpretationResponse;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.nlu.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mix NLU as a service gRPC client
 */
public class NluAasGrpcClient
        extends InterpretationClient {

    private static final Logger logger = LogManager.getLogger();

    private static final String ENGINE_TYPE = InterpretationClients.NLU_GRPC.getValue();

    protected final NluAasGrpcParams params;
    private final RuntimeGrpc.RuntimeStub grpcStub;
    private final StreamObserver<InterpretResponse> responseObserver;

    private final ResponseHandler responseHandler;
    private final ResponseErrorHandler errorHandler;

    protected boolean isInterpretationDone;

    public NluAasGrpcClient(NluAasGrpcParams params,
                            RuntimeGrpc.RuntimeStub grpcStub, // injected, for unit testing
                            ResponseHandler responseHandler,
                            ResponseErrorHandler errorHandler, HeaderEventListener interceptor) {

        super(params.getSessionId());

        this.params = params;
        this.grpcStub = grpcStub;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        interceptor.setListener(header -> {
            var session = SessionManager.getInstance().getSession(getSessionId(), false);
            if (session == null) return;
            if (session.getCommand() == null) return;

            session.getCommand().getStatistics().addEngineResponse("grpcHeaders", header);

            MixUtils.checkIfRateLimited(header, logger);
        });
        responseObserver = new NluAasGrpcResponseObserver(this);
    }

    @Override
    public void startInterpretation() {
        try {
            InterpretRequest request = buildRequest();
            logger.debug("Sending Mix NLU as a service gRPC request:\n{}", request);
            grpcStub.interpret(request, responseObserver);

        } catch (Exception e) {
            MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
            MixUtils.invalidateTokenIfUnauthorized(logger, e, params.getCobraConfig());

            if (e instanceof ExceptionWithExternalResponseErrorCode) {
                sendErrorResponse(((ExceptionWithExternalResponseErrorCode) e).getErrorCode(), e.getMessage());
            } else {
                sendErrorResponse(ExternalResponseErrorCode.INTERPRETATION_COMMAND_EXECUTE_ERROR);
            }
        }
    }

    // https://mix.nuance.com/v3/documentation/nlu-grpc/v1/#interpretrequest
    private InterpretRequest buildRequest() throws ExceptionWithExternalResponseErrorCode {
        params.assertValidity();

        var interpretationParametersBuilder = InterpretationParameters.newBuilder();

        // semantic model
        String semanticModelUri = params.getSemanticModelUri();
        setStatistics(ENGINE_TYPE, NluClientUtils.getInterpretationLanguage(params.getCobraConfig()), semanticModelUri);

        var builder = InterpretRequest.newBuilder()
                .setModel(ResourceReference.newBuilder()
                                .setType(EnumResourceType.SEMANTIC_MODEL)
                                .setUri(semanticModelUri)
//                        .setRequestTimeoutMs(5000)
                )
                .setInput(params.getInput())
                .setParameters(interpretationParametersBuilder
                        .setInterpretationInputLoggingMode(params.getLoggingSensitivity()));

        Map<String, String> clientData = params.getClientData();
        builder.putAllClientData(clientData);

        params.setMaxInterpretations(interpretationParametersBuilder);
        builder.setParameters(interpretationParametersBuilder);

        // user id
        if (params.shouldSendUserId()) {
            builder.setUserId(params.getUserId());
        }

        params.loadWordsets(builder);
        params.loadPersistedWordsets(builder);

        return builder.build();
    }

    protected List<InterpretationResponse.Concept> parseEntities(boolean useFullyFormattedLiteralForm, String entityName, SingleIntentEntityList entitiesForName) {
        List<InterpretationResponse.Concept> cobraConcepts = new ArrayList<>();
        for (SingleIntentEntity entity : entitiesForName.getEntitiesList()) {

            Object value = entity.getStringValue();
            boolean useFullyFormattedFields = useFullyFormattedLiteralForm && entity.hasFormattedTextRange();
            String literal = useFullyFormattedFields ? entity.getFormattedLiteral() : entity.getLiteral();
            int beginIndex = (useFullyFormattedFields ? entity.getFormattedTextRange() : entity.getTextRange()).getStartIndex();
            int endIndex = (useFullyFormattedFields ? entity.getFormattedTextRange() : entity.getTextRange()).getEndIndex();
            if(entity.hasStructValue()) {
                try {
                    value = new JSONObject(JsonFormat.printer().print(entity.getStructValueOrBuilder())).toMap();
                }
                catch (Exception e) {
                    logger.error("Error parsing structValue: {}", e.getMessage());
                }
            }

            var concept = new InterpretationResponse.Concept(entityName, value, literal, beginIndex, endIndex);
            cobraConcepts.add(concept);

            Map<String, SingleIntentEntityList> subEntities = entity.getEntitiesMap();
            List<InterpretationResponse.Concept> concepts = new ArrayList<>();
            for (String subEntityName : entity.getEntitiesMap().keySet()) {
                SingleIntentEntityList subEntitiesForName = subEntities.get(subEntityName);
                concepts.addAll(parseEntities(useFullyFormattedLiteralForm, subEntityName, subEntitiesForName));
            }
            concept.setConcepts(concepts);
        }
        return cobraConcepts;
    }

    @Override
    public void stopInterpretation() {
        var response = new InterpretationResponse(ResponseResult.ResponseStatus.SUCCESS, null, true);
        sendResponse(response);
    }

    @Override
    public void cancelInterpretation(boolean isFinal) {
        var response = new InterpretationResponse(ResponseResult.ResponseStatus.CANCELLED, null, true);
        sendResponse(response);
    }

    protected synchronized void sendResponse(InterpretationResponse response) {
        if (!isInterpretationDone) {
            isInterpretationDone = true;
            ManagedChannelManager.managedChannelShutdown(params.getCobraConfig(), (ManagedChannel)grpcStub.getChannel());

            String responseJson = null;
            try {
                responseJson = response.toPrettyJson();
            } catch (CobraException e) {
                logger.error("Error while converting interpretation response to JSON", e);
            }
            logger.debug("Sending back Cobra interpretation response to client:\n{}", responseJson);

            responseHandler.onResponse(response);
        }
    }

    protected synchronized void sendErrorResponse(ExternalResponseErrorCode errorCode, String ... args) {
        if (!isInterpretationDone) {
            isInterpretationDone = true;
            ManagedChannelManager.managedChannelShutdown(params.getCobraConfig(), (ManagedChannel)grpcStub.getChannel());

            var errorResponse = new InterpretationResponse(true, ResponseCode.createResponseError(errorCode, args));

            String errorResponseJson = null;
            try {
                errorResponseJson = errorResponse.toPrettyJson();
            } catch (CobraException e) {
                logger.error("Error while converting interpretation error response to JSON", e);
            }
            logger.debug("Sending back Cobra interpretation error response to client:\n{}", errorResponseJson);

            errorHandler.handleError(errorResponse);
        }
    }

}
