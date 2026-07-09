package com.nuance.cobra.client.mix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SecretCacheProvider.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*", "org.w3c.*"})
public class MixUtilsTest extends CobraTest
{
    private Logger mockLogger;
    private String sessionId = "12345";

    CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    WebApiJSONConfiguration c = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(cc);

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        String customerName = "customerName";
        c.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "abc123");
        c.setProperty(WebApiConfig.Property.MIX_MAX_CLIENT_DATA_KEYS.key, 1000);
        c.setMapProperty(WebApiConfig.Property.CLIENT_DETAILS.key, Map.of("test", "abc1234"));
        c.setMapProperty(WebApiConfig.Property.REPORTING_TENANT_DATA.key, Map.of("customerName", customerName));

        Session mockSession = mock(Session.class);
        Command mockCommand = mock(Command.class);
        when(mockCommand.getCommandName()).thenReturn("commandName");
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockSession.getStartSessionTimeString()).thenReturn("sessionStartedYesterday");
        when(mockSession.getStepId()).thenReturn(1);
        when(mockSession.getCommandId()).thenReturn("1");
        when(mockSession.makeAudioBlobPath()).thenReturn("fakeAudioBlobPath");

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);
    }

    @Test
    public void nullThrowable()
    {
        mockLogger = mock(Logger.class);
        MixUtils.handleMixThrowable(mockLogger, "", "", null);

        verify(mockLogger, times(1)).error(anyString(), anyString(), anyString());
    }

    @Test
    public void getClientDataMap()
            throws Exception
    {
        Map<String, String> clientDataMap = MixUtils.getClientDataMap(sessionId, cc);
        String json = new ObjectMapper().writeValueAsString(clientDataMap);

        String expected = "{\"test\":\"abc1234\",\"clientDetails\":\"{\\\"test\\\":\\\"abc1234\\\"}\",\"x-nuance-client-id\":\"abc123\",\"x-nuance-request-id\":\"12345-1\",\"x-nuance-dialog-seqid\":\"1\",\"applicationKey\":\"abc123\",\"clientUserId\":\"notDefined\",\"clientSessionId\":\"notDefined\",\"clientDeviceId\":\"notDefined\",\"clientOsName\":\"notDefined\",\"clientOsVersion\":\"notDefined\",\"clientCarrier\":\"notDefined\",\"clientPhoneModel\":\"notDefined\",\"clientSubscriberId\":\"notDefined\",\"webapiSessionId\":\"12345\",\"customerName\":\"customerName\",\"tenant\":\"abc123\",\"command\":\"commandName\",\"sessionStartTime\":\"sessionStartedYesterday\",\"audioBlobPath\":\"fakeAudioBlobPath\"}";
        assertEquals(expected, json);

        //max of -1
        c.setProperty(WebApiConfig.Property.MIX_MAX_CLIENT_DATA_KEYS.key, -1);
        clientDataMap = MixUtils.getClientDataMap(sessionId, cc);
        json = new ObjectMapper().writeValueAsString(clientDataMap);
        assertEquals(expected, json);

        //max of 10
        c.setProperty(WebApiConfig.Property.MIX_MAX_CLIENT_DATA_KEYS.key, 10);
        clientDataMap = MixUtils.getClientDataMap(sessionId, cc);
        json = new ObjectMapper().writeValueAsString(clientDataMap);
        expected = "{\"test\":\"abc1234\",\"clientDetails\":\"{\\\"test\\\":\\\"abc1234\\\"}\",\"x-nuance-client-id\":\"abc123\",\"x-nuance-request-id\":\"12345-1\",\"x-nuance-dialog-seqid\":\"1\",\"applicationKey\":\"abc123\",\"clientUserId\":\"notDefined\",\"clientSessionId\":\"notDefined\",\"clientDeviceId\":\"notDefined\",\"clientOsName\":\"notDefined\"}";
        assertEquals(expected, json);

        //max of 0
        c.setProperty(WebApiConfig.Property.MIX_MAX_CLIENT_DATA_KEYS.key, 0);
        clientDataMap = MixUtils.getClientDataMap(sessionId, cc, Map.of("abc", "123"));
        json = new ObjectMapper().writeValueAsString(clientDataMap);
        expected = "{}";
        assertEquals(expected, json);
    }
    
    @Test
    public void getClientDataMapNoReportingTenantData()
            throws Exception
    {
        String className = this.getClass().getSimpleName();
        String methodName = new Throwable().getStackTrace()[0].getMethodName();
        CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration c = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(cc);
        c.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, className);
        c.setMapProperty(WebApiConfig.Property.CLIENT_DETAILS.key, Map.of("test", methodName));

        String sessionId = "12345";
        Session mockSession = mock(Session.class);
        Command mockCommand = mock(Command.class);
        when(mockCommand.getCommandName()).thenReturn("commandName");
        when(mockSession.getCommand()).thenReturn(mockCommand);
        when(mockSession.getStartSessionTimeString()).thenReturn("sessionStartedYesterday");
        when(mockSession.getStepId()).thenReturn(1);
        when(mockSession.getCommandId()).thenReturn("1");
        when(mockSession.makeAudioBlobPath()).thenReturn("fakeAudioBlobPath");

        when(mockSessionManager.getSession(eq(sessionId), anyBoolean())).thenReturn(mockSession);
        Map<String, String> clientDataMap = MixUtils.getClientDataMap(sessionId, cc);
        String json = new ObjectMapper().writeValueAsString(clientDataMap);

        String expected = "{\"test\":\"getClientDataMapNoReportingTenantData\",\"clientDetails\":\"{\\\"test\\\":\\\"getClientDataMapNoReportingTenantData\\\"}\",\"x-nuance-client-id\":\"MixUtilsTest\",\"x-nuance-request-id\":\"12345-1\",\"x-nuance-dialog-seqid\":\"1\",\"applicationKey\":\"MixUtilsTest\",\"clientUserId\":\"notDefined\",\"clientSessionId\":\"notDefined\",\"clientDeviceId\":\"notDefined\",\"clientOsName\":\"notDefined\",\"clientOsVersion\":\"notDefined\",\"clientCarrier\":\"notDefined\",\"clientPhoneModel\":\"notDefined\",\"clientSubscriberId\":\"notDefined\",\"webapiSessionId\":\"12345\",\"tenant\":\"MixUtilsTest\",\"command\":\"commandName\",\"sessionStartTime\":\"sessionStartedYesterday\",\"audioBlobPath\":\"fakeAudioBlobPath\"}";
        assertEquals(expected, json);
    }

    @Test
    public void checkIfRateLimited()
    {
        Map grpcHeaders = new HashMap();
        Logger mocklogger = mock(Logger.class);

        MixUtils.checkIfRateLimited(grpcHeaders, mocklogger);
        verify(mocklogger, times(0)).error(anyString());

        grpcHeaders.put("x-envoy-ratelimited", true);

        MixUtils.checkIfRateLimited(grpcHeaders, mocklogger);
        verify(mocklogger, times(1)).error(anyString());
    }

    @Test
    public void clientDataCharLimit()
    {
        String key = "1970sLongString";
        String newsString = "The sport's national governing body is facing intense public and political scrutiny over its culture problems and how it settled a $3.5 million lawsuit";
        Map<String, String> additionalItems = Map.of(key, newsString);
        Map<String, String> clientDataMap = MixUtils.getClientDataMap(sessionId, cc, additionalItems);
        assertEquals(newsString.substring(0, 127), clientDataMap.get(key));

        c.setProperty(WebApiConfig.Property.MIX_MAX_CLIENT_DATA_STRING_LENGTH.key, 0);
        clientDataMap = MixUtils.getClientDataMap(sessionId, cc, additionalItems);
        assertEquals(null, clientDataMap.get(key));
    }

    @Test
    public void mixClientSecretsFromKeyVault()
    {
        c.setProperty(WebApiConfig.Property.KEY_VAULT_TENANT_SECRET_NAME.key, "keyVaultTenantSecretNameTest");
        c.setProperty(WebApiConfig.Property.KEY_VAULT_VAULT_URI.key, "keyVaultUriTest");
        c.setProperty(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, "keyVaultClientIdTest");

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode mainNode = factory.objectNode();
        ArrayNode arrayNode = factory.arrayNode();
        arrayNode.add("riAbk888CC2B.97D7eUklVe6pD");
        mainNode.put("mixClientSecrets", arrayNode);

        // Mock SecretCacheProvider and stub the methods
        SecretCacheProvider providerMock = mock(SecretCacheProvider.class);
        when(providerMock.getSecretFromKeyVault(anyString(), any(CombinedConfiguration.class))).thenReturn(mainNode);

        // Mock the static method SecretCacheProvider.getInstance()
        PowerMockito.mockStatic(SecretCacheProvider.class);
        when(SecretCacheProvider.getInstance()).thenReturn(providerMock);

        MixClientCredentials credentials = MixUtils.mixClientCredentialsFromConfig(cc);
        assertEquals(1, credentials.getMixClientSecrets().size());
    }
}