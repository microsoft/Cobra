package com.nuance.cobra.api.globalregistry.adapter;

import com.nuance.cobra.api.globalregistry.parser.GlobalRegistryJSONParser;
import org.json.JSONObject;

/**
 * Adapter class for global registry JSON parser
 *
 * @param <T>
 *         type of global registry element
 */
public class GlobalRegistryJSONParserAdapter<T>
{
    private GlobalRegistryJSONParser<T> parser;

    public GlobalRegistryJSONParserAdapter(GlobalRegistryJSONParser<T> parser)
    {
        this.parser = parser;
    }

    public T parseJSON(JSONObject jsonObject)
    {
        return parser.parseJSON(jsonObject);
    }

}
