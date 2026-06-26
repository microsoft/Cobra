package com.nuance.plugin.kryptonws.session;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.http.FakeHttpSession;
import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.plugin.kryptonws.params.KryptonWsParameters;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseSender;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import com.nuance.plugin.v1.shareable.ISession;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class KryptonWsSession {
    private static final Logger logger = LogManager.getLogger(KryptonWsSession.class);
    private ExecutorService executorService;
    private KryptonWsParameters kryptonWsParameters;
    private JSONObject createSessionLanguagePack;
    private JSONObject semanticModel;
    private JSONArray createSessionObjects;
    private Map<String, JSONObject> sessionObjects = new HashMap<>();
    private Map<String, JSONObject> wordsetObjects = new HashMap<>();
    private Set<String> wordSetIds = new HashSet<>();
    private Session kryptonWsWsSession;
    private KryptonWsResponseSender kryptonWsResponseSender;
    private String kryptonWsSessionId = null;

    private int webapiStepId = 0;
    private IWebApiEndpoint webApiEndpoint;
    private IWebApiFeatures webapiFeatures;
    private KryptonWsWebapiResponseListener webapiResponseListener;
    private WebSocketHttpSession webapiWsSession = null;
    private String webapiSessionId;

    public KryptonWsSession(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void initWebapiWsSession() {
        if (webapiWsSession != null) return;
        webapiWsSession = new WebSocketHttpSession(new FakeHttpSession());

    }

    public void terminateWebapiWsSession(CloseReason closeReason) {
        logger.debug("terminateWebapiWsSession");
        if (webapiWsSession != null) {
            try {
                logger.debug("Calling onClose() and close() on the webapi endpoint");
                webApiEndpoint.onClose(webapiWsSession, closeReason);
                webapiWsSession.close();
            } catch (Exception e) {
                logger.debug("Fail to close webapiWsSession", e);
            }
        }
        webapiWsSession = null;
    }

    public int getNextWebapiStepId() {
        return ++webapiStepId;
    }

    public Map<String, JSONObject> getSessionObjects() {
        return sessionObjects;
    }

    public KryptonWsSession setSessionObjects(Map<String, JSONObject> sessionObjects) {
        this.sessionObjects = sessionObjects;
        return this;
    }

    public Map<String, JSONObject> getWordsetObjects() {
        return wordsetObjects;
    }

    public KryptonWsSession setWordsetObjects(Map<String, JSONObject> wordsetObjects) {
        this.wordsetObjects = wordsetObjects;
        return this;
    }

    public JSONObject getCreateSessionLanguagePack() {
        return createSessionLanguagePack;
    }

    public KryptonWsSession setCreateSessionLanguagePack(JSONObject createSessionLanguagePack) {
        this.createSessionLanguagePack = createSessionLanguagePack;
        return this;
    }

    public IWebApiEndpoint getWebApiEndpoint() {
        return webApiEndpoint;
    }

    public KryptonWsSession setWebApiEndpoint(IWebApiEndpoint webApiEndpoint) {
        this.webApiEndpoint = webApiEndpoint;
        return this;
    }

    public KryptonWsWebapiResponseListener getWebapiResponseListener() {
        return webapiResponseListener;
    }

    public KryptonWsSession setWebapiResponseListener(
            KryptonWsWebapiResponseListener webapiResponseListener) {
        this.webapiResponseListener = webapiResponseListener;
        return this;
    }

    public WebSocketHttpSession getWebapiWsSession() {
        return webapiWsSession;
    }

    public String getWebapiSessionId() {
        return webapiSessionId;
    }

    public KryptonWsSession setWebapiSessionId(String webapiSessionId) {
        this.webapiSessionId = webapiSessionId;
        return this;
    }

    public Session getKryptonWsWsSession() {
        return kryptonWsWsSession;
    }

    public KryptonWsSession setKryptonWsWsSession(Session kryptonWsWsSession) {
        this.kryptonWsWsSession = kryptonWsWsSession;
        return this;
    }

    public IWebApiFeatures getWebapiFeatures() {
        return webapiFeatures;
    }

    public KryptonWsSession setWebapiFeatures(IWebApiFeatures webapiFeatures) {
        this.webapiFeatures = webapiFeatures;
        return this;
    }

    public KryptonWsResponseSender getKryptonWsResponseSender() {
        return kryptonWsResponseSender;
    }

    public KryptonWsSession setKryptonWsResponseSender(KryptonWsResponseSender kryptonWsResponseSender) {
        this.kryptonWsResponseSender = kryptonWsResponseSender;
        return this;
    }

    public String getKryptonWsSessionId() {
        return kryptonWsSessionId;
    }

    public KryptonWsSession setKryptonWsSessionId(String kryptonWsSessionId) {
        this.kryptonWsSessionId = kryptonWsSessionId;
        return this;
    }

    public CombinedConfiguration getWebapiSessionConfig() {
        return getWebapiSession().getSessionConfig();
    }

    public Configuration getPluginConfig() {
        try {
            return webapiFeatures.getConfigs();
        } catch (IOException e) {
            logger.info("Could not get plugin config: ", e);
            return new WebApiJSONConfiguration();
        }
    }

    public Configuration getWebapiStartSessionConfig() {
        return getWebapiSessionConfig().getConfiguration("startSessionParams");
    }

    public boolean isKryptonSessionEstablished() {
        return kryptonWsSessionId != null;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Code below is for unit tests
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setCreateSessionObjects(JSONArray createSessionObjects) {
        this.createSessionObjects = createSessionObjects;
    }

    public JSONArray getCreateSessionObjects() {
        if (createSessionObjects == null) {
            return new JSONArray();
        }
        return createSessionObjects;
    }

    public KryptonWsParameters getKryptonWsParameters() {
        return kryptonWsParameters;
    }

    public void setKryptonWsParameters(KryptonWsParameters kryptonWsParameters) {
        this.kryptonWsParameters = kryptonWsParameters;
    }

    public JSONObject getSemanticModel() {
        return semanticModel;
    }

    public void setSemanticModel(JSONObject semanticModel) {
        this.semanticModel = semanticModel;
    }


    /*
     * It would be better to get rid of the KryptonWsSession completely
     * but we don't want to get rid of the IWebApiFeatures and (websocket) Session for websocket session reuse
     * and we don't really want to pass around the KryptonWsPlugin object
     */
    public void resetKryptonSession() {
        logger.trace("resetKryptonSession");
        //Don't null the executorService

        if (kryptonWsParameters != null) {
            kryptonWsParameters.clearCombinedConfig(kryptonWsSessionId);
        }

        kryptonWsParameters = null;
        createSessionLanguagePack = null;
        semanticModel = null;
        createSessionObjects = null;
        sessionObjects = new HashMap<>();
        kryptonWsSessionId = null;
        webApiEndpoint = null;
        webapiResponseListener = null;
        webapiWsSession = null;
        webapiSessionId = null;
        // Don't null kryptonWsWsSession, kryptonWsResponseSender and webapiFeatures
    }

    public Set<String> getWordSetIds() {
        return wordSetIds;
    }

    public void addWordSet(String wordSetId) {
        wordSetIds.add(wordSetId);
    }

    public ISession getWebapiSession() {
        return getWebApiEndpoint().getWebapiSession(getWebapiSessionId());
    }
}


