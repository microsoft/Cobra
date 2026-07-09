package com.nuance.cobra.utils;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.session.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides rate limiting/throttling functionality to WebAPI.
 */
//TODO This class code needs to extended/modified for other upcoming rate limiting requirements.
//Currently it serves as basic RateLimiter
public class WebAPIRateLimiter
{

    private SemaphoreWrapper globalLimiter;
    private Map<String, SemaphoreWrapper> tenantLimiter;


    public WebAPIRateLimiter(int globalPermit)
    {
        globalLimiter = new SemaphoreWrapper(globalPermit);
        tenantLimiter = new ConcurrentHashMap<String, SemaphoreWrapper>();
    }


    public void release(String tenant)
    {
        globalLimiter.release();
        if (null != tenantLimiter.get(tenant))
        {
            tenantLimiter.get(tenant)
                         .release();
        }
    }

    public void release(int permit)
    {

        globalLimiter.release(permit);

    }

    public boolean checkIfRateLimitExceeded(String tenant, int tenantPermit)
            throws CobraException
    {
        if (globalLimiter.getMaxPermit() >= 0)
        {
            if (!globalLimiter.tryAcquire())
            {
                throw new CobraException(ResponseCode.createResponseError(
                        ExternalResponseErrorCode.GLOBAL_RATE_LIMIT_EXCEEDED_FOR_CONCURRENT_SESSIONS));
            }
        }
        if (tenantPermit >= 0)
        {
            if (null == tenantLimiter.get(tenant))
            {
                tenantLimiter.put(tenant, new SemaphoreWrapper(tenantPermit));
            }
            if (!tenantLimiter.get(tenant)
                              .tryAcquire())
            {
                //This is to release above acquired permission for global rate limit as we are throwing the exception and not actually using the acquired permission
                if (globalLimiter.getMaxPermit() >= 0) globalLimiter.release();
                throw new CobraException(ResponseCode.createResponseError(
                        ExternalResponseErrorCode.TENANT_RATE_LIMIT_EXCEEDED_FOR_CONCURRENT_SESSIONS));

            }
        }
        return false;
    }

    private void releaseExtraRateLimitPermit(SemaphoreWrapper rateLimiter, int noOfSessions)
    {
        if (rateLimiter.getMaxPermit() >= 0)
        {
            if (rateLimiter.getAvailablePermits() == 0
                && noOfSessions < rateLimiter.getMaxPermit())
            {
                rateLimiter.release(rateLimiter.getMaxPermit() - noOfSessions);
            }
        }
    }

    //This will only run in case rate limiting is exceeded and we see discrepancy between rate limiter's available permits and number of concurrent sessions
    public void garbageCollect(Map<String, Session> sessionMap)
    {
        if (globalLimiter.getMaxPermit() < 0)
        {
            return;
        }

        releaseExtraRateLimitPermit(globalLimiter, sessionMap.size());
        Map<String, Integer> noOfSessionsPerTenant = new HashMap<>();
        for (Session session : sessionMap.values())
        {
            String tenant = Session.getTenantKey(session);
            if (null == noOfSessionsPerTenant.get(tenant))
            {
                noOfSessionsPerTenant.put(tenant, 1);
            }
            else
            {
                noOfSessionsPerTenant.put(tenant, noOfSessionsPerTenant.get(tenant) + 1);
            }
        }
        for (String tenant : noOfSessionsPerTenant.keySet())
        {
            //garbageCollect(tenant, noOfSessionsPerTenant.get(tenant));
            if (null != tenantLimiter.get(tenant))
            {
                releaseExtraRateLimitPermit(tenantLimiter.get(tenant), noOfSessionsPerTenant.get(tenant));
            }
        }
    }
}
