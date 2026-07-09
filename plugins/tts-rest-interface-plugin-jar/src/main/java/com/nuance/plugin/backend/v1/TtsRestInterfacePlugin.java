package com.nuance.plugin.backend.v1;

import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.plugin.backend.v1.json.RequestJson;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.WebApiCommonException;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.nuance.cobra.utils.errors.RestPluginsError.UNKNOWN_ACTION;
import static com.nuance.cobra.utils.errors.RestPluginsError.UNSUPPORTED_URI;
import static com.nuance.plugin.backend.v1.TtsRestConfig.Property.TTS_REST_ENCAPSULATE_SIMPLE_TEXT;

public class TtsRestInterfacePlugin implements IWebApiRawPlugin {

    public static final String TTS_FUNCTION_NAME = "tts-rest-api";

    private static final String TTS_URL_MATCHER = "rest/text-to-speech/v1/";
    private static Logger logger = LogManager.getLogger(TtsRestInterfacePlugin.class);
    private IWebApiFeatures webApiFeatures;

    public static final String SENSITIVITY_KEY = "sensitivity";
    public static final String STATISTICS_KEY = "statistics";
    public static final String PARAMETERS_KEY = "parameters";
    public static final String VOICE_KEY = "voice";

    public enum Actions {
        voices("getResources"),
        synthesize("speechSynthesis");

        private final String commandName;
        Actions(String commandName) {
            this.commandName = commandName;
        }

        public String getCommandName() {
            return commandName;
        }
    }


    @Override
    public void onInitPlugin(IWebApiFeatures featuresList) {
        this.webApiFeatures = featuresList;
    }

    @Override
    public String getUrlPattern() {
        return TTS_URL_MATCHER + ".*";
    }

    /**
     * Called on an HTTP get.
     *
     * @param request  The Http request object.
     * @param response The object to be used to send the response.
     */
    @Override
    public boolean onHttpGet(HttpServletRequest request, HttpServletResponse response) throws IOException, WebApiCommonException {

        logger.debug("Received GET: {}", request);
        String action = extractAction(request.getRequestURI());
        request.setAttribute(MetricsConstants.FUNCTION_LABEL_ATTRIBUTE_KEY, TTS_FUNCTION_NAME);
        request.setAttribute(MetricsConstants.ACTION_LABEL_ATTRIBUTE_KEY, action);

        if (action.isEmpty()) {
            throw new WebApiCommonException(UNSUPPORTED_URI, "Missing action in URI " + request.getRequestURI());
        }

        switch (action.toLowerCase()) {
            case "voices":
                response.reset();
                response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                response.setStatus(HttpServletResponse.SC_OK);
                doWapiOverHttp(request, response, RequestJson.fromMap(new HashMap<>()));
                break;
            case "docs":
                response.reset();
                response.setContentType(ContentType.DEFAULT_TEXT.getMimeType());
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setStatus(HttpServletResponse.SC_OK);
                InputStream in = getClass().getResourceAsStream("/tts_api.yaml");
                IOUtils.copy(in, response.getOutputStream());
                response.flushBuffer();
                break;
            default:
                throw new WebApiCommonException(UNKNOWN_ACTION, action);
        }

        return true;
    }

    private String extractAction(String requestURI) {
        String action = "";
        String[] urlSplit = requestURI.split(TTS_URL_MATCHER);

        if (urlSplit.length != 2) {
            return action;
        }

        urlSplit = urlSplit[1].split("\\?");
        action = urlSplit[0];

        return action.replaceAll("/+$", "");
    }

    @Override
    public boolean onHttpPost(HttpServletRequest request, HttpServletResponse response) throws IOException, WebApiCommonException {

        logger.debug("Received POST: {}", request);
        String action = extractAction(request.getRequestURI());
        request.setAttribute(MetricsConstants.FUNCTION_LABEL_ATTRIBUTE_KEY, TTS_FUNCTION_NAME);
        request.setAttribute(MetricsConstants.ACTION_LABEL_ATTRIBUTE_KEY, action);

        if (action.isEmpty() || !action.equalsIgnoreCase("synthesize")) {
            throw new WebApiCommonException(UNKNOWN_ACTION, action);
        }

        String body = org.apache.commons.io.IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        RequestJson queryParam = RequestJson.fromJson(body);

        doWapiOverHttp(request, response, queryParam);

        return true;
    }

    private void doWapiOverHttp(HttpServletRequest request, HttpServletResponse response, RequestJson queryParam) throws IOException {
        IWebApiEndpoint webApi;
        TtsResponseSender responseSender = new TtsResponseSender(response);
        try (javax.websocket.Session session = new WebSocketHttpSession(request.getSession())){

            webApi = webApiFeatures.getWebApiEndpoint(responseSender);

            webApi.onMessage(session, buildWebAPIRequest(request, queryParam).toString());
        } catch (Exception e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
        }

        responseSender.waitForResponse(getSessionConfiguration(), queryParam);
        webApiFeatures.close();
    }

