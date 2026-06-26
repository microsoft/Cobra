package com.nuance.cobra.api.globalregistry.parser;

import org.json.JSONObject;

public interface GlobalRegistryJSONParser<T>
{
    public T parseJSON(JSONObject jsonObject);
}
