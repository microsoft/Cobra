package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.commandbuilder.WebapiStopDirectiveBuilder;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class KryptonWsStopMethod
        extends KryptonWsMethod
{
    private static final Logger logger = LogManager.getLogger();
    public static final String NAME = "Stop";

    public KryptonWsStopMethod(JSONObject jsonObject, KryptonWsSession kryptonWsSession)
    {
        super(jsonObject, kryptonWsSession);
        logger.debug("KryptonWsStopMethod constructor");
    }

    @Override
    protected void onAudioImplementation(byte[] audio)
    {
        logger.trace("Stop ignoring {} audio bytes", audio.length);
    }

    @Override
    public void process()
    {
        super.process();
        logger.trace("process");

        try {
            KryptonWsMethod currentMethod = kryptonWsSession.getWebapiResponseListener().getCurrentMethod();
            if (currentMethod instanceof KryptonWsRecognizeMethod)
            {
                KryptonWsRecognizeMethod recognizeMethod = (KryptonWsRecognizeMethod) currentMethod;
                recognizeMethod.stopRecognitionTimer();

                // if recognition was NOT stopped
                if (recognizeMethod.recognitionWasStopped.compareAndSet(false, true))
                {
                    WebapiStopDirectiveBuilder webapiStopDirectiveBuilder = new WebapiStopDirectiveBuilder(kryptonWsSession.getWebapiSessionId(), kryptonWsSession.getWebapiSession().getCommand().getCommandId(), "speechRecognition");
                    JSONObject webapiStopDirectiveJson = webapiStopDirectiveBuilder.buildRequest();
                    sendToWebapi(webapiStopDirectiveJson.toString());
                }
            } else if(kryptonWsSession.getWebapiSession().getCommand() != null) {
                WebapiStopDirectiveBuilder webapiStopDirectiveBuilder = new WebapiStopDirectiveBuilder(kryptonWsSession.getWebapiSessionId(), kryptonWsSession.getWebapiSession().getCommand().getCommandId(), "speechRecognition");
                JSONObject webapiStopDirectiveJson = webapiStopDirectiveBuilder.buildRequest();
                sendToWebapi(webapiStopDirectiveJson.toString());
            }
            else {
                sendNoActionResponse();
            }
        }
        catch (Exception e) {
            sendErrorResponse(e.getMessage(), null);
        }
    }

    @Override
    public void processWebapiResponse(String webapiResponse)
    {
        WebApiResponse webApiResponse;

        try {
            webApiResponse = WebApiResponse.fromJson(webapiResponse);
        }
        catch (JsonProcessingException e) {
            sendErrorResponse(e.getMessage(), null);
            return;
        }

        if(webApiResponse.isError()) {
            sendErrorResponse(webApiResponse.getErrorsAsString(), null);
            return;
        }

        sendCompleteResponse();
        cleanup();
    }

    @Override
    public boolean canRunWhile(KryptonWsMethod commandThatIsAlreadyRunning)
    {
        return true;
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }
}
