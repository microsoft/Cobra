package com.nuance.plugin.kryptonws.methods;

import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.InputMismatchException;

public class KryptonWsMethodFactory
{
    private static final Logger logger = LogManager.getLogger(KryptonWsMethodFactory.class);

    private static final String[] allowedMethods = {
            KryptonWsCreateSessionMethod.NAME,
            KryptonWsLoadMethod.NAME,
            KryptonWsRecognizeMethod.NAME,
            KryptonWsInterpretMethod.NAME,
            KryptonWsStartRecognitionTimersMethod.NAME,
            KryptonWsStopMethod.NAME,
            KryptonWsEndOfInputMethod.NAME,
            KryptonWsEndSessionMethod.NAME
    };

    private KryptonWsMethodFactory() { throw new IllegalStateException("Utility class"); }

    public static KryptonWsMethod createMethod(String message, KryptonWsSession kryptonWsSession) throws InvocationTargetException {
        logger.debug("createMethod");

        final KryptonWsMethod rc;
        JSONObject jsonObject;
        try
        {
            jsonObject = new JSONObject(message);
            String method = isMethodAllowed(jsonObject.getString("method"));

            Class<?> clazz = Class.forName("com.nuance.plugin.kryptonws.methods.KryptonWs" + method + "Method");
            Constructor<?> constructor = clazz.getDeclaredConstructor(JSONObject.class, KryptonWsSession.class);
            rc = (KryptonWsMethod) constructor.newInstance(jsonObject, kryptonWsSession);
        } catch (JSONException | InvocationTargetException jsonE){
            throw jsonE;
        } catch (Exception e){
            logger.info("Could not create KryptonWsMethod: ", e);
            return null;
        }

        return rc;
    }

    //Return the allowedMethod from the internally-declared array to satisfy Veracode
    private static String isMethodAllowed(String input)
    {
        for(String allowedMethod: allowedMethods)
        {
            if(input.equals(allowedMethod))
            {
                return allowedMethod;
            }
        }
        throw new InputMismatchException("A valid Krypton method was not specified.");
    }
}
