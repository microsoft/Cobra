package com.nuance.plugin.kryptonws.methods;

import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class KryptonWsInvalidMethod
        extends KryptonWsMethod
{
    private static Logger logger = LogManager.getLogger(KryptonWsInvalidMethod.class);

    public KryptonWsInvalidMethod(JSONObject jsonObject,
            KryptonWsSession kryptonWsSession)
    {
        super(jsonObject, kryptonWsSession);
    }

    @Override
    public void processWebapiResponse(String webapiResponse)
    {
        //Empty
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }
}
