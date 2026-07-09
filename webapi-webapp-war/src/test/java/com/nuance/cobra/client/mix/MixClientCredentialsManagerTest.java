package com.nuance.cobra.client.mix;

import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.utils.CobraException;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.*"})
@PrepareForTest({MixClientCredentials.class})
public class MixClientCredentialsManagerTest extends CobraTest
{
    private MixClientCredentials mixClientCredentials;

    private static final String connectionTestServerContext = "/callAuthServiceTest";
    private static HttpServer connectionTestServer;
    private static String connectionTestServerUri;

    private static final String connectionTestServer401Context = "/callAuthServiceTest401?a=b";
    private static HttpServer connectionTestServer401;
    private static String connectionTestServer401Uri;

    private static final String connectionTestServerEmptyContext = "/callAuthServiceEmptyTest?authUri=abc123";
    private static HttpServer connectionTestServerEmpty;
    private static String connectionTestServerEmptyUri;

    private static final String connectionTestServer429Context = "/callAuthServiceTest429";
    private static HttpServer connectionTestServer429;
    private static String connectionTestServer429Uri;


    private static Thread tcpServerThread;
    private static ServerSocket serverSocket;
    private static String serverUri;

    @BeforeClass
    public static void setUpClass()
            throws Exception
    {
        startHttpTestServer();

        startHttp401Server();

        startHttpEmptyTokenServer();

        startHttp429TokenServer();

        startTcpServer();

        MixClientCredentialsManager.init();
    }

