package com.nuance.cobra.utils.response;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import com.sun.net.httpserver.*;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class HttpAlarmSenderTest {

    private static final String USER = "alarmUser"; //gitleaks:allow
    private static final String PASSWORD = "password1234"; //gitleaks:allow
    private static final String EXPECTED_ZEN_OSS_API_KEY_VALUE = "test-zen-oss-api-key"; //gitleaks:allow
    private static final String ENCRYPTED_PASSWORD = "U2FsdGVkX1973qvpUistXc2VmJezbFM5BJkumSh6VcM="; //gitleaks:allow
    private static final String ENCRYPTED_WRONG_PASSWORD = "U2FsdGVkX187jVgapKhBRhXYte72jzr9dF90zgJAU7M="; //gitleaks:allow

    private static final String ALARM_HTTP_CONTEXT_PATH = "/alarms";
    private static final String ALARM_SECRETS_RESOURCE_PATH = "secrets/alarms/";

    private static final String TENANT_NAME = "tenant-name-1234";
    private static final String TEST_PROP_VALUE = "testPropValue5678";


    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration testConf = testConfs.getConfiguration(0);
    HttpServer server;

    /**
     * configures the behavior of a given test.
     */
    static class AlarmTestOptions {

        enum AuthType { BASIC, ZENOSS_API_KEY };

        /**
         * Expected return value Boolean: when null (default), the sendAlarmCanThrowExcaption) method that can throw exceptions will be called
         * instead of the public wrapper that returns true/false (for backward compatibility.)
         * For negative test purposes it is usually preferable to get the exception, as it includes more information about
         * the error that happened. More precise assertion can be done to ensure we got the intended failure and that
         * it was not masked by some other cause.
         */
        Boolean expectedReturnValue = null;

        String templateFile = null;
        AuthType authType;

        AlarmTestOptions(AuthType authType) {
            this.authType = authType;
        }

        AlarmTestOptions(AuthType authType, Boolean expectedReturnValue) {
            this.authType = authType;
            this.expectedReturnValue = expectedReturnValue;
        }

        AlarmTestOptions(AuthType authType, Boolean expectedReturnValue, String templateFile) {
            this.authType = authType;
            this.expectedReturnValue = expectedReturnValue;
            this.templateFile = templateFile;
        }
    }

    @Before
    public void setUp()
            throws Exception
    {
        server = HttpServer.create(new InetSocketAddress(0), 0); //use port 0 to get a free ephemeral port.
        server.setExecutor(null); // creates a default executor
        server.start();

        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_ENDPOINT_URL.key, "http://127.0.0.1:" + server.getAddress().getPort() + ALARM_HTTP_CONTEXT_PATH);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_TEMPLATE_FILE.key, "velocity_alert.vm.tmpl");
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_MEDIA_TYPE.key, "application/json");
        System.setProperty("testProp", TEST_PROP_VALUE);
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void sendAlarmPublicApiSuccessful() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, ENCRYPTED_PASSWORD);

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC, true));
    }

    @Test
    public void sendAlarmPublicApiFailure() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, ENCRYPTED_WRONG_PASSWORD);

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC, false));
    }

    @Test
    public void sendAlarmWrongPassword() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, ENCRYPTED_WRONG_PASSWORD);

        try {
            runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
        } catch (HttpAlarmSenderRequestFailedException e) {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
        }
    }

    @Test(expected = HttpAlarmSenderConfigException.class)
    public void sendAlarmInvalidPasswordConfig() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, "a" ); // not properly encrypted

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test
    public void sendAlarmClearTextPassword() throws HttpAlarmSenderException{
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_CLEARTEXT_PASSWORD.key, "password1234");

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test
    public void sendAlarmPasswordFromFile() throws IOException, HttpAlarmSenderException {
        File secretFile = copyResourceToTempFile("passwordFile.txt", "tmpAlarmPasswordFile");
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD_FILE_PATH.key, secretFile);

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test
    public void sendAlarmWrongPasswordFromFile() throws IOException, HttpAlarmSenderException {
        File secretFile = copyResourceToTempFile("passwordFileWrong.txt", "tmpAlarmPasswordFile");
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD_FILE_PATH.key, secretFile);

        try {
            runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
        } catch (HttpAlarmSenderRequestFailedException e) {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
        }
    }

    @Test(expected = HttpAlarmSenderConfigException.class)
    public void alarmTestBasicAuthNoCredentials() throws HttpAlarmSenderException {
        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test (expected = HttpAlarmSenderConfigException.class)
    public void alarmTestBasicAuthUser() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, ENCRYPTED_PASSWORD);
        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test (expected = HttpAlarmSenderConfigException.class)
    public void alarmTestBasicAuthPassword() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test
    public void alarmTestBasicAuthClearPasswordPrecedence() throws HttpAlarmSenderException {
        // configure password (valid) and cleartext passwork (wrong) to confirm the former takes precedence.
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, ENCRYPTED_PASSWORD);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_CLEARTEXT_PASSWORD.key, "wrongPassword1234");

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test
    public void alarmTestBasicAuthPasswordFilePrecedence() throws HttpAlarmSenderException, IOException {
        // configure password file (valid) and password (wrong) to confirm the former takes precedence.
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        File secretFile = copyResourceToTempFile("passwordFile.txt", "tmpAlarmPasswordFile");

        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, ENCRYPTED_WRONG_PASSWORD);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD_FILE_PATH.key, secretFile);

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC));
    }

    @Test
    public void alarmTestZenOssApiKey() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY.key, EXPECTED_ZEN_OSS_API_KEY_VALUE);

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.ZENOSS_API_KEY));
    }

    @Test
    public void alarmTestWrongZenOssApiKey() throws HttpAlarmSenderException {
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY.key, "bad-key");

        try {
            runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.ZENOSS_API_KEY));
        } catch (HttpAlarmSenderRequestFailedException e) {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatusCode());
        }

    }

    @Test
    public void alarmTestZenOssApiKeyFromFile() throws IOException, HttpAlarmSenderException {
        File secretFile = copyResourceToTempFile("zenoss-api-key.txt", "tmpAlarmZenKeyFile");
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY_FILE_PATH.key, secretFile);
        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.ZENOSS_API_KEY));
    }

    @Test
    public void alarmTestZenOssApiKeyFilePrecedence() throws IOException, HttpAlarmSenderException {
        // configure zenoss-api-key file (valid) and basic auth (invalid) to confirm the former takes precedence.
        File secretFile = copyResourceToTempFile("zenoss-api-key.txt", "tmpAlarmZenKeyFile");
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY_FILE_PATH.key, secretFile);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY.key, "bad key");

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.ZENOSS_API_KEY));
    }

    @Test
    public void alarmTestZenOssApiKeyBasicAuthPrecedence() throws HttpAlarmSenderException, IOException {
        // configure zenoss-api-key file (valid) and basic auth (wrong) to confirm the former takes precedence.
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY.key, EXPECTED_ZEN_OSS_API_KEY_VALUE);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, USER);
        testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, ENCRYPTED_WRONG_PASSWORD);

        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.ZENOSS_API_KEY));
    }

   @Test(expected = HttpAlarmSenderConfigException.class)
    public void setWrongTemplate() throws HttpAlarmSenderException {
       runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC, null, "wrongTemplate"));
   }

    @Test(expected = HttpAlarmSenderTemplateException.class)
    public void parseError() throws HttpAlarmSenderException {
        runSendAlarmTest(new AlarmTestOptions(AlarmTestOptions.AuthType.BASIC, null, "velocity_alert_parse_error.vm.tmpl"));
    }

    /**
     * Called by individual test cases after their setup
     */
    private void runSendAlarmTest(AlarmTestOptions options) throws HttpAlarmSenderException {
        if (options.templateFile != null) {
            testConf.setProperty(WebApiConfig.Property.HTTP_ALARM_TEMPLATE_FILE.key, options.templateFile);
        }

        ResponseCode respCode = ResponseCode.createResponseError(ExternalResponseErrorCode.INVALID_VERIFICATION_CODE_ERROR);
        createTestAlarmHttpContext(options, respCode);

        System.setProperty("JETTY_BASE", ".");
        ThreadContext.put(ThreadContextRunnable.TENANT_ID, TENANT_NAME);
        HttpAlarmSender alarmSender = new HttpAlarmSender();
        IResponse response = createResponseFromCode(respCode);

        if (options.expectedReturnValue == null) {
            alarmSender.sendAlarmOrThrowException(response, testConfs);
        }
        else {
            assertEquals(options.expectedReturnValue, alarmSender.sendAlarm(response, testConfs));
        }
    }

    /**
     * Creates the handler for the stub alarm server and associates it with the alarm context.
     */
    private HttpContext createTestAlarmHttpContext(AlarmTestOptions options, ResponseCode alarmServerResponse) {
        final HttpContext alarmContext = server.createContext(ALARM_HTTP_CONTEXT_PATH, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {

                int httpStatus = 200;
                String responseStr = "OK";

                if (options.authType == AlarmTestOptions.AuthType.ZENOSS_API_KEY) {
                    System.out.println("Authenticating with z-api-key header.");
                    String zenApiKey = httpExchange.getRequestHeaders().getFirst(HttpAlarmSender.ZEN_OSS_API_KEY_HEADER);
                    assertNotNull(zenApiKey);
                    if (! EXPECTED_ZEN_OSS_API_KEY_VALUE.equals(zenApiKey)) {
                        httpStatus = 401;
                        responseStr = "Unauthorized";
                    }
                }

                // asserts on the expected request content
                validateRequest(httpExchange);

                httpExchange.sendResponseHeaders(httpStatus, responseStr.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(responseStr.getBytes());
                os.close();
            }

            public boolean validateRequest(HttpExchange httpExchange) throws IOException {
                String theString = IOUtils.toString(httpExchange.getRequestBody(),"UTF-8");
                System.out.println(theString);

                assertTrue(theString.contains("WebAPI"));
                assertTrue(theString.contains(String.valueOf(alarmServerResponse.getCode())));

                return true;
            }

        });

        if (options.authType == AlarmTestOptions.AuthType.BASIC) {
            alarmContext.setAuthenticator(new BasicAuthenticator("alarm-realm") {
                @Override
                public boolean checkCredentials(String user, String pwd) {
                    System.out.println("Authenticating ... " + user + ":" + pwd);
                    return user.equals(USER) && pwd.equals(PASSWORD);
                }
            });
        }

        return alarmContext;
    }


    /**
     * Creates and returns a response object for an error code.
     */
    private IResponse createResponseFromCode(ResponseCode respCode) {
        return new IResponse() {
            @Override
            public String toJson() throws CobraException {
                return "";
            }

            @Override
            public List<ResponseCode> getErrors() {
                return Collections.singletonList(respCode);
            }
        };
    }

    /**
     * Creates a temp file and copies the content of a resource (under src/test/resources/secrets/alarms) in it.
     * Used to test reading the secrets from a file.
     */
    private File copyResourceToTempFile(String resourceFile, String tempSecretPrefix) throws IOException {
        File tmpSecretFile = File.createTempFile(tempSecretPrefix, ".txt");
        tmpSecretFile.deleteOnExit();

        InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(ALARM_SECRETS_RESOURCE_PATH + resourceFile);
        Files.copy(fileInputStream, tmpSecretFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tmpSecretFile;
    }
}