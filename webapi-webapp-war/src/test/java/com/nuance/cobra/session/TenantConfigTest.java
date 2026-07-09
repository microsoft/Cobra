//package com.nuance.cobra.session;
//
//import org.json.JSONObject;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.assertTrue;
//
//@Deprecated
//public class TenantConfigTest
//{
//
//    private static final Integer DEFAULT_GATEWAY_PORT = 443;
//    private static final String DEFAULT_DICTATION_LANGUAGE = "en-us";
//    private static final ApiVersion DEFAULT_API_VERSION = ApiVersion._1_0;
//    private static final Integer DEFAULT_SESSION_TIMEOUT = 900;
//    private static final String DEFAULT_DIALOG_MODEL_APPLICATION_VERSION = "1.0";
//    private static final String DEFAULT_SERVICE_PARAM_APPLICATION = "NinaCloud";
//    private static final String DEFAULT_SERVICE_PARAM_CARRIER = "notDefined";
//    private static final String DEFAULT_SERVICE_PARAM_PHONE_MODEL = "notDefined";
//    private static final String DEFAULT_SERVICE_PARAM_PHONE_OS = "notDefined";
//    private static final String DEFAULT_SERVICE_PARAM_SCRIPT_VERSION = "notDefined";
//
//    @Before
//    public void setUp()
//            throws Exception
//    {
//
//    }
//
//    @After
//    public void tearDown()
//            throws Exception
//    {
//
//    }
//
//    public static class ConfigJson extends JSONObject
//    {
//        void putProperty(Config.Property property, Object value)
//        {
//            put(property.getKey(), value);
//        }
//
//        void putGatewayHost(Object gatewayHost)
//        {
//            putProperty(Config.Property.NMSP_GATEWAY_HOST, gatewayHost);
//        }
//
//        void putGatewayPort(Object gatewayPort)
//        {
//            putProperty(Config.Property.NMSP_GATEWAY_PORT, gatewayPort);
//        }
//
//        void putNmaidId(Object nmaidId)
//        {
//            putProperty(Config.Property.NMAID_ID, nmaidId);
//        }
//
//        void putNmaidKey(Object nmaidKey)
//        {
//            putProperty(Config.Property.NMAID_KEY, nmaidKey);
//        }
//
//        void putDictationLanguage(Object dictationLanguage)
//        {
//            putProperty(Config.Property.DICTATION_LANGUAGE, dictationLanguage);
//        }
//
//        void putApiVersion(Object apiVersion)
//        {
//            putProperty(Config.Property.API_VERSION, apiVersion);
//        }
//
//        void putSessionTimeoutSeconds(Object sessionTimeoutSeconds)
//        {
//            putProperty(Config.Property.SESSION_TIMEOUT_SECONDS, sessionTimeoutSeconds);
//        }
//
//        void putDialogModelCompanyName(Object companyName)
//        {
//            putProperty(Config.Property.DIALOG_MODEL_COMPANY_NAME, companyName);
//        }
//
//        void putDialogModelApplicationName(Object applicationName)
//        {
//            putProperty(Config.Property.DIALOG_MODEL_APPLICATION_NAME, applicationName);
//        }
//
//        void putDialogModelApplicationVersion(Object applicationVersion)
//        {
//            putProperty(Config.Property.DIALOG_MODEL_APPLICATION_VERSION, applicationVersion);
//        }
//
//        void putServiceParamApplication(Object application)
//        {
//            putProperty(Config.Property.SERVICE_PARAM_APPLICATION, application);
//        }
//
//        void putServiceParamCarrier(Object carrier)
//        {
//            putProperty(Config.Property.CLIENT_CARRIER, carrier);
//        }
//
//        void putServiceParamPhoneModel(Object phoneModel)
//        {
//            putProperty(Config.Property.CLIENT_PHONE_MODEL, phoneModel);
//        }
//
//        void putServiceParamPhoneOs(Object phoneOs)
//        {
//            putProperty(Config.Property.CLIENT_OS_NAME, phoneOs);
//        }
//
//        void putServiceParamScriptVersion(Object scriptVersion)
//        {
//            putProperty(Config.Property.CLIENT_SCRIPT_VERSION, scriptVersion);
//        }
//
//    }
//
//    public static ConfigJson getMinimalJsonObject()
//    {
//        final ConfigJson configJson = new ConfigJson();
//        configJson.putGatewayHost("testGatewayHost");
//        configJson.putNmaidId("testCustomerId");
//        configJson.putNmaidKey("testCustomerKey");
//        configJson.putDialogModelCompanyName("testCompanyName");
//        configJson.putDialogModelApplicationName("testApplicationName");
//
//        return configJson;
//    }
//
//
//    private TenantConfig getMinimalTenantConfig() throws InvalidConfigurationException
//    {
//        final JSONObject minimalJsonObject = getMinimalJsonObject();
//        return TenantConfig.fromJsonObject(minimalJsonObject);
//    }
//
//
//    @Test(expected=NullPointerException.class)
//    public void test_fromJsonObject_null() throws Exception
//    {
//        TenantConfig.fromJsonObject(null);
//    }
//
//    /*
//    @Test(expected=IOException.class)
//    public void test_fromInputStream_IOException() throws Exception
//    {
//        final InputStream mockInputStream = mock(InputStream.class);
//        when(mockInputStream.read()).thenThrow(new IOException());
//        when(mockInputStream.read(any(byte[].class))).thenThrow(new IOException());
//        when(mockInputStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
//
//        TenantConfig.fromInputStream(mockInputStream);
//    }
//    */
//
//    /*
//    @Test(expected=JsonParseException.class)
//    public void test_fromInputStream_notJson() throws Exception
//    {
//        final ByteArrayInputStream bais = new ByteArrayInputStream("not json".getBytes());
//
//        TenantConfig.fromInputStream(bais);
//    }
//    */
//
//    /*
//    @Test(expected=JsonMappingException.class)
//    public void test_fromInputStream_emptyJsonArray() throws Exception
//    {
//        final ByteArrayInputStream bais = new ByteArrayInputStream("[]".getBytes());
//
//        TenantConfig.fromInputStream(bais);
//    }
//    */
//
//    /*
//    @Test(expected=InvalidConfigurationException.class)
//    public void test_fromInputStream_jsonArray() throws Exception
//    {
//        final ByteArrayInputStream bais = new ByteArrayInputStream("[\"nmaidKey\",\"nmaidId\"]".getBytes());
//
//        TenantConfig.fromInputStream(bais);
//    }
//    */
//
//    @Test(expected=MissingRequiredPropertyException.class)
//    public void test_fromJsonObject_emptyJsonObject() throws Exception
//    {
//        final JSONObject jsonObject = new JSONObject();
//
//        TenantConfig.fromJsonObject(jsonObject);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_unknownField() throws Exception
//    {
//        final JSONObject jsonObject = new JSONObject();
//        jsonObject.put("notafield", "someValue");
//
//        TenantConfig.fromJsonObject(jsonObject);
//    }
//
//    private ConfigJson getJsonWithAllFields()
//    {
//        final String nmspGatewayHost = "testGatewayHost";
//        final String nmspGatewayPort = "12345";
//        final String nmaidKey = "testCustomerKey";
//        final String nmaidId = "testCustomerId";
//        final String dictationLanguage = "testDictationLanguage";
//        final String apiVersion = "1.0";
//        final String sessionTimeoutSeconds = "100";
//        final String dialogModelCompanyName = "testCompanyName";
//        final String dialogModelApplicationName = "testApplicationName";
//        final String dialogModelApplicationVersion = "testApplicationVersion";
//        final String serviceParamApplication = "testServiceApplication";
//        final String serviceParamCarrier = "testCarrier";
//        final String serviceParamPhoneModel = "testPhoneModel";
//        final String serviceParamPhoneOs = "testPhoneOs";
//        final String serviceParamScriptVersion = "testScriptVersion";
//
//        final ConfigJson configJson = new ConfigJson();
//        configJson.putNmaidKey(nmaidKey);
//        configJson.putNmaidId(nmaidId);
//        configJson.putGatewayHost(nmspGatewayHost);
//        configJson.putGatewayPort(nmspGatewayPort);
//        configJson.putDictationLanguage(dictationLanguage);
//        configJson.putApiVersion(apiVersion);
//        configJson.putSessionTimeoutSeconds(sessionTimeoutSeconds);
//        configJson.putDialogModelCompanyName(dialogModelCompanyName);
//        configJson.putDialogModelApplicationName(dialogModelApplicationName);
//        configJson.putDialogModelApplicationVersion(dialogModelApplicationVersion);
//        configJson.putServiceParamApplication(serviceParamApplication);
//        configJson.putServiceParamCarrier(serviceParamCarrier);
//        configJson.putServiceParamPhoneModel(serviceParamPhoneModel);
//        configJson.putServiceParamPhoneOs(serviceParamPhoneOs);
//        configJson.putServiceParamScriptVersion(serviceParamScriptVersion);
//
//        return configJson;
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_allFieldsPlusUnknownField() throws Exception
//    {
//        final ConfigJson configJson = getJsonWithAllFields();
//
//        configJson.put("notafield", "someValue");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test
//    public void test_fromJsonObject_all() throws Exception
//    {
//
//        final ConfigJson configJson = getJsonWithAllFields();
//
//        final TenantConfig tenantConfig = TenantConfig.fromJsonObject(configJson);
//
//        assertTrue("testGatewayHost".equals(tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_HOST, String.class)));
//        assertTrue(12345 == tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_PORT, Integer.class));
//        assertTrue("testCustomerKey".equals(tenantConfig.getProperty(Config.Property.NMAID_KEY, String.class)));
//        assertTrue("testCustomerId".equals(tenantConfig.getProperty(Config.Property.NMAID_ID, String.class)));
//        assertTrue("testDictationLanguage".equals(tenantConfig.getProperty(Config.Property.DICTATION_LANGUAGE, String.class)));
//        assertTrue(ApiVersion._1_0.equals(tenantConfig.getProperty(Config.Property.API_VERSION, ApiVersion.class)));
//        assertTrue(100 == tenantConfig.getProperty(Config.Property.SESSION_TIMEOUT_SECONDS, Integer.class));
//        assertTrue("testCompanyName".equals(tenantConfig.getProperty(Config.Property.DIALOG_MODEL_COMPANY_NAME, String.class)));
//        assertTrue("testApplicationName".equals(tenantConfig.getProperty(Config.Property.DIALOG_MODEL_APPLICATION_NAME, String.class)));
//        assertTrue("testApplicationVersion".equals(tenantConfig.getProperty(Config.Property.DIALOG_MODEL_APPLICATION_VERSION, String.class)));
//        assertTrue("testServiceApplication".equals(tenantConfig.getProperty(Config.Property.SERVICE_PARAM_APPLICATION, String.class)));
//        assertTrue("testCarrier".equals(tenantConfig.getProperty(Config.Property.CLIENT_CARRIER, String.class)));
//        assertTrue("testPhoneModel".equals(tenantConfig.getProperty(Config.Property.CLIENT_PHONE_MODEL, String.class)));
//        assertTrue("testPhoneOs".equals(tenantConfig.getProperty(Config.Property.CLIENT_OS_NAME, String.class)));
//        assertTrue("testScriptVersion".equals(tenantConfig.getProperty(Config.Property.CLIENT_SCRIPT_VERSION, String.class)));
//    }
//
//
//    @Test(expected=MissingRequiredPropertyException.class)
//    public void test_fromJsonObject_noNmaidKey() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.remove(Config.Property.NMAID_KEY.getKey());
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullNmaidKey() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.NMAID_KEY.getKey(), JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyNmaidKey() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.NMAID_KEY.getKey(), "");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=MissingRequiredPropertyException.class)
//    public void test_fromJsonObject_noNmaidId() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.remove(Config.Property.NMAID_ID.getKey());
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullNmaidId() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.NMAID_ID.getKey(), JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyNmaidId() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.NMAID_ID.getKey(), "");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=MissingRequiredPropertyException.class)
//    public void test_fromJsonObject_noNmspGatewayHost() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.remove(Config.Property.NMSP_GATEWAY_HOST.getKey());
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullNmspGatewayHost() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.NMSP_GATEWAY_HOST.getKey(), JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyNmspGatewayHost() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.NMSP_GATEWAY_HOST.getKey(), "");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=MissingRequiredPropertyException.class)
//    public void test_fromJsonObject_noDialogModelCompanyName() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.remove(Config.Property.DIALOG_MODEL_COMPANY_NAME.getKey());
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullDialogModelCompanyName() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.DIALOG_MODEL_COMPANY_NAME.getKey(), JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyDialogModelCompanyName() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.DIALOG_MODEL_COMPANY_NAME.getKey(), "");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=MissingRequiredPropertyException.class)
//    public void test_fromJsonObject_noDialogModelApplicationName() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.remove(Config.Property.DIALOG_MODEL_APPLICATION_NAME.getKey());
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullDialogModelApplicationName() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.DIALOG_MODEL_APPLICATION_NAME.getKey(), JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyDialogModelApplicationName() throws Exception
//    {
//        final JSONObject configJson = getMinimalJsonObject();
//        configJson.put(Config.Property.DIALOG_MODEL_APPLICATION_NAME.getKey(), "");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//
//    @Test
//    public void test_fromJsonObject_defaultValues() throws Exception
//    {
//        final TenantConfig tenantConfig = getMinimalTenantConfig();
//
//        assertTrue(DEFAULT_GATEWAY_PORT.equals(tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_PORT)));
//        assertTrue(DEFAULT_DICTATION_LANGUAGE.equals(tenantConfig.getProperty(Config.Property.DICTATION_LANGUAGE)));
//        assertTrue(DEFAULT_API_VERSION.equals(tenantConfig.getProperty(Config.Property.API_VERSION)));
//        assertTrue(DEFAULT_SESSION_TIMEOUT.equals(tenantConfig.getProperty(Config.Property.SESSION_TIMEOUT_SECONDS)));
//        assertTrue(DEFAULT_DIALOG_MODEL_APPLICATION_VERSION.equals(tenantConfig.getProperty(Config.Property.DIALOG_MODEL_APPLICATION_VERSION)));
//        assertTrue(DEFAULT_SERVICE_PARAM_APPLICATION.equals(tenantConfig.getProperty(Config.Property.SERVICE_PARAM_APPLICATION)));
//        assertTrue(DEFAULT_SERVICE_PARAM_CARRIER.equals(tenantConfig.getProperty(Config.Property.CLIENT_CARRIER)));
//        assertTrue(DEFAULT_SERVICE_PARAM_PHONE_MODEL.equals(tenantConfig.getProperty(Config.Property.CLIENT_PHONE_MODEL)));
//        assertTrue(DEFAULT_SERVICE_PARAM_PHONE_OS.equals(tenantConfig.getProperty(Config.Property.CLIENT_OS_NAME)));
//        assertTrue(DEFAULT_SERVICE_PARAM_SCRIPT_VERSION.equals(tenantConfig.getProperty(Config.Property.CLIENT_SCRIPT_VERSION)));
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullNmspGatewayPort() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort(JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyNmspGatewayPort() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort("");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nmspGatewayPortTooLow() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort(-1);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nmspGatewayPortTooHigh() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort(65536);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test
//    public void test_fromJsonObject_nmspGatewayPortMinimum() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort(0);
//
//        final TenantConfig tenantConfig = TenantConfig.fromJsonObject(configJson);
//
//        assertTrue(0 == tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_PORT, Integer.class));
//    }
//
//    @Test
//    public void test_fromJsonObject_nmspGatewayPortMaximum() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort(65535);
//
//        final TenantConfig tenantConfig = TenantConfig.fromJsonObject(configJson);
//
//        assertTrue(65535 == tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_PORT, Integer.class));
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nmspGatewayPortCoercedBad() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort("65536");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test
//    public void test_fromJsonObject_nmspGatewayPortCoercedGood() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putGatewayPort("12345");
//
//        final TenantConfig tenantConfig = TenantConfig.fromJsonObject(configJson);
//
//        assertTrue(12345 == tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_PORT, Integer.class));
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullDictationLanguage() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putDictationLanguage(JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyDictationLanguage() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putDictationLanguage("");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_nullApiVersion() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putApiVersion(JSONObject.NULL);
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_emptyApiVersion() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putApiVersion("");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void test_fromJsonObject_unknownApiVersion() throws Exception
//    {
//        final ConfigJson configJson = getMinimalJsonObject();
//        configJson.putApiVersion("notAnApiVersion");
//
//        TenantConfig.fromJsonObject(configJson);
//    }
//
//
//
//    /*
//    @Test
//    public void test_getNmaidKey() throws Exception
//    {
//        final String nmaidKey = "testCustomerKey";
//
//        final TenantConfig tenantConfig = getMinimalTenantConfig();
//
//        TestUtils.inject(tenantConfig, TenantConfig.class, "nmaidKey", nmaidKey);
//
//        assertTrue(nmaidKey.equals(tenantConfig.getProperty(Config.Property.NMAID_KEY, String.class)));
//    }
//
//    @Test
//    public void test_getNmaidId() throws Exception
//    {
//        final String nmaidId = "testCustomerId";
//
//        final TenantConfig tenantConfig = getMinimalTenantConfig();
//
//        TestUtils.inject(tenantConfig, TenantConfig.class, "nmaidId", nmaidId);
//
//        assertTrue(nmaidId.equals(tenantConfig.getProperty(Config.Property.NMAID_ID, String.class)));
//    }
//
//    @Test
//    public void test_getDictationLanguage() throws Exception
//    {
//        final String dictationLanguage = "testDictationLanguage";
//
//        final TenantConfig tenantConfig = getMinimalTenantConfig();
//
//        TestUtils.inject(tenantConfig, TenantConfig.class, "dictationLanguage", dictationLanguage);
//
//        assertTrue(dictationLanguage.equals(tenantConfig.getProperty(Config.Property.DICTATION_LANGUAGE, String.class)));
//    }
//
//    @Test
//    public void test_getApiVersion() throws Exception
//    {
//        final ApiVersion apiVersion = ApiVersion._1_0;
//
//        final TenantConfig tenantConfig = getMinimalTenantConfig();
//
//        TestUtils.inject(tenantConfig, TenantConfig.class, "apiVersion", apiVersion);
//
//        assertTrue(apiVersion.equals(tenantConfig.getProperty(Config.Property.API_VERSION, ApiVersion.class)));
//    }
//
//    @Test
//    public void test_getNmspGatewayAddress() throws Exception
//    {
//        final String nmspGatewayHost = "testGatewayHost";
//
//        final TenantConfig tenantConfig = getMinimalTenantConfig();
//
//        TestUtils.inject(tenantConfig, TenantConfig.class, "nmspGatewayHost", nmspGatewayHost);
//
//        assertTrue(nmspGatewayHost.equals(tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_HOST, String.class)));
//    }
//
//    @Test
//    public void test_getNmspGatewayPort() throws Exception
//    {
//        final int nmspGatewayPort = 344;
//
//        final TenantConfig tenantConfig = getMinimalTenantConfig();
//
//        TestUtils.inject(tenantConfig, TenantConfig.class, "nmspGatewayPort", nmspGatewayPort);
//
//        assertTrue(nmspGatewayPort == tenantConfig.getProperty(Config.Property.NMSP_GATEWAY_PORT, Integer.class));
//    }
//    */
//
//}
