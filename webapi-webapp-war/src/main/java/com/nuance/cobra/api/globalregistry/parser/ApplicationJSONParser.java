package com.nuance.cobra.api.globalregistry.parser;

import com.nuance.cobra.api.globalregistry.Application;
import com.nuance.cobra.api.globalregistry.ApplicationVersion;
import com.nuance.cobra.api.globalregistry.adapter.GlobalRegistryJSONParserAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Custom JSON parser for global registry "Application" element
 */
public class ApplicationJSONParser
        implements GlobalRegistryJSONParser<Application>
{

    private static final Logger logger = LogManager.getLogger(ApplicationJSONParser.class);

    @Override
    public Application parseJSON(JSONObject jsonObject)
    {
        Application application = new Application();
        for (String keyName : jsonObject.keySet())
        {
            Object appVersionObj = jsonObject.get(keyName);
            if (appVersionObj instanceof JSONObject)
            {
                GlobalRegistryJSONParserAdapter<ApplicationVersion> adapter =
                        new GlobalRegistryJSONParserAdapter<ApplicationVersion>(
                                new ApplicationVersionJSONParser());
                ApplicationVersion appVersion = adapter.parseJSON((JSONObject) appVersionObj);
                application.addApplicationVersion(keyName, appVersion);
            }

        }
        return application;
    }

}
