package com.nuance.cobra.utils.validator;

import com.nuance.cobra.session.WebApiConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

class WebApiConfigParser {
    private static JSONObject webApiConfig = null;

    WebApiConfigParser(){
        parseApiConfig();
    }

    JSONObject getApiConfig() {
        if(webApiConfig == null){
            parseApiConfig();
        }
        return webApiConfig;
    }
    private static void parseApiConfig(){
        webApiConfig = new JSONObject();

        for(WebApiConfig.Property prop : WebApiConfig.Property.values())
        {
            JSONObject fieldParameter = new JSONObject();
            String fieldName;
            String type;
            Object defaultValue;
            JSONArray validValues;

            if(prop.defaultValue == null){
                fieldName = prop.key;
                type = String.class.getSimpleName();
                fieldParameter.put("type", type);
            }else{
                fieldName = prop.key;
                defaultValue = prop.defaultValue;
                if(defaultValue instanceof List){
                    type = "JSONArray";
                }
                else{
                    type = prop.defaultValue.getClass().getSimpleName();
                }
                fieldParameter.put("type", type);
            }
            if(prop.getValidValues() != null && prop.getValidValues().length > 0){
                validValues = fetchValidValues(prop.getValidValues());
                fieldParameter.put("validValues", validValues);
            }

            webApiConfig.put(fieldName, fieldParameter);
        }
    }

    private static JSONArray fetchValidValues(Object[] values) {
        JSONArray validValues = new JSONArray(Arrays.asList(values));
        if(Arrays.asList(values).get(0) instanceof Enum){
            JSONArray enumValues = new JSONArray();
            for(Object o : values){
                enumValues.put(o);
            }
            validValues = enumValues;
        }
        return validValues;
    }
}

