package com.nuance.cobra.api.globalregistry.parser;

import com.nuance.cobra.api.globalregistry.Grammar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Custom JSON parser for global registry "Grammar" element
 */
public class GrammarJSONParser
        implements GlobalRegistryJSONParser<Grammar>
{

    private static final Logger logger = LogManager.getLogger(GrammarJSONParser.class);

    @Override
    public Grammar parseJSON(JSONObject jsonObject)
    {
        Grammar grammar = new Grammar();
        for (String key : jsonObject.keySet())
        {
            if (key.equalsIgnoreCase("current"))
            {
                grammar.setCurrentVersion((jsonObject.get(key)).toString());
            }
            else
            {
                grammar.addVersionURL(key, (jsonObject.get(key)).toString());
            }
        }
        return grammar;

    }

}
