package com.nuance.cobra.client;

import com.nuance.cobra.session.SessionManager;

import java.util.Map;

/**
 * The abstract definition of a InterpretationClient
 */
public abstract class UserResetClient
        extends Client
{
    /**
     * InterpretationClient constructor
     *
     * @param sessionId
     *         The session identifier of the current session
     */
    protected UserResetClient(String sessionId)
    {
        super(sessionId);
    }

    /**
     * Abstract method for the execution of the user reset
     */
    public abstract void startUserReset();

    /**
     * Abstract method for stopping the execution of the user reset
     */
    public abstract void stopUserReset();

    /**
     * Abstract method for cancelling the execution of the user reset
     */
    public abstract void cancelUserReset(boolean isFinal);

    public void setStatistics(String engine)
    {
        Map<String, Object> engineRequestStatistics = Map.of(
                "engine", engine);

        SessionManager.getInstance()
                      .getSession(getSessionId(), false)
                      .getCommand()
                      .getStatistics()
                      .setEngineRequests(engineRequestStatistics);
    }

}
