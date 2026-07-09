package com.nuance.nmsp.servlet;

import com.nuance.ncs.nmsp.engine.pdx.PdxClient;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Transaction
{
    private final AuthorizationInfo authorization;
    private final String uniqueId;
    Object syncOject = new Object();
    GatewayConditionWait condition = new GatewayConditionWait();
    private PdxClient client;
    private boolean requestCompleted = true;
    private String sessionId;
    private String errorMsg;
    private String results = "";
    private int errorId = 0;

    private static final Logger log = ESAPI.getLogger(Transaction.class);

    public Transaction(AuthorizationInfo authorization, String uniqueId)
    {
        this.authorization = authorization;
        this.uniqueId = uniqueId;
        this.errorId = 0;
    }

    public AuthorizationInfo getAuthorizationInfo()
    {
        return authorization;
    }

    public String getUniqueId()
    {
        return uniqueId;
    }

    public boolean isRequestCompleted()
    {
        synchronized (syncOject)
        {
            return requestCompleted;
        }
    }

    public void setRequestCompleted(boolean requestCompleted)
    {
        synchronized (syncOject)
        {
            this.requestCompleted = requestCompleted;
            condition.signal();
        }
    }

    public void waitForResponse()
    {
        condition.waitForNMSPGateway();
    }

    public PdxClient getClient()
    {
        return client;
    }

    public void setClient(PdxClient client)
    {
        this.client = client;
    }

    public String getSessionId()
    {
        return this.sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public void cleanup()
    {
        if (client != null)
        {
            try
            {
                client.endCommand();
                client.freeResource();
                client.shutdown();
            }
            catch (Exception fe)
            {
                log.info(Logger.EVENT_UNSPECIFIED, "Exception while shutting down PDX client", fe);
            }
            client = null;
        }
    }

    public String getError()
    {
        return errorMsg;
    }

    public void setError(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public int getErrorID()
    {
        return errorId;
    }

    public void setErrorID(int id)
    {
        this.errorId = id;
    }

    public String getResults()
    {
        return results;
    }

    public void setResults(String results)
    {
        this.results = results;
    }

    protected class GatewayConditionWait
    {
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        public void waitForNMSPGateway()
        {
            lock.lock();
            try
            {
                condition.await();
            }
            catch (InterruptedException e)
            {
                log.info(Logger.EVENT_UNSPECIFIED, "Caught an InterruptedException", e);
                Thread.currentThread().interrupt();
            }
            finally
            {
                lock.unlock();
            }
        }

        public void signal()
        {
            lock.lock();
            try
            {
                condition.signal();
            }
            finally
            {
                lock.unlock();
            }
        }

    }

}
