package com.nuance.cobra.utils.validator;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.owasp.encoder.Encode;

public class TenantConfigValidatorTest {
    private TenantConfigValidator validator;
    @Test
    public void testWrongTenantKey() {
        String json = "{\"jwtIssuer\" : \"webapiQA\",\n" +
                "  \"jwtForceExpirationCheck\" : true,\n" +
                " \"jwtHashAlgorithmWhitelist\" : [ \"HS256\", \"HS384\" ],\n" +
                " \"activeAuthenticationMethods\" : [ \"jws\", \"hashcode\" ], \n" +
                " \"publicKeysMap\" : { \"key1\" : \"test\", \"key2\" : \"test\" }, \n " +
                " \"sessionTimeoutSeconds\" : 60,\n" +
                "  \"sensitiity\" : \"open\"}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"sensitiity\":{\"Error\":\"Wrong Key\"}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testWrongDataType() {
        String json = "{\"jwtIssuer\" : \"webapiQA\",\n" +
                "  \"jwtForceExpirationCheck\" : 10,\n" +
                " \"sessionTimeoutSeconds\" : 60,\n" +
                " \"jwtHashAlgorithmWhitelist\" : [ \"HS256\", \"HS384\" ],\n" +
                " \"publicKeysMap\" : { \"key1\" : \"test\", \"key2\" : \"test\" }, \n " +
                "  \"sensitivity\" : \"open\"}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"jwtForceExpirationCheck\":{\"Wrong Data Type\":{\"Defined Data Type\":\"Integer\",\"Value\":\"10\",\"Expected Data Type\":\"Boolean\"}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testWrongAllowedValue_string() {
        String json = "{\"jwtIssuer\" : \"webapiQA\",\n" +
                "  \"jwtForceExpirationCheck\" : true,\n" +
                " \"speechRecognitionEngine\" : mreNim,\n" +
                " \"sessionTimeoutSeconds\" : 60,\n" +
                " \"jwtHashAlgorithmWhitelist\" : [ \"HS256\", HS384 ],\n" +
                " \"publicKeysMap\" : { \"key1\" : \"test\", \"key2\" : \"test\" }, \n " +
                "  \"sensitivity\" : \"open\"}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"speechRecognitionEngine\":{\"Value Not Allowed\":{\"User Values\":\"mreNim\",\"Allowed Values\":\"[\\\"mrecnim\\\",\\\"krypton-grpc\\\",\\\"speechServiceASR\\\"]\",\"Expected Data Type\":\"String\"}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testWrongAllowedValue_list() {
        String json = "{\"jwtIssuer\" : \"webapiQA\",\n" +
                "  \"jwtForceExpirationCheck\" : true,\n" +
                " \"speechRecognitionEngine\" : mrecnim,\n" +
                " \"sessionTimeoutSeconds\" : 60,\n" +
                " \"jwtHashAlgorithmWhitelist\" : [ HS56, HS384 ],\n" +
                " \"publicKeysMap\" : { \"key1\" : \"test\", \"key2\" : \"test\" }, \n " +
                "  \"sensitivity\" : \"open\"}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"jwtHashAlgorithmWhitelist\":{\"Value Not Allowed\":{\"User Values\":\"[HS56]\",\"Allowed Values\":\"[none, HS256, HS384, HS512, RS256, RS384, RS512, ES256, ES384, ES512, PS256, PS384, PS512]\",\"Expected Data Type\":\"JSONArray\"}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testWrongProjectID(){
        String json = "{ \"projectId\" : \"10test10\" }";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"projectId\":{\"Wrong Input\":{\"User Values\":\"10test10\",\"Error\":\"Invalid Numeric String / Value contains alphabet.\"}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testWrongCloudEnvironment(){
        String json = "{\"cloudEnvironment\" : true}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"cloudEnvironment\":{\"Wrong Data Type\":{\"Defined Data Type\":\"Boolean\",\"Value\":\"true\",\"Expected Data Type\":\"String\"}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testVocsMapping(){
        String json = "{ \"vocsMapping\" : {\n" +
                "    \"Zoe\" : {\n" +
                "      \"name\" : \"Zoe-Mlsc\",\n" +
                "      \"pcm_16_16k\" : {\n" +
                "        \"voice_model\" : \"xpremium-high\",\n" +
                "        \"output_type\" : \"0\"\n" +
                "      }\n" +
                "    }}}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testInvalidVocsMapping(){
        String json = "{ \"vocsMapping\" : [{\n" +
                "    \"Zoe\" : {\n" +
                "      \"name\" : \"Zoe-Mlsc\",\n" +
                "      \"pcm_16_16k\" : {\n" +
                "        \"voice_model\" : \"xpremium-high\",\n" +
                "        \"output_type\" : \"0\"\n" +
                "      }\n" +
                "    }" +
                "  }]" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"vocsMapping\":{\"Schema Error\":{\"Expected Schema\":\"JSONObject\",\"Defined Schema\":\"JSONArray\"}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testInvalidVoiceMapping(){
        String json = "{ \"vocsMapping\" : {\n" +
                "    \"Zoe\" : [{\n" +
                "      \"name\" : \"Zoe-Mlsc\",\n" +
                "      \"pcm_16_16k\" : {\n" +
                "        \"voice_model\" : \"xpremium-high\",\n" +
                "        \"output_type\" : \"0\"\n" +
                "      }\n" +
                "    }]" +
                "  }" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"vocsMapping\":{\"Zoe\":{\"Schema Error\":{\"Expected Schema\":\"JSONObject\",\"Defined Schema\":\"JSONArray\"}}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testInvalidVoiceName(){
        String json = "{ \"vocsMapping\" : {\n" +
                "    \"Zoe\" : {\n" +
                "      \"name\" : true,\n" +
                "      \"pcm_16_16k\" : {\n" +
                "        \"voice_model\" : \"xpremium-high\",\n" +
                "        \"output_type\" : \"0\"\n" +
                "      }\n" +
                "    }" +
                "  }" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"vocsMapping\":{\"Zoe\":{\"name\":{\"Wrong Data Type\":{\"Defined Data Type\":\"Boolean\",\"Value\":\"true\",\"Expected Data Type\":\"String\"}}}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testInvalidVoiceSchema(){
        String json = "{ \"vocsMapping\" : {\n" +
                "    \"Zoe\" : {\n" +
                "      \"name\" : \"Zoe\",\n" +
                "      \"pcm_16_16k\" : {\n" +
                "        \"voice_model\" : \"xpremium-high\",\n" +
                "        \"output_type\" : true\n" +
                "      }\n" +
                "    }" +
                "  }" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"vocsMapping\":{\"Zoe\":{\"pcm_16_16k\":{\"output_type\":{\"Wrong Data Type\":{\"Defined Data Type\":\"Boolean\",\"Value\":\"true\",\"Expected Data Type\":\"String\"}}}}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testUnknownKey(){
        String json = "{ \"vocsMapping\" : {\n" +
                "    \"Zoe\" : {\n" +
                "      \"name\" : \"zoe\",\n" +
                "      \"pcm_16_11k\" : {\n" +
                "        \"voice_model\" : \"xpremium-high\",\n" +
                "        \"output_type\" : \"0\"\n" +
                "      }\n" +
                "    }" +
                "  }" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"vocsMapping\":{\"Zoe\":{\"pcm_16_11k\":{\"Error\":\"Wrong Key\"}}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testInvalidSchema(){
        String json = "{ \"vocsMapping\" : {\n" +
                "    \"Zoe\" : {\n" +
                "      \"name\" : \"zoe\",\n" +
                "       \"tuneable_data\" : {" +
                "           \"test\" : \"test\"" +
                "}" +
                "    }" +
                "  }" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"vocsMapping\":{\"Zoe\":{\"tuneable_data\":{\"Schema Error\":{\"Expected Schema\":\"JSONArray\",\"Defined Schema\":\"JSONObject\"}}}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testPublicKeyMap(){
        String json = "{" +
                "\"publicKeysMap\" : {" +
                "    \"key1\" : true,\n" +
                "    \"key2\" : \"test \"" +
                "}" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"publicKeysMap\":{\"key1\":{\"Wrong Data Type\":{\"Defined Data Type\":\"Boolean\",\"Value\":\"true\",\"Expected Data Type\":\"String\"}}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testPluginList(){
        String json = "{\"activePluginsList\" : [ {\n" +
                "    \"jarName\" : \"webapi-backend-monitoring-plugin-*.*.*-*.jar\",\n" +
                "    \"settings\" : {\n" +
                "      \"backendServicesMap\" : {\n" +
                "        \"krypton\" : \"__HOST__:22\",\n" +
                "        \"http\" : \"__HOST__:443\",\n" +
                "        \"GoodOrBad\" : \"__HOST__:22\"\n" +
                "      },\n" +
                "      \"activeAuthenticationMethods\" : [ \"http_auth\" ],\n" +
                "      \"basicAuthenticationUsername\" : \"user\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"jarName\":\"tts-rest-interface-plugin-*.*.*-*.jar\",\n" +
                "       \"settings\":{\n" +
                "            \"activeAuthenticationMethods\":[\"http_auth\"],\n" +
                "            \"basicAuthenticationUsername\":\"user\"\n" +
                "        }\n" +
                "  }\n" +
                "  ]}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testBlankValue(){
        String json = "{\"type\" : \"\"}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"type\":{\"Error\":\"Invalid Value / Blank String\"}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test (expected = JSONException.class)
    public void testInvalidJson(){
        String json = "{\"test\" : \"test\"," +
                "\"test\" : \"test\"}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"Error\":\"Invalid Json Format : Look for possible Duplicate Key / Wrong Schema\"}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testKryptonLmWeightTest(){
        String json = "{\"kryptonDomainLmWeight\" : true}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"kryptonDomainLmWeight\":{\"Expected Schema\":\"[\\\"String\\\" or \\\"Integer\\\"]\",\"Defined Schema\":\"Boolean\"}}", validator.jsonCompare(new JSONObject(json)).toString());
    }

