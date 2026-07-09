package com.nuance.plugin.kryptonws.methods;

import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class KryptonWsStartRecognitionTimersMethod
        extends KryptonWsMethod
{
    public static final String NAME = "StartRecognitionTimers";
    public static final String PARAM_NAME = "startRecognitionTimers";

    private static Logger logger = LogManager.getLogger(KryptonWsStartRecognitionTimersMethod.class);

    protected KryptonWsStartRecognitionTimersMethod(JSONObject jsonObject, KryptonWsSession kryptonWsSession)
    {
        super(jsonObject, kryptonWsSession);
    }

    @Override
    public void process()
    {
        super.process();

        KryptonWsMethod kryptonWsMethod = kryptonWsSession.getWebapiResponseListener().getCurrentMethod();
        if(kryptonWsMethod == null)
        {
            sendNoActionResponse();
        } else if(kryptonWsMethod instanceof KryptonWsRecognizeMethod)
        {
            KryptonWsRecognizeMethod kryptonWsRecognizeMethod = (KryptonWsRecognizeMethod) kryptonWsMethod;
            KryptonWsMessage.STATUS status = kryptonWsRecognizeMethod.startRecognitionTimer(true);
            logger.trace("startRecognitionTimer returned {}", status.statusString);
            sendResponse(status);
        } else
        {
            //Session not idle, processing commands other than Recognize
            sendErrorResponse("Processing a method other than Recognize", null);
        }
    }


    @Override
    protected void onAudioImplementation(byte[] audio)
    {
        getLogger().debug("onAudioImplementation");
    }

    @Override
    public void processWebapiResponse(String webapiResponse)
    {
        getLogger().debug("processWebapiResponse");
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
