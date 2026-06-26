package com.nuance.cobra.session;


import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.test.TestConstants;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.WebAPIRateLimiter;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PrepareForTest({ CombinedConfiguration.class, WebAPIRateLimiter.class, WebApiConfigManager.class})
public class SessionManagerTest
{
    private static final String SERVER_CONFIG_CLASSNAME =
            "com.nuance.cobra.session.ServerConfig$ServerConfigHolder";

    private Class<?> sessionManagerHolderClass;
    private SessionManager mockSessionManager;
    private SessionManager originalSessionManager;
    private Map<String, Session> sessionMap;

    @Mock
    private WebAPIRateLimiter mockSessionRateLimiter;

    private CombinedConfiguration serverConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    private Configuration serverConfig = CobraConfigurationUtils.getHighestConfig(serverConfigs);

    @Before
    public void setUp()
            throws Exception
    {
//        originalServerConfig = TestUtils.inject(serverConfigHolderClass, "instance", mockServerConfigs);
        serverConfig.setProperty(WebApiConfig.Property.SESSION_CLEAN_UP_SLEEP_TIME_MILLISECONDS.key, 60000);
        serverConfig.setProperty(WebApiConfig.Property.TENANT_RATE_LIMIT_CONCURRENT_SESSIONS.key, -1);

        sessionManagerHolderClass = Class.forName(TestConstants.SESSION_MANAGER_HOLDER_CLASSNAME);

        originalSessionManager = SessionManager.getInstance();

        sessionMap = originalSessionManager.getSessionMap();
        sessionMap.clear();

        mockSessionManager = mock(SessionManager.class);

        Whitebox.setInternalState(WebApiConfigManager.class, "webApiConfig", serverConfigs);
        
        TestUtils.inject(originalSessionManager, SessionManager.class, "sessionRateLimiter", mockSessionRateLimiter);
    }

    @After
    public void tearDown()
            throws Exception
    {
        TestUtils.inject(sessionManagerHolderClass, "instance", originalSessionManager);

        sessionMap.clear();

    }


    @Test
    public void test_getInstance()
            throws Exception
    {
        TestUtils.inject(sessionManagerHolderClass, "instance", mockSessionManager);
        assertTrue(SessionManager.getInstance() == mockSessionManager);
    }


    private static final String UUID_PATTERN = "(?i:[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12})";

    @Test
    public void test_startSession()
            throws Exception
    {
        final SessionManager sessionManager = SessionManager.getInstance();

        Map requestParameters = new HashMap<String,Object>();
        requestParameters.put(WebApiConfig.Property.APPLICATION_KEY.key,"mock1234");
        when(mockSessionRateLimiter.checkIfRateLimitExceeded(anyString(),anyInt())).thenReturn(false);
        final Session session = sessionManager.startSession(requestParameters, serverConfigs);
        assertNotNull(session);

        //assertTrue(sessionId.matches(UUID_PATTERN));

        assertTrue(sessionMap.containsKey(session.getSessionId()));
        assertTrue(session == sessionMap.get(session.getSessionId()));
    }

    @Test
    public void test_startSessionNLPS()
            throws Exception
    {
        final SessionManager sessionManager = SessionManager.getInstance();

        PowerMockito.mockStatic(WebApiConfigManager.class);
        PowerMockito.when(WebApiConfigManager.getCombinedConfigForTenant("testApp_testAppKey")).thenReturn(serverConfigs);

        Map requestParameters = new HashMap<String,Object>();
        requestParameters.put(WebApiConfig.Property.APPLICATION_KEY.key,"mockAppKey");
        when(mockSessionRateLimiter.checkIfRateLimitExceeded(anyString(),anyInt())).thenReturn(false);

        //NLPS not allowed
        try {
            Session session = sessionManager.startSessionNlps(requestParameters, "testTenant", "testApp", "testAppKey");
            fail("Should have gotten an NLPS not allowed exception");
        } catch (InvalidConfigurationException e) {
            //success
        }

        serverConfigs.removeConfiguration(WebApiConfigManager.CONFIG_NAME_START_SESSION_PARAMS);

        serverConfig.setProperty(WebApiConfig.Property.NLPS_ENABLED.key, true);
        Session session = sessionManager.startSessionNlps(requestParameters, "testTenant", "testApp", "testAppKey");
        assertNotNull(session);

        assertTrue(sessionMap.containsKey(session.getSessionId()));
        assertTrue(session == sessionMap.get(session.getSessionId()));
    }