    @Test
    public void testTtsGenreTest(){
        String json = "{" +
                "\"speechSynthesisTuningBundles\": {\n" +
                "\"sports\": [{\n" +
                "\"uri\": \"file:///usr/local/Nuance/VoCS/ActivePrompts/Samantha/test.rst\",\n" +
                "\"content_type\": \"application/x-vocalizer-rettt+text\"\n" +
                "},{\n" +
                "\"uri\": \"file:///usr/local/Nuance/VoCS/ActivePrompts/Samantha/test.rst\",\n" +
                "\"content_type\": \"application/x-vocalizer-rettt+text\"\n" +
                "}],\n" +
                "\"entertainment\": [{\n" +
                "\"uri\": true,\n" +
                "\"content_type\": \"application/x-vocalizer-rettt+text\"\n" +
                "}]\n" +
                "}" +
                "}";
        json = Encode.forHtmlContent(json);
        validator = new TenantConfigValidator();
        Assert.assertEquals("{\"speechSynthesisTuningBundles\":{\"entertainment\":{\"Error\":{\"uri\":{\"Wrong Data Type\":{\"Defined Data Type\":\"Boolean\",\"Value\":\"true\",\"Expected Data Type\":\"String\"}}}}}}", validator.jsonCompare(new JSONObject(json)).toString());
    }
}
