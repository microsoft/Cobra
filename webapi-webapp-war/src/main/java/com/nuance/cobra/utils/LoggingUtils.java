package com.nuance.cobra.utils;

import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.logging.log4j.ThreadContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class LoggingUtils
{
    static final TimeZone timeZone = TimeZone.getTimeZone("UTC");

    /**
     * Thread-safe declaration of the date format
     */
    private static final ThreadLocal<DateFormat> utcDateFormat = ThreadLocal.withInitial(() -> {
            var initialValue = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US);
            initialValue.setTimeZone(timeZone);
            return initialValue;
        }
    );

    LoggingUtils()
    {
        throw new IllegalStateException("Utility class");
    }

    public static void setThreadContext(javax.websocket.Session wsSession) {
        com.nuance.cobra.session.Session webApiSession = getWebApiSession(wsSession);
        setThreadContext(webApiSession);
    }
    public static void setThreadContext(com.nuance.cobra.session.Session session) {
        if (session != null) {
            String sessionId = LoggingUtils.getWebApiSessionIdForLogging(session);
            String tenantId = LoggingUtils.getTenantForLogging(session);
            String companyName = LoggingUtils.getCompanyNameForLogging(session);
            String appName = LoggingUtils.getAppNameForLogging(session);
            ThreadContext.put(ThreadContextRunnable.SESSION_ID, sessionId);
            ThreadContext.put(ThreadContextRunnable.TENANT_ID, tenantId);
            ThreadContext.put(ThreadContextRunnable.COMPANY_NAME, companyName);
            ThreadContext.put(ThreadContextRunnable.APP_NAME, appName);
        }
    }

    //Returns an empty string if the session isn't found
    public static String getWebApiSessionIdForLogging(javax.websocket.Session wsSession)
    {
        com.nuance.cobra.session.Session webApiSession = getWebApiSession(wsSession);
        return getWebApiSessionIdForLogging(webApiSession);
    }

    public static String getTenantForLogging(com.nuance.cobra.session.Session webApiSession)
    {
        return getStringPropertyValueForLogging(webApiSession, WebApiConfig.Property.APPLICATION_KEY);
    }

    public static String getCompanyNameForLogging(com.nuance.cobra.session.Session webApiSession)
    {
        return getStringPropertyValueForLogging(webApiSession, WebApiConfig.Property.DIALOG_MODEL_COMPANY_NAME);
    }

    public static String getAppNameForLogging(com.nuance.cobra.session.Session webApiSession)
    {
        return getStringPropertyValueForLogging(webApiSession, WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME);
    }

    public static String getStringPropertyValueForLogging(com.nuance.cobra.session.Session webApiSession, WebApiConfig.Property stringProperty)
    {
        var stringValue = "";
        if (webApiSession != null)
        {
            stringValue = webApiSession.getSessionConfig()
                                       .getString(stringProperty.key);
        }
        return stringValue;
    }

    public static String getWebApiSessionIdForLogging(com.nuance.cobra.session.Session webApiSession)
    {
        var sessionId = "";
        if (webApiSession != null)
        {
            sessionId = webApiSession.getSessionId();
        }
        return sessionId;
    }

    public static com.nuance.cobra.session.Session getWebApiSession(javax.websocket.Session session)
    {
        com.nuance.cobra.session.Session webApiSession = null;
        try
        {
            webApiSession = SessionManager.getInstance()
                                          .getSession(session);
        }
        catch (NullPointerException e)
        {
            //There's no session
        }
        return webApiSession;
    }

    public static String getUTCFormattedTimestamp() {
        return getUTCFormattedTimestamp(Calendar.getInstance(timeZone));
    }

    public static String getUTCFormattedTimestamp(Calendar date) {
        String value = utcDateFormat.get().format(date.getTime());
        utcDateFormat.remove();
        return value;
    }
}