    @Test(expected = NullPointerException.class)
    public void test_endSession_null() throws Exception
    {
        final SessionManager sessionManager = SessionManager.getInstance();

        //now using a concurrentHashMap, null keys not permitted, throwing NullPointerException
        final boolean result = sessionManager.endSession(null);
        fail("No exception thrown");
    }

    @Test
    public void test_endSession_true() throws Exception
    {
        final String myKey = "myKey";
        CombinedConfiguration mockServerConfigs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration mockServerConfig = mockServerConfigs.getConfiguration(0);
        mockServerConfig.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, "mock1234");
        final Session mockSession = mock(Session.class);
        when(mockSession.getTenantKey()).thenReturn("test");
        when(mockSession.getSessionConfig()).thenReturn(mockServerConfigs);

        sessionMap.put(myKey, mockSession);

        final SessionManager sessionManager = SessionManager.getInstance();

        final boolean result = sessionManager.endSession(myKey);
        assertTrue(result);

        assertTrue(0 == sessionMap.size());
    }

    @Test
    public void test_endSession_false_empty() throws Exception
    {
        final String myKey = "myKey";

        final SessionManager sessionManager = SessionManager.getInstance();

        final boolean result = sessionManager.endSession(myKey);
        assertFalse(result);

        assertTrue(0 == sessionMap.size());
    }

    @Test
    public void test_endSession_false_notEmpty() throws Exception
    {
        final String myKey = "myKey";
        final Session mockSession = mock(Session.class);
        when(mockSession.getTenantKey()).thenReturn("test");
        sessionMap.put(myKey, mockSession);

        final SessionManager sessionManager = SessionManager.getInstance();

        final boolean result = sessionManager.endSession("anotherKey");
        assertFalse(result);

        assertTrue(1 == sessionMap.size());
    }

    @Test
    @Ignore
    //TODO: Will need to be updated for Sessionless
    public void test_getSession_null_without() throws Exception
    {
        final String myKey = null;

        final SessionManager sessionManager = SessionManager.getInstance();
        final Session result = sessionManager.getSession(myKey, false);
        assertNull(result);
    }

    @Test (expected = NullPointerException.class)
    public void test_getSession_null_with() throws Exception
    {
        final String myKey = null;

        final Session mockSession = mock(Session.class);
        //now using a concurrentHashMap, null keys not permitted, throwing NullPointerException
        sessionMap.put(myKey, mockSession);
        fail("No exception thrown");
    }


    @Test
    public void test_getSession_empty_without()
    {
        final String myKey = "";

        final SessionManager sessionManager = SessionManager.getInstance();
        final Session result = sessionManager.getSession(myKey, false);
        assertNull(result);
    }

    @Test
    public void test_getSession_empty_with() throws Exception
    {
        final String myKey = "";

        final Session mockSession = mock(Session.class);
        when(mockSession.getTenantKey()).thenReturn("test");
        sessionMap.put(myKey, mockSession);

        final SessionManager sessionManager = SessionManager.getInstance();
        final Session result = sessionManager.getSession(myKey, false);

        assertTrue(mockSession == result);
    }

    @Test
    public void test_getSession_without() throws Exception
    {
        final String myKey = "testId";

        final SessionManager sessionManager = SessionManager.getInstance();
        final Session result = sessionManager.getSession(myKey, false);
        assertNull(result);
    }

    @Test
    public void test_getSession_with() throws Exception
    {
        final String myKey = "testId";

        final Session mockSession = mock(Session.class);
        when(mockSession.getTenantKey()).thenReturn("test");
        sessionMap.put(myKey, mockSession);

        final SessionManager sessionManager = SessionManager.getInstance();
        final Session result = sessionManager.getSession(myKey, false);

        assertTrue(mockSession == result);
    }

    @Test
    public void test_Holder_constructor() throws Exception
    {
        final Constructor<?> constructor = sessionManagerHolderClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void test_getSession_expired() throws Exception
    {
        final String myKey = "testId";

        final Session mockSession = mock(Session.class);
        when(mockSession.getTenantKey()).thenReturn("test");
        sessionMap.put(myKey, mockSession);

        when(mockSession.isExpired()).thenReturn(true);
        final SessionManager sessionManager = SessionManager.getInstance();
        Session result = sessionManager.getSession(myKey, true);
        assertNull(result);
    }

    @Test(expected = CobraException.class)
    public void test_globalRateLimit()
            throws Exception
    {
        final String sessionId = "testSessionId";

        final Session mockSession = mock(Session.class);
        when(mockSession.getSessionId()).thenReturn(sessionId);

        final SessionManager sessionManager = SessionManager.getInstance();

        Map requestParameters = new HashMap<String, Object>();
        requestParameters.put(WebApiConfig.Property.APPLICATION_KEY.key, "mock1234");
        WebAPIRateLimiter rateLimiter = new WebAPIRateLimiter(2);
        TestUtils.inject(sessionManager, SessionManager.class, "sessionRateLimiter", rateLimiter);
        try
        {

            Session session = sessionManager.startSession(requestParameters, serverConfigs);
            serverConfigs.removeConfiguration(WebApiConfigManager.CONFIG_NAME_START_SESSION_PARAMS);
            Session session1 = sessionManager.startSession(requestParameters, serverConfigs);
            serverConfigs.removeConfiguration(WebApiConfigManager.CONFIG_NAME_START_SESSION_PARAMS);
            Session session2 = sessionManager.startSession(requestParameters, serverConfigs);

        }
        catch (CobraException ce)
        {
            assertEquals(ce.getResult()
                           .getErrors()
                           .get(0)
                           .getMessage(),
                    ExternalResponseErrorCode.GLOBAL_RATE_LIMIT_EXCEEDED_FOR_CONCURRENT_SESSIONS.getMessage());
            throw ce;
        }
    }
    @Test(expected = CobraException.class)
    public void test_tenantRateLimit()
            throws Exception
    {
        final String sessionId = "testSessionId";

        final Session mockSession = mock(Session.class);
        when(mockSession.getSessionId()).thenReturn(sessionId);

        final SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.init();
        Map requestParameters = new HashMap<String, Object>();
        requestParameters.put(WebApiConfig.Property.APPLICATION_KEY.key, "mock1234");
        serverConfig.setProperty(WebApiConfig.Property.TENANT_RATE_LIMIT_CONCURRENT_SESSIONS.key, 1);
        serverConfig.setProperty(WebApiConfig.Property.GLOBAL_RATE_LIMIT_CONCURRENT_SESSIONS.key, 1);
        try{
            Session session1 = sessionManager.startSession(requestParameters, serverConfigs);
            serverConfigs.removeConfiguration(WebApiConfigManager.CONFIG_NAME_START_SESSION_PARAMS);
            Session session2 = sessionManager.startSession(requestParameters, serverConfigs);
            serverConfigs.removeConfiguration(WebApiConfigManager.CONFIG_NAME_START_SESSION_PARAMS);
            Session session3 = sessionManager.startSession(requestParameters, serverConfigs);
        }
        catch (CobraException ce)
        {
            assertEquals(ce.getResult()
                           .getErrors()
                           .get(0)
                           .getMessage(),
                    ExternalResponseErrorCode.TENANT_RATE_LIMIT_EXCEEDED_FOR_CONCURRENT_SESSIONS.getMessage());
            throw ce;
        }
    }

    @Test
    public void getSessionFromSocketSession() {
        javax.websocket.Session session = mock(javax.websocket.Session.class);
        assertNull(SessionManager.getInstance().getSession(session));
    }
}
