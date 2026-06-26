package com.nuance.cobra.client;

import com.nuance.cobra.session.SessionManager;

import java.util.Map;

/**
 * The abstract definition of a InterpretationClient
 */
public abstract class InterpretationClient
        extends Client
{
    /**
     * InterpretationClient constructor
     *
     * @param sessionId
     *         The session identifier of the current session
     */
    protected InterpretationClient(String sessionId)
    {
        super(sessionId);
    }

    /**
     * Abstract method for the execution of the interpretation
     */
    public abstract void startInterpretation();

    /**
     * Abstract method for stopping the execution of the interpretation
     */
    public abstract void stopInterpretation();

    /**
     * Abstract method for cancelling the execution of the interpretation
     */
    public abstract void cancelInterpretation(boolean isFinal);

    public void setStatistics(String engine, String interpretationLanguage, String semanticModelUri)
    {
        setStatisticModelVersion(semanticModelUri);
        setEngineRequestStatistics(engine, interpretationLanguage, semanticModelUri);
    }

    private void setStatisticModelVersion(String semanticModelUri)
    {
        SessionManager.getInstance()
                      .getSession(getSessionId(), false)
                      .getCommand()
                      .getStatistics()
                      .setModelVersion(semanticModelUri);
    }

    private void setEngineRequestStatistics(String engine, String interpretationLanguage, String semanticModelUri)
    {
        Map<String, Object> engineRequestStatistics = Map.of(
                "engine", engine,
                "language", interpretationLanguage,
                "model", semanticModelUri);

        SessionManager.getInstance()
                      .getSession(getSessionId(), false)
                      .getCommand()
                      .getStatistics()
                      .setEngineRequests(engineRequestStatistics);
    }

}