    private JSONObject buildWebAPIRequest(HttpServletRequest request, RequestJson queryParam) throws IOException {
        JSONObject webApiRequest = new JSONObject();
        JSONArray commands = new JSONArray();

        Actions action = Actions.valueOf(extractAction(request.getRequestURI()));

        JSONObject parameters = new JSONObject();
        JSONObject command = new JSONObject();
        command.put("name", "startSession");
        command.put("id", "ss");

        parameters.put("speechSynthesisCodec", queryParam.audioConfig.getOrDefault("audioEncoding", "pcm_16_16k"));
        parameters.put("applicationKey", webApiFeatures.getConfigs().getString("applicationKey"));

        parameters.put(STATISTICS_KEY, queryParam.generalConfig.get(STATISTICS_KEY));
        parameters.put(SENSITIVITY_KEY, queryParam.generalConfig.get(SENSITIVITY_KEY));
        parameters.put("clientUserId", queryParam.clientMetaDataConfig.get("clientUserId"));
        parameters.put("clientDeviceId", queryParam.clientMetaDataConfig.get("clientDeviceId"));
        parameters.put("clientOsName", queryParam.clientMetaDataConfig.get("clientOsName"));
        parameters.put("clientOsVersion", queryParam.clientMetaDataConfig.get("clientOsVersion"));
        parameters.put("clientDetails", queryParam.clientMetaDataConfig);

        final String authorization = request.getHeader("Authorization");

        JSONObject auth = new JSONObject();
        auth.put("type", "no_auth");
        auth.put("data", "");
        if (authorization != null && !authorization.isEmpty()) {
            auth = new JSONObject();
            auth.put("type", "http_auth");
            auth.put("data", authorization);
        }

        parameters.put("authentication", auth);

        command.put(PARAMETERS_KEY, parameters);
        commands.put(command);

        command = new JSONObject();
        parameters = new JSONObject();

        putCommandParameters(command, parameters, queryParam, action);

        commands.put(command);

        command = new JSONObject();
        parameters = new JSONObject();
        command.put("name", "endSession");
        command.put("id", "es");
        command.put(PARAMETERS_KEY, parameters);
        commands.put(command);

        webApiRequest.put("type", "commandChain");
        webApiRequest.put("commands", commands);

        return webApiRequest;
    }

    private CombinedConfiguration getSessionConfiguration() throws IOException {
        return webApiFeatures.getConfigs();
    }

    protected void putCommandParameters(JSONObject command, JSONObject parameters, RequestJson queryParam, Actions action){
        command.put("name", action.getCommandName());
        if(action == Actions.synthesize) {
            command.put("id", "tts");
            boolean shouldEncapsulateSimpleText = (Boolean)TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getDefaultValue();
            try {
                CombinedConfiguration config = webApiFeatures.getConfigs();
                shouldEncapsulateSimpleText = config.getBoolean(TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getKey(), (Boolean)TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getDefaultValue());
            }
            catch (Exception e) {
                logger.error("Couldn't find tenant config");
            }
            Object type = queryParam.input.getOrDefault("type", "text");
            type = type.equals("text") && shouldEncapsulateSimpleText ? "ssml" : type;
            parameters.put("type",  type); // Simple text is encapsulated in ssml
            parameters.put("data", getTTSText(shouldEncapsulateSimpleText, queryParam));
            parameters.put(VOICE_KEY, queryParam.voice.get(VOICE_KEY));
            parameters.put(STATISTICS_KEY, queryParam.generalConfig.get(STATISTICS_KEY));
            parameters.put(SENSITIVITY_KEY, queryParam.generalConfig.get(SENSITIVITY_KEY));
            command.put(PARAMETERS_KEY, parameters);
        }
        else if (action == Actions.voices) {
            command.put("id", action.getCommandName());
        }
    }

    protected Object getTTSText(boolean shouldEncapsulateSimpleText, RequestJson queryParam) {
        Object text = queryParam.input.get("text");
        try {
            CombinedConfiguration config = webApiFeatures.getConfigs();
            if(shouldEncapsulateSimpleText && text instanceof String) {
                String textString = (String)text;
                if(!textString.isBlank() && !textString.contains("</speak>")) {
                    logger.debug("Encapsulating text in ssml");
                    String voice = (String)queryParam.voice.get(VOICE_KEY);
                    String language = "";
                    Configuration voiceMapping = config.configurationAt("vocsMapping").configurationAt(voice);
                    language = voiceMapping.getString("language");

                    if(voiceMapping.containsKey("nvcVersion") && voiceMapping.getString("nvcVersion").equals("nvc3")) {
                        // NTTS
                        text = String.format("<lang xml:lang=\"%s\">%s</lang>",
                                language,
                                text);
                    }
                    else {
                        // TTSaaS
                        text = String.format("<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"https://www.w3.org/2001/mstts\" xml:lang=\"%s\">%s</speak>",
                                language,
                                text);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Couldn't find tenant config");
        }

        return text;
    }
}
