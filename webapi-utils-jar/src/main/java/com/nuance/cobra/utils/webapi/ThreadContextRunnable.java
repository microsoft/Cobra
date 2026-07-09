package com.nuance.cobra.utils.webapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import java.util.Map;

public abstract class ThreadContextRunnable implements Runnable {
    public static final String SESSION_ID = "session.id";
    public static final String TENANT_ID = "application.key";
    public static final String COMPANY_NAME = "company.name";
    public static final String APP_NAME = "app.name";

    private Logger logger = LogManager.getLogger();
    private final Map context = ThreadContext.getContext();

    @Override
    public void run() {
        ThreadContext.putAll(context);

        try
        {
            runWithContext();
        }
        catch (Throwable e) {
            // log what has happened so we don't silently fail.
            logger.error("Error while running task in ThreadContextRunnable: ", e);
            throw e;
        }
        finally {
            ThreadContext.clearAll();
        }
    }

    public abstract void runWithContext();
}
