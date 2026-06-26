package com.nuance.cobra.utils.response;

import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.response.error.listener.ErrorListener;
import com.nuance.cobra.utils.response.error.listener.ThrottledErrorListener;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ResponseErrorHandler
{
    private static final Logger logger = LogManager.getLogger(ResponseErrorHandler.class);
    private static final long THROTTLE_INTERVAL_SECONDS = 60;
    private static final long THROTTLE_INTERVAL_MILLISECONDS = TimeUnit.SECONDS.toMillis(THROTTLE_INTERVAL_SECONDS);

    private LinkedList<ErrorListener> listeners = new LinkedList<>();
    private static Map<String, EventInfo> throttleMap = new HashMap<>();

    private static class EventInfo {
        protected long lastTime;
        protected Integer sentCount;
        protected String tenant;
    }

    public void addListener(ErrorListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(ErrorListener listener)
    {
        listeners.remove(listener);
    }

    public void handleError(IResponse error)
    {
        for (ErrorListener listener : listeners)
        {
            if (listener instanceof ThrottledErrorListener) {
                if (isThrottled(error, ThreadContext.get(ThreadContextRunnable.TENANT_ID), listener))
                {
                    return;
                }

                listener.notify(error);

                updateThrottle(error, ThreadContext.get(ThreadContextRunnable.TENANT_ID), listener);
            } else {
                listener.notify(error);
            }
        }
    }

    private void updateThrottle(IResponse event, String tenantName, ErrorListener owner) {
        CombinedConfiguration srvConf = WebApiConfigManager.getInstance();

        if (getLimit(srvConf) < 0) return;

        String eventKey = event.getErrors().get(0).getCode() + "|" + tenantName + "|" + owner.getClass().getSimpleName();

        if (throttleMap.containsKey(eventKey))
        {
            EventInfo info = throttleMap.get(eventKey);
            if ((System.currentTimeMillis() - info.lastTime) > THROTTLE_INTERVAL_MILLISECONDS)
            {
                info.lastTime = System.currentTimeMillis();
                info.sentCount = 1;
            }
            else
            {
                info.sentCount++;
            }
        }
        else
        {
            EventInfo info = new EventInfo();
            info.sentCount = 1;
            info.lastTime = System.currentTimeMillis();
            info.tenant = ThreadContext.get(ThreadContextRunnable.TENANT_ID);
            throttleMap.put(eventKey, info);
        }
    }

    private boolean isThrottled(IResponse event, String tenantName, ErrorListener owner) {
        CombinedConfiguration srvConf = WebApiConfigManager.getInstance();

        if (getLimit(srvConf) < 0) return false;

        String eventKey = event.getErrors().get(0).getCode() + "|" + tenantName + "|" + owner.getClass().getSimpleName();

        if (throttleMap.containsKey(eventKey))
        {
            EventInfo info = throttleMap.get(eventKey);
            if ((System.currentTimeMillis() - info.lastTime) < THROTTLE_INTERVAL_MILLISECONDS)
            {
                return (info.sentCount >= getLimit(srvConf));
            }
            else
            {
                info.sentCount = 1;
            }
            return false;
        }
        else
        {
            return false;
        }
    }

    private int getLimit(CombinedConfiguration srvConf) {
        int result = srvConf.getInt(WebApiConfig.Property.ALARMS_PER_MINUTE.key);
        if (result < 0 ) {
            result = srvConf.getInt(WebApiConfig.Property.SNMP_TRAPS_PER_MINUTE.key);
        }

        return result;
    }
}
