package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.kryptonws.KryptonWsPlugin;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.util.LinkedHashMap;
import java.util.List;

public class KryptonWsEndSessionMethod
        extends KryptonWsMethod
{

    private static final Logger logger = LogManager.getLogger();
    public static final String NAME = "EndSession";


    protected KryptonWsEndSessionMethod(JSONObject jsonObject, KryptonWsSession kryptonWsSession) {
        super(jsonObject, kryptonWsSession);
        logger.debug("KryptonWsEndSessionMethod Constructor");
    }

    @Override
    public void process() {
        super.process();
        JSONObject command = new JSONObject()
                .put("name", "endSession")
                .put("id", this.kryptonWsRequestId);
        JSONObject endSession = new JSONObject()
                .put("type", "command")
                .put("sessionId", this.kryptonWsSession.getWebapiSessionId())
                .put("command", command);

        sendToWebapi(endSession.toString());
    }

    @Override
    protected void onAudioImplementation(byte[] audio) {

    }

    @Override
    public void processWebapiResponse(String webapiResponse) {
        logger.info("Got webapi response {}.", webapiResponse);

        boolean closeSession = false;
        KryptonWsMessage.STATUS status = KryptonWsMessage.STATUS.FAILED;

        try {
            WebApiResponse response = WebApiResponse.fromJson(webapiResponse);

            if (response.command != null && !response.command.get("name").equals("endSession") && response.isCanceled())
            {
                return; // ignore cancelled responses from active command
            }

            if (response.isError()) {

                logger.warn("There was an error closing session {}.\n{}", this.kryptonWsSession.getKryptonWsSessionId(), response.getErrorsAsString());

                for (Object error : (List) response.result.get("errors")) {
                    kryptonWsResponseBuilder.addError((String) ((LinkedHashMap) error).get("message"), null);
                }
            } else {
                closeSession = true;

                logger.info("Successfully closed session {}.", kryptonWsSession.getKryptonWsSessionId());

                status = KryptonWsMessage.STATUS.COMPLETE;

                logger.info("Successfully closed session {}.", kryptonWsSession.getKryptonWsSessionId());
            }

        } catch (JsonProcessingException e) {

            logger.warn("There was an issue parsing the webapi response: {}\n{}", webapiResponse, e.getStackTrace());
        }

        sendResponse(status);
        cleanup();

        if (closeSession) {
            KryptonWsPlugin.closeKryptonWsWsSession(this.kryptonWsSession, new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal closure."));
            resetKryptonSession();
        }
    }

    @Override
    public boolean canRunWhile(KryptonWsMethod commandThatIsAlreadyRunning)
    {
        return super.canRunWhile(commandThatIsAlreadyRunning) || commandThatIsAlreadyRunning.getMethodName().equals(KryptonWsRecognizeMethod.NAME);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
