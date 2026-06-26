package com.nuance.cobra.api.globalregistry.parser;

import com.nuance.cobra.api.globalregistry.ApplicationVersion;
import com.nuance.cobra.api.globalregistry.Grammar;
import com.nuance.cobra.api.globalregistry.adapter.GlobalRegistryJSONParserAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Custom JSON parser for global registry "ApplicationVersion" element
 */
public class ApplicationVersionJSONParser
        implements GlobalRegistryJSONParser<ApplicationVersion>
{

    private static final Logger logger = LogManager.getLogger(ApplicationJSONParser.class);

    @Override
    public ApplicationVersion parseJSON(JSONObject jsonObject)
    {
        ApplicationVersion appVersion = new ApplicationVersion();
        for (Object key : jsonObject.keySet())
        {
            String keyName = (String) key;
            logger.debug("Grammar Name: " + keyName);
            Object grammerObj = jsonObject.get(keyName);
            if (grammerObj instanceof JSONObject)
            {
                GlobalRegistryJSONParserAdapter<Grammar> parserAdapter = new GlobalRegistryJSONParserAdapter<Grammar>(
                        new GrammarJSONParser());
                Grammar grammer = parserAdapter.parseJSON((JSONObject) grammerObj);
                appVersion.addGrammars(keyName, grammer);
            }
        }
        return appVersion;
    }

}