    private static void startHttpTestServer()
            throws IOException
    {
        String tokenResponse =
                "{\"access_token\":\"Bqwqr8PWlQI9lnAiLz4LZD75xuDX_OUMi-tmk_2q7cK_tiJ6tlJ-wzHVAynIU6EVQ\",\"expires_in\":899,\"scope\":\"asr nlu tts dlg\",\"token_type\":\"bearer\"}";
        connectionTestServer = HttpServer.create(new InetSocketAddress(0), 0);
        connectionTestServer.setExecutor(null); // creates a default executor
        connectionTestServer.start();
        connectionTestServer.createContext(connectionTestServerContext, httpExchange -> {
            httpExchange.sendResponseHeaders(200, tokenResponse.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(tokenResponse.getBytes(StandardCharsets.UTF_8));
            os.close();
        });
        connectionTestServerUri = "http://127.0.0.1:" + connectionTestServer.getAddress().getPort() + connectionTestServerContext;
    }

    private static void startHttp401Server()
            throws IOException
    {
        connectionTestServer401 = HttpServer.create(new InetSocketAddress(0), 0);
        connectionTestServer401.setExecutor(null); // creates a default executor
        connectionTestServer401.start();
        connectionTestServer401.createContext(connectionTestServer401Context, httpExchange -> {
            String notAuth = "Not Authorized";
            httpExchange.sendResponseHeaders(401, notAuth.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(notAuth.getBytes(StandardCharsets.UTF_8));
            os.close();
        });
        connectionTestServer401Uri = "http://127.0.0.1:" + connectionTestServer401.getAddress().getPort() +
                                     connectionTestServer401Context;
    }

    private static void startHttpEmptyTokenServer()
            throws IOException
    {
        String emptyTokenResponse = "";
        connectionTestServerEmpty = HttpServer.create(new InetSocketAddress(0), 0);
        connectionTestServerEmpty.setExecutor(null); // creates a default executor
        connectionTestServerEmpty.start();
        connectionTestServerEmpty.createContext(connectionTestServerEmptyContext, httpExchange -> {
            httpExchange.sendResponseHeaders(200, emptyTokenResponse.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(emptyTokenResponse.getBytes(StandardCharsets.UTF_8));
            os.close();
        });
        connectionTestServerEmptyUri = "http://127.0.0.1:" + connectionTestServerEmpty.getAddress().getPort() + connectionTestServerEmptyContext;
    }

    private static void startHttp429TokenServer()
            throws IOException
    {
        String emptyTokenResponse = "";
        connectionTestServer429 = HttpServer.create(new InetSocketAddress(0), 0);
        connectionTestServer429.setExecutor(null); // creates a default executor
        connectionTestServer429.start();
        connectionTestServer429.createContext(connectionTestServer429Context, httpExchange -> {
            httpExchange.sendResponseHeaders(429, -1);
        });
        connectionTestServer429Uri = "http://127.0.0.1:" + connectionTestServer429.getAddress().getPort() + connectionTestServer429Context;
    }

    private static void startTcpServer()
    {
        tcpServerThread = new Thread(() -> {
            Socket clientSocket = null;
            try
            {
                serverSocket = new ServerSocket(0);
                serverUri = "http://127.0.0.1:" + serverSocket.getLocalPort();
                clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println("See ya!");
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        tcpServerThread.start();
    }

    @AfterClass
    public static void cleanUpClass() throws Exception
    {
        serverSocket.close();
        tcpServerThread.interrupt();
        tcpServerThread.join();
        connectionTestServerEmpty.stop(0);
        connectionTestServer401.stop(0);
        connectionTestServer429.stop(0);
        connectionTestServer.stop(0);
    }

    @Test
    public void callAuthService()
            throws IOException
    {
        mixClientCredentials =
                new MixClientCredentials("abc123", Collections.singletonList("abc123"), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        connectionTestServerUri);

        MixAccessToken mat = MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
        assertEquals("Bqwqr8PWlQI9lnAiLz4LZD75xuDX_OUMi-tmk_2q7cK_tiJ6tlJ-wzHVAynIU6EVQ", mat.getAccessToken());
    }

    @Test(expected = IOException.class)
    public void callAuthServiceBad()
            throws IOException
    {
        mixClientCredentials =
                new MixClientCredentials("abc123", Collections.singletonList("abc123"), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        connectionTestServer401Uri);

        mockServerConfig.setProperty(WebApiConfig.Property.MIX_IDLE_REFRESH_ON_EXPIRE.key, true);

        MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
    }

    @Test(expected = IOException.class)
    public void callAuthServiceBadMinusOne()
            throws IOException
    {
        mixClientCredentials =
                new MixClientCredentials("abc123", Collections.singletonList("abc123"), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        serverUri);

        MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
    }

    @Test(expected = IOException.class)
    public void callAuthServiceNoId()
            throws IOException, InterruptedException, CobraException
    {
        mixClientCredentials =
                new MixClientCredentials("", Collections.singletonList("abc123"), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        connectionTestServer401Uri);
        MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
    }

    @Test(expected = IOException.class)
    public void callAuthServiceNoSecret()
            throws IOException
    {
        mixClientCredentials =
                new MixClientCredentials("", Collections.singletonList(""), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        connectionTestServer401Uri);
        MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
    }

    @Test(expected = IOException.class)
    public void callAuthServiceEmptyToken()
            throws IOException
    {
        mixClientCredentials =
                new MixClientCredentials("", Collections.singletonList("abc123"), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        connectionTestServerEmptyUri);
        MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
    }

    @Test(expected = IOException.class)
    public void callAuthServiceBadUri()
            throws IOException
    {
        mixClientCredentials =
                new MixClientCredentials("", Collections.singletonList("abc123"), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        "https://somebaduri");
        MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
    }

    @Test(expected = IOException.class)
    public void callAuthService429()
            throws IOException
    {
        mixClientCredentials =
                new MixClientCredentials("", Collections.singletonList("abc123"), "asr nlu tts dlg",
                        "grant_type=client_credentials&scope=",
                        connectionTestServer429Uri);
        MixClientCredentialsManager.callAuthService(mixClientCredentials, 30000);
    }

    @Test
    public void getBearerToken()
            throws ExecutionException
    {
        JSONObject token = new JSONObject("{\"access_token\":\"abc1234\",\"expires_in\":899,\"scope\":\"asr nlu tts dlg\",\"token_type\":\"bearer\"}");
        Instant hourFromNow = Instant.now().plus(1, ChronoUnit.HOURS);
        MixAccessToken mat = new MixAccessToken(token, hourFromNow);

        //Cover MixAccessToken
        String at = mat.getAccessToken();
        hourFromNow = mat.getActualExpirationTime();

        //The test
        LoadingCache<String, MixAccessToken> mockCache = mock(LoadingCache.class);
        MixClientCredentialsManager.setMixClientCredentialsMixAccessTokenCache(mockCache);
        PowerMockito.when(mockCache.get(any())).thenReturn(mat);

        String gotToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(mockServerConfigs));
        assertEquals(token.getString("access_token"), gotToken);

        //Test null
        PowerMockito.when(mockCache.get(any())).thenReturn(null);
        gotToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(mockServerConfigs));
        assertNull(gotToken);

        //Test expired
        mat = new MixAccessToken(token, Instant.now().minus(1, ChronoUnit.HOURS));
        PowerMockito.when(mockCache.get(any())).thenReturn(mat);
        gotToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(mockServerConfigs));
        assertEquals(token.getString("access_token"), gotToken);

        //Test exception
        PowerMockito.when(mockCache.get(any())).thenThrow(new ExecutionException(new Exception()));
        gotToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(mockServerConfigs));
        assertNull(gotToken);
    }

    @Test
    public void requeueRemovedItemTest()
            throws ExecutionException
    {
        LoadingCache<String, MixAccessToken> loadingCache = mock(LoadingCache.class);

        MixClientCredentials mixClientCredentials = new MixClientCredentials("testTenant", Collections.singletonList("testTenantSecret"), "asr nlu tts dlg", "grant_type=client_credentials&scope=", "https://auth.example.com/oauth2/token");
        String mixClientCredentialsString = mixClientCredentials.serialize();
        MixAccessToken mixAccessToken = new MixAccessToken(new JSONObject("{\"access_token\":\"abc1234\",\"expires_in\":899,\"scope\":\"asr nlu tts dlg\",\"token_type\":\"bearer\"}"), Instant.now());

        RemovalNotification<String, MixAccessToken> removalNotification = RemovalNotification.create(mixClientCredentialsString, mixAccessToken, RemovalCause.EXPLICIT);

        PowerMockito.mockStatic(MixClientCredentials.class);
        PowerMockito.when(MixClientCredentials.deserialize(anyString())).thenReturn(mixClientCredentials);
        MixClientCredentialsManager.setMixClientCredentialsMixAccessTokenCache(loadingCache);
        PowerMockito.when(loadingCache.get(anyString())).thenReturn(mixAccessToken);

        MixClientCredentialsManager.requeueRemovedItem(removalNotification);
        verify(loadingCache, never()).get(anyString());


        removalNotification = RemovalNotification.create(mixClientCredentialsString, mixAccessToken, RemovalCause.EXPIRED);
        MixClientCredentialsManager.requeueRemovedItem(removalNotification);
        verify(loadingCache).get(anyString());


        PowerMockito.when(loadingCache.get(anyString())).thenThrow(new ExecutionException("testing", new Throwable()));
        MixClientCredentialsManager.requeueRemovedItem(removalNotification);
        verify(loadingCache).invalidate(anyString());
    }
}
