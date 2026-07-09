package com.nuance.cobra.client;

import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;

import java.util.Map;

/**
 * The abstract definition of a SpeechSynthesisClient
 */
public abstract class SpeechSynthesisClient
        extends Client
{
    public enum ClientState
    {
        IDLE,
        EXECUTING,
        STOPPED,
        DONE
    }

    /**
     * SpeechSynthesisClient constructor
     *
     * @param sessionId
     *         The session identifier of the current session
     */
    protected SpeechSynthesisClient(String sessionId)
    {
        super(sessionId);
    }

    /**
     * Abstract method for the execution of the speech synthesis
     */
    public abstract void startSpeechSynthesis();

    /**
     * Abstract method for stopping the execution of the speech synthesis
     */
    public abstract void stopSpeechSynthesis();

    /**
     * Abstract method for cancelling the execution of the speech synthesis
     */
    public abstract void cancelSpeechSynthesis(boolean isFinal);

    /**
     * Abstract method to check if speech synthesis is dome
     */
    public abstract boolean isSpeechSynthesisDone();

    // Empty/wrong value of tts genre would result in disabling loading genre for this request.
    // Set statistics value for speechSynthesisTextGenre as 'notDefined'.
    public void setStatisticsForGenre(String userTtsGenre)
    {
        Session session = SessionManager.getInstance().getSession(getSessionId(), false);
        if (session == null) return;
        session.getCommand().getStatistics().setSpeechSynthesisTuningBundle(userTtsGenre);
    }

    public void setResponseStatisticsMediaDurationMillis(int mediaDurationMillis)
    {
        try
        {
            Session session = SessionManager.getInstance().getSession(getSessionId(), false);
            if (session == null) return;
            session.getCommand()
                   .getStatistics()
                   .setMediaDuration(mediaDurationMillis);
        } catch(NullPointerException npe)
        {
            //Do nothing
        }
    }

    public void setEngineRequestStatistics(String engine, String language, String voice, String voiceModel, String voiceQuality, Map<String, Object> resources)
    {
        if(language == null) language = "";

        Map<String, Object> engineRequestStatistics = Map.of(
                "engine", engine,
                "language", language,
                "voice", voice,
                "voiceModel", voiceModel,
                "voiceQuality", voiceQuality,
                "resources", resources
        );

        Session session = SessionManager.getInstance().getSession(getSessionId(), false);
        if (session == null) return;

        session.getCommand().getStatistics().setEngineRequests(engineRequestStatistics);
    }
}
