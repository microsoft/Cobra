package com.nuance.cobra.utils.response;

import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.encryption.OpenSSLDecryptor;
import com.nuance.cobra.utils.encryption.SSLDecodingException;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;


public class HttpAlarmSender {

    static public String ZEN_OSS_API_KEY_HEADER = "z-api-key";

    private static final Logger logger = LogManager.getLogger(HttpErrorListener.class);
    private OkHttpClient httpClient;
    private VelocityEngine velocityEngine;

    public HttpAlarmSender() {
        httpClient = new OkHttpClient();
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, WebApiConfigManager.getConfigPath());
        velocityEngine.init();
    }

    //template utility class
    public static class Utils {
        public static Class<StringEscapeUtils> stringEscapeUtils() {
            return StringEscapeUtils.class;
        }

        public static Class<Base64> base64() {
            return Base64.class;
        }
    }

    /**
     * Sends an alarm. Return true if it was sent successfully, false otherwise.
     */
    public boolean sendAlarm(IResponse event, CombinedConfiguration srvConf) {
        try {
            sendAlarmOrThrowException(event, srvConf);
        } catch (HttpAlarmSenderException e) {
            logger.error("Failed to send HTTP alarm: ", e);
            return false;
        }

        return true;
    }

    /**
     * Send an alarm, throws exception if the alarm was not successfully sent. Useful to
     */
    void sendAlarmOrThrowException(IResponse event, CombinedConfiguration srvConf)
            throws HttpAlarmSenderException {

        VelocityContext context = new VelocityContext();

        context.put("tenant", ThreadContext.get(ThreadContextRunnable.TENANT_ID));
        context.put("event", event);
        context.put("systemEnvironment", System.getenv());
        context.put("systemProperties", System.getProperties());
        context.put("webApiConfig", srvConf);
        context.put("utils", Utils.class);

        Template template;

        try {
            template = velocityEngine.getTemplate(srvConf.getString(WebApiConfig.Property.HTTP_ALARM_TEMPLATE_FILE.key));
        } catch (ResourceNotFoundException rnfe) {
            throw new HttpAlarmSenderConfigException("Failed to load event format template.", WebApiConfig.Property.HTTP_ALARM_TEMPLATE_FILE.key, rnfe);
        } catch (ParseErrorException pee) {
            throw new HttpAlarmSenderTemplateException("Velocity template parse error.", pee);
        } catch (MethodInvocationException mie) {
            throw new HttpAlarmSenderTemplateException("Velocity template execution threw an exception.", mie);
        }
        String renderedTemplate = "";
        try (StringWriter sw = new StringWriter()) {
            template.merge(context, sw);
            sw.flush();
            renderedTemplate = sw.toString();
            logger.debug("Rendered alarm: {}", renderedTemplate);
        } catch (IOException e) {
            logger.info("Failed to close stream", e);
        }

        String endpointURL = srvConf.getString(WebApiConfig.Property.HTTP_ALARM_ENDPOINT_URL.key);
        RequestBody body = RequestBody.create(MediaType.parse(srvConf.getString(WebApiConfig.Property.HTTP_ALARM_MEDIA_TYPE.key)), renderedTemplate);
        Request.Builder request = new Request.Builder()
                .url(endpointURL)
                .post(body)
                .addHeader("content-type", "application/json; charset=utf-8");

        addAuthorizationHeaders(request, srvConf);

        try (Response response = httpClient.newCall(request.build()).execute()) {
            if (!response.isSuccessful()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error response from alarm server: {} {}", response.message(), response.body().string());
                }

                throw new HttpAlarmSenderRequestFailedException(response.code());
            }
        } catch (IOException e) {
            throw new HttpAlarmSenderException("Exception when sending alarm.", e);
        }
    }

    /**
     * Sets the autorization header(s) to the request based on the configuration.
     */
    private void addAuthorizationHeaders(Request.Builder request, CombinedConfiguration srvConf)
            throws HttpAlarmSenderException {

        String zenOssApiKey = null;

        // try reading the ZenOss api key from a file (if path configured), or else getting the value from the config.
        zenOssApiKey = readConfigValue(WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY_FILE_PATH.key,
                WebApiConfig.Property.HTTP_ALARM_ZENOSS_API_KEY.key, srvConf);

        // add the ZenOss api key custom header if the key could be obtained and return.
        if (zenOssApiKey != null) {
            request.addHeader(ZEN_OSS_API_KEY_HEADER, zenOssApiKey);
            return;
        }

        // ZenOss API Key was not defined, use basic authentication instead

        // try reading the username from a file (if path configured), or else getting the value from the config.
        String username = readConfigValue(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME_FILE_PATH.key,
                WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key, srvConf);

        if (username == null) {
            throw new HttpAlarmSenderConfigException("Username for HTTP alarm basic authentication is not defined in configuration.", WebApiConfig.Property.HTTP_ALARM_BASICAUTH_USERNAME.key);
        }

        // try reading the password from a file (if path configured), or get null
        String password = readConfigValue(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD_FILE_PATH.key,
                null, srvConf);

        if (password == null) {
            // password file path not defined, try to read the legacy param (encrypted using the Nuance webapi password key
            String encryptedPassword = srvConf.getString(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key);
            if (encryptedPassword != null) {
                try {
                    password = OpenSSLDecryptor.decrypt(encryptedPassword);
                } catch (SSLDecodingException e) {
                    throw new HttpAlarmSenderConfigException("Failed to decrypt password in configuration.",
                            WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key, e);
                }
            }
        }

        if (password == null) {
            // still not defined... Try the password in cleartext, supported for on-premises
            // deployments that do not have access to the encryption key
            password = srvConf.getString(WebApiConfig.Property.HTTP_ALARM_BASICAUTH_CLEARTEXT_PASSWORD.key);
        }

        if (password == null) {
            throw new HttpAlarmSenderConfigException("Password for HTTP alarm basic authencation is not defined in configuration.", WebApiConfig.Property.HTTP_ALARM_BASICAUTH_PASSWORD.key);
        }

        String credential = Credentials.basic(username, password);
        request.addHeader("Authorization", credential);
    }

    /**
     * Reads a config value from a file, if the file path is specified in the config (passed as parameter.)
     * Optionally, an alternate key to directly get the value from the config can be specified.
     */
    String readConfigValue(@NotNull String filePathConfigKey, @Nullable String altValueConfigKey, CombinedConfiguration srvConf)
            throws HttpAlarmSenderException {

        String valueFilePath = srvConf.getString(filePathConfigKey);
        if (valueFilePath != null) {
            try {
                return Files.readString(Paths.get(valueFilePath))
                        .replaceAll("\r", "")
                        .replaceAll("\n", "")
                        .strip();
            } catch (IOException e) {
                throw new HttpAlarmSenderConfigException("Failed to read data from file \'" +  valueFilePath + "\'.", filePathConfigKey, e);
            }
        }

        if (altValueConfigKey != null) {
            return srvConf.getString(altValueConfigKey);
        }

        return null;
    }


}
