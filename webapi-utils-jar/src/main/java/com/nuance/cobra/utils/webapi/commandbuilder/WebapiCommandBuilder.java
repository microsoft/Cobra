package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.commons.configuration2.Configuration;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

public abstract class WebapiCommandBuilder
{
    protected String type;
    protected String name;

    protected WebapiCommandBuilder(String type, String name)
    {
        this.type = type;
        this.name = name;
    }

    public void setParameters(Configuration parameters)
    {
        for (Iterator<String> it = parameters.getKeys(); it.hasNext(); )
        {
            String key = it.next();
            Object value = parameters.getProperty(key);
            try
            {
                java.lang.reflect.Method method = this.getClass().getDeclaredMethod(key, value.getClass());
                method.invoke(this, value);
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
            {
                //Do nothing
            }
        }
    }

    public abstract JSONObject buildRequest();
}
