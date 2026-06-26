package com.nuance.cobra.session;


import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.command.CommandFactory;
import com.nuance.cobra.command.StartSessionCommand;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("StringEquality")
@RunWith(PowerMockRunner.class)
@PrepareForTest(SessionManager.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*", "javax.management.*","org.w3c.dom.*"})
public class SessionTest extends CobraTest
{
    private final CombinedConfiguration mockSessionConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private final Configuration mockSessionConfig = mockSessionConfigs.getConfiguration(0);
    String requestString = "{ \"type\": \"command\",  \"command\": { \"name\": \"" + "startSession" +
            "\", \"id\": \"1\",    \"parameters\": { \"applicationKey\":\"cmapplicationKey\" , \"verificationCode\":\"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\" } }}";

    @Before
    public void setUp()
            throws Exception
    {
        super.setUp();
    }

    @After
    public void cleanUp()
            throws Exception
    {
        super.tearDown();
    }

    @Test
    public void test_constructor_nullSessionId() throws Exception
    {
        final String sessionId = null;
        final Session session = new Session(sessionId, mockSessionConfigs);
        assertNull(session.getCommand());

        assertTrue(sessionId == session.getSessionId());
    }

    @Test
    public void testStepId(){
        int id = 1001;
        String str = "test";
        Session s = new Session(str, mockSessionConfigs);
        s.setStepId(id);
        assertTrue(id == s.getStepId());

        s.incrementStepId();
        assertEquals(1002, s.getStepId());
    }

    @Test
    public void test_constructor_emptySessionId() throws Exception
    {
        final String sessionId = "";
        final Session session = new Session(sessionId, mockSessionConfigs);
        assertNull(session.getCommand());

        assertTrue(sessionId == session.getSessionId());
    }

    @Test
    public void test_constructor() throws Exception
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);
        assertNull(session.getCommand());

        assertTrue(sessionId == session.getSessionId());
    }

    @Test
    public void test_getProperty_null_null() throws Exception
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        final Object o = session.getProperty(null);
        assertNull(o);
    }


    @Test
    public void test_getProperty_apiConnectionCloser_null() throws Exception
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        final Object o = session.getProperty(Session.SessionPropertyKeys.APICONNECTIONCLOSER);
        assertNull(o);
    }

    @Test
    public void test_getProperty_apiConnectionCloser_notNull() throws Exception
    {
        final String property = "oneProperty";

        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        final Map<Session.SessionPropertyKeys, Object> propertyMap = session.getPropertyMap();
        propertyMap.put(Session.SessionPropertyKeys.APICONNECTIONCLOSER, property);

        final Object o = session.getProperty(Session.SessionPropertyKeys.APICONNECTIONCLOSER);
        assertTrue(property == o);
    }

    @Test(expected=NullPointerException.class)
    public void test_setProperty_null() throws Exception
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        final String property = "oneProperty";
        session.setProperty(null, property);
    }

    @Test
    public void test_setProperty_apiConnectionCloser_null() throws Exception
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        session.setProperty(Session.SessionPropertyKeys.APICONNECTIONCLOSER, null);

        final Map<Session.SessionPropertyKeys, Object> propertyMap = session.getPropertyMap();
        assertTrue(3 == propertyMap.size()); //SESSIONLESS
        assertTrue(null == propertyMap.get(Session.SessionPropertyKeys.APICONNECTIONCLOSER));
    }

    @Test
    public void test_setProperty_apiConnectionCloser_notNull() throws Exception
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        final String property = "oneProperty";
        session.setProperty(Session.SessionPropertyKeys.APICONNECTIONCLOSER, property);

        final Map<Session.SessionPropertyKeys, Object> propertyMap = session.getPropertyMap();
        assertTrue(3 == propertyMap.size());
        assertTrue(property == propertyMap.get(Session.SessionPropertyKeys.APICONNECTIONCLOSER));
    }



    // Test SessionPropertyKeys valueOf

    @Test(expected=NullPointerException.class)
    public void test_sessionPropertyKeys_valueOf_null() throws Exception
    {
        Session.SessionPropertyKeys.valueOf(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_sessionPropertyKeys_valueOf_empty() throws Exception
    {
        Session.SessionPropertyKeys.valueOf("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_sessionPropertyKeys_valueOf_invalid() throws Exception
    {
        Session.SessionPropertyKeys.valueOf("this is not a valid key value");
    }

    @Test
    public void test_sessionPropertyKeys_valueOf_apiConnectionCloser() throws Exception
    {
        final Session.SessionPropertyKeys key = Session.SessionPropertyKeys.valueOf("APICONNECTIONCLOSER");
        assertTrue(Session.SessionPropertyKeys.APICONNECTIONCLOSER == key);
    }

    @Test
    public void test_isExpired_validSession()
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);
        mockSessionConfig.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key, 10000);
        mockSessionConfig.setProperty(WebApiConfig.Property.SESSION_INACTIVITY_TIMEOUT_SECONDS.key, 10000);
        Calendar lastActivity = Calendar.getInstance();
        lastActivity.add(Calendar.SECOND, -1000);
        session.setLastActivityTime(lastActivity);
        assertFalse(session.isExpired());
    }

    @Test
    public void test_isExpired_expiredSession()
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);
        mockSessionConfig.setProperty(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key, 10);
        mockSessionConfig.setProperty(WebApiConfig.Property.SESSION_INACTIVITY_TIMEOUT_SECONDS.key, 10);
        Calendar lastActivity = Calendar.getInstance();
        lastActivity.add(Calendar.SECOND, -1000);
        session.setLastActivityTime(lastActivity);
        assertTrue(session.isExpired());
    }

    @Test
    public void test_getConfigForRequest() throws Exception
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        CombinedConfiguration commandConfig = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Request request = Request.fromJson(requestString);
        Command command = new StartSessionCommand(request, commandConfig, null);
        session.setCommand(command);
        CombinedConfiguration resultantConfig = session.getConfigForRequest();
        assertEquals("cmapplicationKey", resultantConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key));
    }

    @Test
    public void test_getConfigWithMerge() throws Exception
    {
        final String sessionId = "testSessionId";
        CombinedConfiguration cc = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration c = cc.getConfiguration(0);
        c.setProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key, "coverCopyConstructorIteration");
        final Session session = new Session(sessionId, cc);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(WebApiConfig.Property.APPLICATION_KEY.key, "cmapplicationKey2");
        CombinedConfiguration resultantConfig = session.getConfigWithMerge(parameters);
        assertEquals("cmapplicationKey2", resultantConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key));
    }

    @Test
    public void test_getCommandName() throws CobraException {
        Session testSession = new Session("testId", mockSessionConfigs);
        assertNotNull(testSession.getCommandName());
        assertNotNull(testSession.getCommandId());
        assertNull(testSession.getResponseHandler());

        testSession.setCommand(CommandFactory.createCommand(Request.fromJson(requestString), mock(IResponseSender.class), mockSessionConfigs));
        testSession.getCommandName();
        testSession.getResponseHandler();
        assertNotNull(testSession.getCommand());

        assertNotNull(testSession.getCommandId());
    }

    @Test
    public void test_isSessionUsingStubBackend(){
        mockSessionConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, true);
        Session testSession = new Session("testId", mockSessionConfigs);
        PowerMockito.mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);
        when(mockSessionManager.getSession("testId", false)).thenReturn(testSession);

        assertTrue(Session.isSessionUsingStubBackend("testId"));

        mockSessionConfig.setProperty(WebApiConfig.Property.STUB_BACKEND.key, false);
        testSession = new Session(("testId"), mockSessionConfigs);

        assertFalse(Session.isSessionUsingStubBackend("testId"));
    }

    @Test
    public void test_getTenantKey(){
        mockSessionConfig.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "some_key");
        Session testSession = new Session("testId", mockSessionConfigs);

        assertEquals("some_key", testSession.getTenantKey());
        assertEquals("some_key", Session.getTenantKey(testSession));

        CombinedConfiguration mockConfig = mock(CombinedConfiguration.class);
        testSession = new Session("testId2", mockConfig);
        when(mockConfig.getString(anyString())).thenThrow(new IllegalStateException());
        assertEquals(null, testSession.getTenantKey());
    }

    @Test
    public void test_RetrieveActiveDynamicVocabularies(){
        SessionManager mockSessionManager = mock(SessionManager.class);
        PowerMockito.mockStatic(SessionManager.class);
        PowerMockito.when(SessionManager.getInstance()).thenReturn(mockSessionManager);

        ArrayList<Object> testAppVocabularies = new ArrayList<>();
        testAppVocabularies.add(new DynamicVocabulary("testDynamicVoc", DynamicVocabulary.VocabularyType.APPLICATION_NUANCE_DOMAINLM, new HashMap<>()));

        ArrayList<Object> testActiveAppVocabularies = new ArrayList<>();
        testActiveAppVocabularies.add(testAppVocabularies.get(0));

        mockSessionConfig.setProperty(WebApiConfig.Property.ACTIVE_DYNAMIC_VOCABULARY.key, testActiveAppVocabularies);
        mockSessionConfig.setProperty(WebApiConfig.Property.APPLICATION_VOCABULARIES.key, testAppVocabularies);

        Session testSession = new Session("testId", mockSessionConfigs);
        when(mockSessionManager.getSession("testId", false)).thenReturn(testSession);
        testSession.setProperty(Session.SessionPropertyKeys.DYNAMICVOCABULARIES, null);
        assertNotNull(testSession.retrieveActiveDynamicVocabularies(mockSessionConfigs));

        testAppVocabularies.remove(0);
        testSession.setProperty(Session.SessionPropertyKeys.DYNAMICVOCABULARIES, testAppVocabularies);
        mockSessionConfig.setProperty(WebApiConfig.Property.ACTIVE_DYNAMIC_VOCABULARY.key, testAppVocabularies);
        assertNotNull(testSession.retrieveActiveDynamicVocabularies(mockSessionConfigs));
    }

    @Test
    public void cancelActiveCommand()
            throws CobraException
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        session.cancelActiveCommand("tests");

        Command mockCommand = mock(Command.class);
        ResponseHandler mockResponseHandler = mock(ResponseHandler.class);
        when(mockCommand.getResponseHandler()).thenReturn(mockResponseHandler);

        session.setCommand(mockCommand);
        session.cancelActiveCommand("tests");
        verify(mockCommand).cancel("tests");
    }

    @Test
    public void endSessionTime()
    {
        final String sessionId = "testSessionId";
        final Session session = new Session(sessionId, mockSessionConfigs);

        assertFalse(session.isSessionEnded());

        session.setEndSessionTime(Calendar.getInstance());
        session.setEndSessionTime(Calendar.getInstance());

        assertTrue(session.isSessionEnded());
    }
}
