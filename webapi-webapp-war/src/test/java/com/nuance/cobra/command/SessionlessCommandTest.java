package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.client.VocalizerClients;
import com.nuance.cobra.session.*;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({WebApiConfigManager.class, SessionManager.class})
public class SessionlessCommandTest extends CobraTest {
    private static final String PASSWORD = "U2FsdGVkX1+kgJjdYLiqgD6MtKpgzb/uvp6QNG3Vv6k="; //gitleaks:allow

    private String commandJson = "{\"type\":\"command\",\"command\":{\"name\":\"speechSynthesis\",\"id\":\"2\",\"parameters\":{\"data\":\"123\",\"type\":\"text\",\"voice\":\"Carol\",\"language\":\"en-us\",\"sessionParameters\":{\"applicationKey\":\"companyName_appName\",\"verificationCode\":\"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\",\"speechSynthesisCodec\":\"pcm_16_16k\",\"speechRecognitionCodec\":\"pcm_16_16k\"}}}}\n"; //gitleaks:allow
    private Session mockSession;
    private File f;
    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration testConf = testConfs.getConfiguration(0);
    private Request request;
    private SpeechSynthesisCommand speechSynthesisCommand;
    public static String path = "companyName_appName.json";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {

        super.setUp();


        request = Request.fromJson(commandJson);
        request.tenantKey = "companyName_appName";
        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.mockStatic(SessionManager.class);
        // Setup mock SessionManager
        mockSession = mock(Session.class);
        testConf.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key,"123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key,"DV_123");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key,"Linux");
        testConf.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key,"4.14");
        testConf.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key,"as23213df");
        testConf.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_VERSION.key,"1");
        testConf.setProperty(WebApiConfig.Property.PUBLIC_KEY.key, "123456789");
        testConf.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nim.getValue());
        testConf.setProperty(WebApiConfig.Property.STUB_BACKEND.key ,false);
        testConf.setProperty(WebApiConfig.Property.NMAID_KEY.key, "b7635accb9e5f37de5c9d9cd7578b6a05015720c981c6e0104f720d400bde56cfd116e4aabeee418e5ab53116ac034c97b409c0579fea300a2ec5264e7796b0b" ); //gitleaks:allow
        testConf.setProperty(WebApiConfig.Property.COMMAND_DATA.key, "123");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_HOST_ADDRESS.key, "127.0.0.1");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_HOST_PORT.key, 8162);
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_PROTOCOL.key,"V3");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_USERNAME.key, "xyz");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_PASSWORD.key, PASSWORD);
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_SECURITY_ENCRYPTION_PROTOCOL.key, SnmpResponseTrapSender.TrapSecurityEncryptionAlgorithm.usmAesCfb128Protocol.toString());
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_PRIVACY.key,PASSWORD);
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_CONTEXT_NAME.key, "context");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_CONTEXT_ENGINE_OID.key, "9876543210");//hex
        testConf.setProperty(WebApiConfig.Property.SNMP_LOCAL_ENGINE_OID.key, "1234567890");//hex
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_SEND_RETRIES.key, 1);
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_SEND_TIMEOUT.key, 11500);
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_TRAP_OID.key, "1.3.6.1.4.1.3621.31415");
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAPS_PER_MINUTE.key, -1);
        PowerMockito.when(WebApiConfigManager.getInstance()).thenReturn(testConfs);
        when(mockSessionManager.getSession(anyString(), anyBoolean())).thenReturn(mockSession);
        mockServerConfig.setProperty(WebApiConfig.Property.VOCALIZER.key, VocalizerClients.nim.getValue());
        when(mockSession.getSessionConfig()).thenReturn(testConfs);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant(any())).thenReturn(testConfs);

    }

    @Test
    public void test_happyPath(){
        org.json.JSONObject j = new org.json.JSONObject(commandJson);
        Map<String,Object> o = j.getJSONObject("command").getJSONObject("parameters").getJSONObject("sessionParameters").toMap();
        Set<String> i = o.keySet();

        for (String os : i)
        {
            testConf.setProperty(os, o.get(os));

        }
        SessionValidator validator = new SessionValidator(testConfs);

        assertTrue(validator.validate());
    }

    @Test
    public void test_badParameters() throws Exception {
        String newJson = "{\"type\":\"command\",\"command\":{\"name\":\"speechSynthesis\",\"id\":\"2\",\"parameters\":{\"data\":\"123\",\"type\":\"text\",\"voice\":\"Carol\",\"language\":\"en-us\",\"sessionParameters\":{\"applicationKey\":\"companyName_appName\",\"verificationCode\":\"xxx\",\"speechSynthesisCodec\":\"pcm_16_16k\",\"speechRecognitionCodec\":\"pcm_16_16k\"}}}}\n"; //gitleaks:allow
        org.json.JSONObject j = new org.json.JSONObject(newJson);
        Map<String,Object> o = j.getJSONObject("command").getJSONObject("parameters").getJSONObject("sessionParameters").toMap();
        Set<String> i = o.keySet();

        for (String os : i)
        {
            testConf.setProperty(os, o.get(os));

        }
        SessionValidator validator = new SessionValidator(testConfs);

        boolean c = validator.validate();

        assert(!c);


    }

}
