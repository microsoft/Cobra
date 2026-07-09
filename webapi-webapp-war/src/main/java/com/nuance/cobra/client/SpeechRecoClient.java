package com.nuance.cobra.client;

import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.audio.AudioDestination;
import com.nuance.cobra.utils.audio.AudioNodeObserver;
import com.nuance.cobra.utils.recognition.result.RecResultType;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * The abstract definition of a SpeechRecoClient
 *
 */
public abstract class SpeechRecoClient extends Client implements AudioNodeObserver, AudioDestination
{

    public static final String START_SPEECH_TIMER_ID = "START_SPEECH_TIMER";
    public static final String MAX_SPEECH_TIMER_ID = "MAX_SPEECH_TIMER";

    /**
     * SpeechRecoClient constructor
     * 
     * @param sessionId
     *        The session Id of the current session
     */
    protected SpeechRecoClient(String sessionId)
    {
        super(sessionId);
    }

    /**
     * Abstract method for the execution of the speech recognition
     */
    public abstract void startSpeechRecognition();

    /**
     * Abstract method for stopping the speech recognition
     */
    public abstract void stopSpeechRecognition();

    /**
     * Abstract method for cancelling the speech recognition
     */
    public abstract void cancelSpeechRecognition(boolean isFinal);

    /**
     * Abstract method for starting recognition timers
     */
    public abstract void startRecognitionTimers();

    /**
     * Indicates desired rec result type.
     * Returns false if desired rec result type isn't supported.
     */
    public abstract boolean setRecResultType(RecResultType recResultType);

    /**
     * Returns the maximum confidence score for this recognizer
     */
    public abstract float getMaxConfidence();

    public void setStatistics(String engine, String language, String topic, List<Map<String, String>> dlms, List<Map<String, Object>> builtins)
    {
        if(!dlms.isEmpty())
        {
            setStatisticModelVersion(dlms.get(0).get("uri"));
        }
        setEngineRequestStatistics(engine, language, topic, dlms, builtins);
    }

    public void setStatisticModelVersion(String semanticModelUri)
    {
        Session session = SessionManager.getInstance().getSession(getSessionId(), false);
        if (session == null) return;
        if (session.getCommand() == null) return;
        session.getCommand().getStatistics().setModelVersion(semanticModelUri);
    }

    private void setEngineRequestStatistics(String engine, String language, String topic, List<Map<String, String>> dlms, List<Map<String, Object>> builtins)
    {
        Map<String, Object> engineRequestStatistics = Map.of(
                "engine", engine,
                "language", language,
                "topic", topic,
                "dlms", dlms,
                "builtins", builtins
        );

        Session session = SessionManager.getInstance().getSession(getSessionId(), false);
        if (session == null) return;
        if (session.getCommand() == null) return;

        session.getCommand().getStatistics().setEngineRequests(engineRequestStatistics);
    }

    protected void setAudioStreamingToEngineStatistics(int nbBufferedAudioChunks, int nbSentAudioChunks, Calendar timeAtWhichTheFirstAudioChunkWasSent, Calendar timeAtWhichTheLastAudioChunkWasSent)
    {
        Session session = SessionManager.getInstance().getSession(getSessionId(), false);
        if (session == null) return;
        if (session.getCommand() == null) return;

        session.getCommand().getStatistics().setAudioStreamingToEngineStatistics(nbBufferedAudioChunks, nbSentAudioChunks, timeAtWhichTheFirstAudioChunkWasSent, timeAtWhichTheLastAudioChunkWasSent);
    }

    public String getLanguage() {
        return null;
    }

    public void setObservers(List<AudioNodeObserver> observers) {
        // Unused by default
    }
}
