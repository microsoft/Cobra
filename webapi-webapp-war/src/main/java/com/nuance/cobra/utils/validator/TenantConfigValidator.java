package com.nuance.cobra.utils.validator;

import com.google.re2j.Pattern;
import com.nuance.cobra.command.SpeechSynthesisCommand;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TenantConfigValidator {
    private static JSONObject apiConfig;
    private static final String ERROR_VALUE = "Error";
    private static final String SCHEMA_ERROR_VALUE = "Schema Error";

    static {
        WebApiConfigParser parser = new WebApiConfigParser();
        apiConfig = parser.getApiConfig();
    }

    public JSONObject jsonCompare(JSONObject json) {
        return compareConfigs(json);
    }

    private JSONObject compareConfigs(JSONObject userTenantConfig) {
        JSONObject response = new JSONObject();

        Iterator<String> keys = userTenantConfig.keys();

        while (keys.hasNext()){
            String key = keys.next();
            Object value = userTenantConfig.get(key);

            JSONObject resObject;
            switch (key) {
                case "vocsMapping":
                    resObject = checkVocsSchema(value);
                    break;
                case "projectId":
                    resObject = validateNumericString((String) value);
                    break;
                case "cloudEnvironment":
                    resObject = validateString(value);
                    break;
                case "publicKeysMap":
                case "kryptonLanguageToUrlMapping":
                case "speechSynthesisTuningBundles":
                    resObject = validateStringKeyMap(value);
                    break;
                case "activePluginsList":
                    resObject = validateStringKeyMapArray(value);
                    break;
                case "kryptonDomainLmWeight":
                    resObject = validateStringIntegerDataType(value);
                    break;
                case "variables":
                    continue;
                default:
                    resObject = validateConfig(key, value);
                    break;
            }
            createResponse(key, resObject, response);
        }
        return response;
    }

    private JSONObject validateConfig(String key, Object userValue) {
        JSONObject response = new JSONObject();
        JSONObject resObject;
        JSONObject apiConfigElement;
        if(!apiConfig.has(key)){
            response = getNoKeyResponse();
        }else{
            apiConfigElement = apiConfig.getJSONObject(key);
            String userValueType = userValue.getClass().getSimpleName();
            String expectedValueType = apiConfigElement.getString("type");
            if(userValue.equals("")) {
                response = getBlankValueResponse();
            }else if(!userValueType.equals(expectedValueType)){
                resObject = getWrongDataTypeResponse(userValue, userValueType, expectedValueType);
                createResponse("Wrong Data Type", resObject, response);
            }else if(apiConfigElement.has("validValues")){
                Object validValues = apiConfigElement.get("validValues");
                resObject =  checkValidValues(validValues, userValue, userValueType, expectedValueType);
                createResponse("Value Not Allowed", resObject, response);
            }
        }
        return response;
    }

    private JSONObject checkValidValues(Object validValues, Object userValues, String userInputType, String expectedInputType) {
        JSONObject resObject = new JSONObject();
        if(userInputType.equals("String") && !validValues.toString().contains(userValues.toString())) {
            resObject = getWrongValidValueResponse(userValues, validValues, expectedInputType);
        }else if(userInputType.equals("JSONArray")) {
            List<String> userValueList = getValuesList((JSONArray) userValues);
            List<String> validValuesList = getValuesList((JSONArray) validValues);
            userValueList.removeAll(validValuesList);
            if(!userValueList.isEmpty()){
                resObject = getWrongValidValueResponse(userValueList, validValuesList, expectedInputType);
            }
        }
        return resObject;
    }

    private JSONObject validateStringKeyMapArray(Object value) {
        JSONObject response = validateJSONArray(value);
        if(response.length() == 0){
            for(Object object : (JSONArray) value){
                JSONObject resObject = validateStringKeyMap(object);
                createResponse(ERROR_VALUE, resObject, response);
            }
        }
        return response;
    }

    private JSONObject validateStringKeyMap(Object value) {
        JSONObject response = validateJSONObject(value);
        JSONObject resObject;
        if(response.length() == 0){
            JSONObject keyMap = (JSONObject) value;
            Iterator<String> keys = keyMap.keys();
            while(keys.hasNext()){
                String key = keys.next();
                if(checkParentKey(key)){
                    resObject = validateConfig(key, keyMap.get(key));
                }else if(keyMap.get(key) instanceof JSONObject){
                    resObject = validateStringKeyMap(keyMap.getJSONObject(key));
                }else if(keyMap.get(key) instanceof JSONArray){
                    resObject = validateStringKeyMapArray(keyMap.getJSONArray(key));
                }else{
                    resObject = validateString(keyMap.get(key));
                }
                createResponse(key, resObject, response);
            }
        }
        return response;
    }

    private boolean checkParentKey(String key) {
        boolean hasKey = false;
        if(apiConfig.has(key)){
            hasKey = true;
        }
        return hasKey;
    }

    private JSONObject checkVocsSchema(Object value) {
        JSONObject response = validateJSONObject(value);
        if(response.length() == 0){
            JSONObject resObject;
            JSONObject vocsMap = (JSONObject) value;
            Iterator<String> voices = vocsMap.keys();

            while (voices.hasNext()) {
                String key = voices.next();
                resObject = validateJSONObject(vocsMap.get(key));
                if(resObject.length() == 0){
                    resObject = checkVoiceSchema((JSONObject) vocsMap.get(key));
                }
                createResponse(key, resObject, response);
            }
        }
        return response;
    }

    private JSONObject checkVoiceSchema(JSONObject voiceSchema) {
        JSONObject response = new JSONObject();
        Iterator<String> voiceMap = voiceSchema.keys();

        while(voiceMap.hasNext()){
            JSONObject resObject;
            String key = voiceMap.next();
            switch (key){
                case "name":
                case "vocsURL":
                case "voice_gender":
                case "voice_age":
                case "language":
                    resObject = validateString(voiceSchema.get(key));
                    createResponse(key, resObject, response);
                    break;
                case "pcm_16_8k":
                case "pcm_16_16k":
                case "pcm_16_22k":
                case "ima_adpcm":
                case "opus_wb":
                case "opus_16_16k":
                case "opus_16_24k":
                case "opus_nb":
                case "opus_16_8k":
                case "ulaw":
                case "alaw":
                    resObject = validateStringKeyMap(voiceSchema.get(key));
                    createResponse(key, resObject, response);
                    break;
                case SpeechSynthesisCommand.TUNABLE_DATA:
                case SpeechSynthesisCommand.USER_DICTIONARY:
                    resObject = validateStringKeyMapArray(voiceSchema.get(key));
                    createResponse(key, resObject, response);
                    break;
                default:
                    resObject = getNoKeyResponse();
                    createResponse(key, resObject, response);
                    break;
            }
        }
        return response;
    }

    private JSONObject validateNumericString(String value) {
        JSONObject response = new JSONObject();
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if(!(pattern.matches(value))){
            JSONObject resObject = getWrongNumericStringResponse(value);
            createResponse("Wrong Input", resObject, response);
        }
        return response;
    }

    private JSONObject validateString(Object value) {
        JSONObject response = new JSONObject();
        if(!(value instanceof String)){
            JSONObject resObject = getWrongDataTypeResponse(value, value.getClass().getSimpleName(), "String");
            createResponse("Wrong Data Type", resObject, response);
        }
        return response;
    }

    private Object trimResponse(Object str){
        return String.format("%s",str.toString().replaceAll("[!@#$%^&*<>]", ""));
    }

    private JSONObject validateJSONObject(Object value) {
        JSONObject resObject;
        JSONObject response = new JSONObject();
        if(!(value instanceof JSONObject)){
             resObject = getWrongDataTypeResponse(value.getClass().getSimpleName(), "JSONObject");
             createResponse(SCHEMA_ERROR_VALUE, resObject, response);
        }else if(((JSONObject) value).length() == 0){
            resObject = getEmptySchemaResponse();
            createResponse(SCHEMA_ERROR_VALUE, resObject, response);
        }
        return response;
    }

    private JSONObject validateJSONArray(Object value) {
        JSONObject response = new JSONObject();
        JSONObject resObject;

        if(!(value instanceof JSONArray)){
            resObject = getWrongDataTypeResponse(value.getClass().getSimpleName(), "JSONArray");
            createResponse(SCHEMA_ERROR_VALUE, resObject, response);
        }
        else if(((JSONArray) value).length() == 0)
        {
            resObject = getEmptySchemaResponse();
            createResponse(SCHEMA_ERROR_VALUE, resObject, response);
        }
        return response;
    }

    private JSONObject validateStringIntegerDataType(Object value){
        JSONObject response = new JSONObject();
        String type = value.getClass().getSimpleName();
        if(!type.equals("String") && !type.equals("Integer")){
            response = getWrongDataTypeResponse(value.getClass().getSimpleName(), "[\"String\" or \"Integer\"]");
        }
        return response;
    }

    private List<String> getValuesList(JSONArray userValues) {
        List<String> list = new ArrayList<>();
        for(Object o : userValues) {
            list.add(o.toString());
        }
        return list;
    }

    private JSONObject getNoKeyResponse() {
        JSONObject resObject = new JSONObject();
        resObject.put(ERROR_VALUE, "Wrong Key");
        return resObject;
    }

    private JSONObject getWrongDataTypeResponse(String userInputType, String expectedInputType) {
        JSONObject resObject = new JSONObject();
        resObject.put("Defined Schema", userInputType);
        resObject.put("Expected Schema", expectedInputType);
        return resObject;
    }

    private JSONObject getWrongDataTypeResponse(Object userValue, String userInputType, String expectedInputType) {
        JSONObject resObject = new JSONObject();
        resObject.put("Value", trimResponse(userValue));
        resObject.put("Defined Data Type", userInputType);
        resObject.put("Expected Data Type", expectedInputType);
        return resObject;
    }

    private JSONObject getWrongValidValueResponse(Object userValues, Object validValues, String expectedInputType) {
        JSONObject resObject = new JSONObject();
        resObject.put("User Values", trimResponse(userValues));
        resObject.put("Allowed Values", trimResponse(validValues));
        resObject.put("Expected Data Type", expectedInputType);
        return resObject;
    }

    private JSONObject getWrongNumericStringResponse(String value) {
        JSONObject resObject = new JSONObject();
        resObject.put("User Values", trimResponse(value));
        resObject.put(ERROR_VALUE, "Invalid Numeric String / Value contains alphabet.");
        return resObject;
    }

    private JSONObject getBlankValueResponse() {
        JSONObject resObject = new JSONObject();
        resObject.put(ERROR_VALUE, "Invalid Value / Blank String");
        return resObject;
    }

    private void createResponse(String key, JSONObject resObject, JSONObject response) {
        if(resObject.length() != 0){
            response.put((String) trimResponse(key), resObject);
        }
    }

    private JSONObject getEmptySchemaResponse() {
        JSONObject response = new JSONObject();
        response.put("Error", "Schema can't be Empty.");
        return response;
    }
}
