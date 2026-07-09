package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.hash.CobraHash;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.commandbuilder.WebapiStartSessionCommandBuilder;
import com.nuance.plugin.kryptonws.KryptonWsConfig;
import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_AUTH_TYPE;
import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_ENABLE_METRICS;

public class KryptonWsCreateSessionMethod
        extends KryptonWsMethod
{
    private static final Logger logger = LogManager.getLogger(KryptonWsCreateSessionMethod.class);
    KryptonWsLoadMethod kryptonWsLoadMethod = null;
    JSONArray sessionObjectJsonArray;
    public static final String NAME = "CreateSession";

    public KryptonWsCreateSessionMethod(JSONObject jsonObject, KryptonWsSession kryptonWsSession)
    {
        super(jsonObject, kryptonWsSession);
        logger.debug("KryptonWsCreateSessionMethod constructor");
    }

    @Override
    public void process()
    {
        logger.trace("process");

        createKryptonWsSession();

        super.process();

        String applicationKey = getApplicationKey();
        String secret = getSecret();
        String applicationVersion = getApplicationVersion();

        if(applicationKey == null || secret == null) {
            terminateSessionWithError("Couldn't authenticate request.");
            return;
        }
        else if(applicationVersion == null) {
            terminateSessionWithError("Application version is missing.");
            return;
        }

        var sessionParametersJson = methodJson.optJSONObject("sessionParameters");
        var recognitionParametersJson = methodJson.optJSONObject("recognitionParameters");

        KryptonWsParameters kryptonWsParameters;
        try
        {
            kryptonWsParameters = new KryptonWsParameters(sessionParametersJson, recognitionParametersJson);
            kryptonWsSession.setKryptonWsParameters(kryptonWsParameters);
        } catch (ConversionException | IllegalArgumentException e) {
            logger.error("There was an issue parsing parameters. {}", e.getMessage());

            kryptonWsResponseBuilder.addError(e.getMessage(), null);

            sendNoMetricsFailedResponse();

            resetKryptonSession(); // only reset if createSession

            return;
        }

        var languagePack = methodJson.optJSONObject("languagePack");
        String errorMessage = validateLanguagePack(languagePack);
        if (errorMessage != null)
        {
            sendErrorResponse(errorMessage, null);
            cleanup();
            resetKryptonSession();
            return;
        }
        kryptonWsSession.setCreateSessionLanguagePack(languagePack);

        sessionObjectJsonArray = methodJson.optJSONArray("sessionObjects");
        this.kryptonWsSession.setCreateSessionObjects(sessionObjectJsonArray);

        String authType = kryptonWsSession.getPluginConfig().getString(KRYPTON_WS_AUTH_TYPE.getKey(), KRYPTON_WS_AUTH_TYPE.getDefaultValue().toString());

        var webapiStartSessionCommandBuilder = new WebapiStartSessionCommandBuilder(
                Integer.toString(kryptonWsSession.getNextWebapiStepId()), applicationKey,
                authType, secret);
        webapiStartSessionCommandBuilder.setParameters(kryptonWsParameters.getConfigurations());

        buildClientSessionId(webapiStartSessionCommandBuilder);

        JSONObject webapiStartSessionCommandJson = webapiStartSessionCommandBuilder.buildRequest();
        sendToWebapi(webapiStartSessionCommandJson.toString());
    }

    private void buildClientSessionId(WebapiStartSessionCommandBuilder webapiStartSessionCommandBuilder)
    {
        webapiStartSessionCommandBuilder.clientSessionId(kryptonWsSession.getKryptonWsSessionId());

        var hybrid = getHybrid();
        if(hybrid != null)
        {
            var dialogSessionId = hybrid.optString("dialogSessionId", null);
            if(dialogSessionId != null)
            {
                webapiStartSessionCommandBuilder.clientSessionId(dialogSessionId);
            }
        }
    }

    private String getApplicationKey()
    {
        var hybrid = getHybrid();
        if(hybrid != null) {
            var clientId = hybrid.optString("clientId", null);
            if (clientId != null) {
                return clientId;
            }
        }

        var clientData = methodJson.optJSONObject("clientData");

        if(clientData != null) {
            var companyName = clientData.optString("companyName", "Nuance");
            var applicationName = clientData.optString("applicationName", "MyApp");
            ThreadContext.put(ThreadContextRunnable.COMPANY_NAME, companyName);
            ThreadContext.put(ThreadContextRunnable.APP_NAME, applicationName);

            return companyName + "_" + applicationName;

        }

        return null;
    }

    private String getApplicationVersion() {
        var clientData = methodJson.optJSONObject("clientData");
        if (clientData != null) {
            return clientData.optString("applicationVersion", null);
        }
        return null;
    }

    private String getSecret()
    {
        String secret = null;
        var hybrid = getHybrid();
        if(hybrid != null)
        {
            secret = hybrid.optString("secret", null);
        }
        if(secret != null) return secret;

        var trust = methodJson.optJSONObject("trust");
        if(trust != null)
        {
            return trust.optString("signature", null);
        }

        return null;
    }

    private void terminateSessionWithError(String errorMessage) {
        sendErrorResponse(errorMessage, null);
        cleanup();
        resetKryptonSession();
    }

    @Override
    public void processWebapiResponse(String webapiResponseJson)
    {
        logger.info("Got webapi response: {}", webapiResponseJson);

        WebApiResponse webApiResponse;
        try
        {
            webApiResponse = WebApiResponse.fromJson(webapiResponseJson);
        }
        catch (JsonProcessingException e)
        {
            var errorMessage = "Unable to parse webapi response json";
            logger.warn("{}: {}", errorMessage, webapiResponseJson);
            sendErrorResponse(errorMessage, null);
            cleanup();
            resetKryptonSession();
            return;
        }

        if(webApiResponse.isError())
        {
            sendErrorResponse(webApiResponse.getErrorsAsString(), null);
            cleanup();
            resetKryptonSession();
            return;
        }

        kryptonWsSession.setWebapiSessionId(webApiResponse.sessionId);

        CombinedConfiguration webapiCombinedConfig = kryptonWsSession.getWebapiSessionConfig();
        kryptonWsSession.getKryptonWsParameters().afterCreateSessionAddConfigToWebapi(webapiCombinedConfig);
        areMetricsEnabled = kryptonWsSession.getPluginConfig().getBoolean(KRYPTON_WS_ENABLE_METRICS.getKey());

        var sendResponse = true;
        if(sessionObjectJsonArray != null && sessionObjectJsonArray.length() > 0)
        {
            boolean loading = loadSessionObjects();
            sendResponse = !loading;
        }

        if(kryptonWsLoadMethod != null)
        {
            kryptonWsResponseBuilder.setSessionObjects(kryptonWsLoadMethod.getLoadedObjects().toList());
        }

        if(sendResponse)
        {
            sendCompleteResponse();
            cleanup();
        }
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    private void createKryptonWsSession()
    {
        logger.trace("createKryptonWsSession");
        try
        {
            var kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();
            IWebApiEndpoint webApiEndpoint = kryptonWsSession.getWebapiFeatures().getWebApiEndpoint(
                    kryptonWsWebapiResponseListener);

            kryptonWsSession.setWebapiResponseListener(kryptonWsWebapiResponseListener)
                            .setWebApiEndpoint(webApiEndpoint)
                            .setKryptonWsSessionId(getMethodKryptonWsSessionIdOrGenerate());
        }
        catch (IOException e)
        {
            logger.warn("Couldn't get webApiEndpoint: ", e);
        }
    }

    @Override
    public boolean isKryptonWsSessionIdAssociatedToThisKryptonWsWsSession()
    {
        return true;
    }

    @Override
    public boolean validateKryptonSessionEstablished()
    {
        //For CreateSession, we only validate when there's no session
        return !kryptonWsSession.isKryptonSessionEstablished();
    }

    protected boolean loadSessionObjects()
    {
        logger.trace("loadSessionObjects");
        JSONObject loadRequest = new JSONObject()
                .put("method", KryptonWsLoadMethod.NAME)
                .put("version", "1.0")
                .put("sessionId", kryptonWsSession.getKryptonWsSessionId())
                .put("requestId", kryptonWsRequestId + "-Load")
                .put("objects", sessionObjectJsonArray);

        sessionObjectJsonArray = null;

        kryptonWsLoadMethod = new KryptonWsLoadMethod(loadRequest, kryptonWsSession);
        return kryptonWsLoadMethod.process(this);
    }

    public String validateLanguagePack(JSONObject languagePack)
    {
        if(languagePack == null)
        {
            return "languagePack is required";
        }

        var language = languagePack.optString("language", null);
        if(language == null)
        {
            return "languagePack.language is required";
        }

        return null;
    }

    protected void setKryptonWsLoadMethod(KryptonWsLoadMethod loadMethod) {
        kryptonWsLoadMethod = loadMethod;
    }

    protected JSONObject getHybrid() {
        return methodJson.optJSONObject("hybrid");
    }
}
