package com.nuance.cobra.client.mix.nlu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.client.NluClientUtils;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.DynamicVocabularyDb;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.rpc.asr.v1.Result;
import com.nuance.rpc.nlu.v1.*;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NluAasGrpcParams
{

    private static final Logger logger = LogManager.getLogger();

    private String sessionId;
    private CombinedConfiguration cobraConfig;
    private List<DynamicVocabulary> dynamicVocabularies;
    private int maxInterpretations;

    public enum InputLoggingMode {
        PLAIN_TEXT("PLAINTEXT"),
        SUPPRESSED("SUPPRESSED");

        private final String value;

        InputLoggingMode(String name)
        {
            value = name;
        }

        @Override
        public String toString()
        {
            return value;
        }

    }

    public NluAasGrpcParams(String sessionId,
                               CombinedConfiguration cobraConfig) {

        this(sessionId, cobraConfig, null);
    }

    public NluAasGrpcParams(String sessionId,
                               CombinedConfiguration cobraConfig,
                               List<DynamicVocabulary> dynamicVocabularies) {

        this.sessionId = sessionId;
        this.cobraConfig = cobraConfig;
        this.dynamicVocabularies = dynamicVocabularies;
        this.maxInterpretations = -1;
    }

    public EnumInterpretationInputLoggingMode getLoggingSensitivity() {
        var configInputLoggingMode = cobraConfig.getString(WebApiConfig.Property.NLUAAS_GRPC_INPUT_LOGGING_MODE.key);

        logger.trace("inputLoggingMode set: {}", configInputLoggingMode);

        var inputLoggingMode = EnumInterpretationInputLoggingMode.PLAINTEXT; // Default for both webapi and NLUaaS

        String sensitivity = cobraConfig.getString(WebApiConfig.Property.SENSITIVITY.key);
        if(configInputLoggingMode.equalsIgnoreCase(InputLoggingMode.SUPPRESSED.toString()) ||
                (sensitivity != null && !sensitivity.equals("open"))) {
            inputLoggingMode = EnumInterpretationInputLoggingMode.SUPPRESSED;
        }

        return inputLoggingMode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public CombinedConfiguration getCobraConfig() {
        return cobraConfig;
    }

    public String getUrl()
    {
        return cobraConfig.getString(WebApiConfig.Property.NLUAAS_GRPC_SERVER_URL.key);
    }

    public boolean hasSemanticModelUri() {
        String contextTag = cobraConfig.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key, (String) WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.defaultValue);
        String language = NluClientUtils.getInterpretationLanguage(cobraConfig);
        String semanticModelUrl = cobraConfig.getString(WebApiConfig.Property.NLE_SEMANTIC_MODEL_URL.key);
        boolean hasSemanticModelUrl = semanticModelUrl != null && !semanticModelUrl.isBlank();

        return hasSemanticModelUrl || !contextTag.equals(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.defaultValue) && language != null;
    }

    public String getSemanticModelUri() {
        String semanticModelUrl = cobraConfig.getString(WebApiConfig.Property.NLE_SEMANTIC_MODEL_URL.key);

        if (semanticModelUrl != null && !semanticModelUrl.isBlank())
        {
            return semanticModelUrl;
        }
        String contextTag = cobraConfig.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key);
        String language = NluClientUtils.getInterpretationLanguage(cobraConfig);
        return String.format("urn:nuance-mix:tag:model/%s/mix.nlu?=language=%s", contextTag, language);
    }

    public InterpretationInput getInput() throws ExceptionWithExternalResponseErrorCode {
        String dataType = cobraConfig.getString(WebApiConfig.Property.COMMAND_DATA_TYPE.key);
        if(cobraConfig.getString(WebApiConfig.Property.COMMAND_DATA_TYPE.key) == null ||
                dataType.matches("text(/plain)?")) //the types should be declared into an enum. see codecs.
        {
            String input = cobraConfig.getString(WebApiConfig.Property.COMMAND_DATA.key);
            return (input == null) ? null : InterpretationInput.newBuilder().setText(input).build();
        }

        if (dataType.equalsIgnoreCase("speechRecognitionVerboseResult")) {
            try {
                Object input = cobraConfig.getProperty(WebApiConfig.Property.COMMAND_DATA.key);

                InterpretationInput.Builder interpretationBuilder = InterpretationInput.newBuilder();
                Result.Builder recoResponseBuilder = Result.newBuilder();
                Gson gson = new GsonBuilder().create();
                JsonObject jsonObject = gson.toJsonTree(input).getAsJsonObject();
                jsonObject.remove("notifications"); // Verbose results from webapi includes notifications
                String jsonString = jsonObject.toString();
                JsonFormat.parser().ignoringUnknownFields().merge(jsonString, recoResponseBuilder);
                interpretationBuilder.setAsrResult(Any.pack(recoResponseBuilder.build()));
                return interpretationBuilder.build();
            } catch (InvalidProtocolBufferException e) {
                logger.error("Couldn't parse x-nuance-krypton-result: {}", e.getMessage());
                return null;
            }
        }

        throw new ExceptionWithExternalResponseErrorCode("Mix NLU as a service gRPC request: Input type not supported: \"" + cobraConfig.getString(WebApiConfig.Property.COMMAND_DATA_TYPE.key) + "\"."
                , ExternalResponseErrorCode.NLUAAS_GRPC_INPUT_ERROR);
    }

    public void loadWordsets(InterpretRequest.Builder requestBuilder)
    {
        if(dynamicVocabularies == null) { return; }

        for (DynamicVocabulary dynVoc : dynamicVocabularies)
        {
            if (dynVoc.getType() == DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET)
            {
                processWordset(dynVoc, requestBuilder);
            }

            if(dynVoc.getType() == DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_WORDSET_PKG)
            {
                processWordsetPkg(dynVoc, requestBuilder);
            }
        }
    }

    void processWordset(DynamicVocabulary dynVoc, InterpretRequest.Builder requestBuilder)
    {
        String uri = dynVoc.getUri();
        if(uri != null && !uri.isBlank())
        {
            addUriResource(requestBuilder, uri, EnumResourceType.WORDSET);
        } else
        {
            String inlineWordset;
            try
            {
                inlineWordset = dynVoc.getJsonBody();
                requestBuilder.addResources(InterpretationResource.newBuilder().setInlineWordset(inlineWordset));
            }
            catch (CobraException e)
            {
                logger.warn("Unable to convert dynamic vocabulary (inline wordset) to JSON.", e);
            }
        }

    }

    void processWordsetPkg(DynamicVocabulary dynVoc, InterpretRequest.Builder requestBuilder)
    {
        String uri = dynVoc.getUri();
        if(uri != null && !uri.isBlank())
        {
            addUriResource(requestBuilder, uri, EnumResourceType.COMPILED_WORDSET);
        }
    }

    public void loadPersistedWordsets(InterpretRequest.Builder requestBuilder)
    {
        if(!cobraConfig.getBoolean(WebApiConfig.Property.LOAD_PERSISTED_WORDSETS.key)) { return; }

        Set<String> conceptNames = DynamicVocabularyDb.getPersistedWordsets(cobraConfig);
        if(conceptNames.isEmpty()) { return; }

        for (String conceptName : conceptNames)
        {
            DynamicVocabulary dynamicVocab = DynamicVocabularyDb.getWordset(conceptName, cobraConfig);
            if(dynamicVocab == null)
            {
                logger.warn("Couldn't get {} wordset, despite being listed in set", conceptName);
                continue;
            }
            try
            {
                String inlineWordset = dynamicVocab.getJsonBody();
                requestBuilder.addResources(InterpretationResource.newBuilder().setInlineWordset(inlineWordset));
            } catch (CobraException e)
            {
                logger.warn("Couldn't activate {} wordset", conceptName);
            }
        }
    }

    void addUriResource(InterpretRequest.Builder requestBuilder, String uri, EnumResourceType type)
    {
        requestBuilder.addResources(InterpretationResource.newBuilder()
                                                          .setExternalReference(
                                                                  ResourceReference.newBuilder()
                                                                                   .setUri(uri)
                                                                                   .setRequestTimeoutMs(cobraConfig.getInt(WebApiConfig.Property.NLUAAS_GRPC_RESOURCE_REQUEST_TIMEOUT_MS.key))
                                                                                   .setType(type)));
    }

    public Map<String, String> getClientData()
    {
        Map<String, String> nluClientData = new HashMap<>();
            
        if(getCobraConfig().getBoolean(WebApiConfig.Property.INTERPRETATION_VERBOSE_RESULT.key)){
            nluClientData.put("x-nuance-supplemental-metrics", "type.googleapis.com/nuance.nlu.interpret.v1.Metrics");
        }
        nluClientData.put("language", NluClientUtils.getInterpretationLanguage(cobraConfig));

        return MixUtils.getClientDataMap(sessionId, cobraConfig, nluClientData);
    }

    public boolean shouldSendUserId() {
        return MixUtils.shouldAddUserId(cobraConfig, WebApiConfig.Property.NLUAAS_GRPC_USE_USERID, logger);
    }

    public String getUserId() {
        return cobraConfig.getString(WebApiConfig.Property.CLIENT_USER_ID.key);
    }

    public void assertValidity() throws ExceptionWithExternalResponseErrorCode {
        if (cobraConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key) == null) { // need an app key for '.putAllClientData(params.getClientData())' to work
            throw new ExceptionWithExternalResponseErrorCode("Mix NLU as a service gRPC request: no application key specified.", ExternalResponseErrorCode.INTERPRETATION_COMMAND_EXECUTE_ERROR);
        }
        if (!hasSemanticModelUri()) {
            throw new ExceptionWithExternalResponseErrorCode("Mix NLU as a service gRPC request: no semantic model specified.", ExternalResponseErrorCode.NLUAAS_GRPC_SEMANTIC_MODEL_ERROR);
        }
        if (getInput() == null) {
            throw new ExceptionWithExternalResponseErrorCode("Mix NLU as a service gRPC request: no input specified.", ExternalResponseErrorCode.NLUAAS_GRPC_INPUT_ERROR);
        }
    }

    public int getMaxInterpretations() {
        return maxInterpretations;
    }

    public void setMaxInterpretations(InterpretationParameters.Builder interpretationParametersBuilder)
    {
        var configMaxInterpretations = cobraConfig.getInt(WebApiConfig.Property.NLUAAS_GRPC_MAX_INTERPRETATIONS.key);
        var defaultMaxInterpretations = (int) WebApiConfig.Property.NLUAAS_GRPC_MAX_INTERPRETATIONS.defaultValue;

        if(configMaxInterpretations <= defaultMaxInterpretations)
        {
            return;
        }

        maxInterpretations = configMaxInterpretations;
        interpretationParametersBuilder.setMaxInterpretations(maxInterpretations);
        logger.trace("maxInterpretations set: {}", maxInterpretations);
    }

}
