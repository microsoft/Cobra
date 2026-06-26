package com.nuance.cobra.client.nim;


import com.nuance.cobra.client.SpeechRecoClient;
import com.nuance.cobra.session.DynamicVocabulary;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.utils.audio.AudioChunk;
import com.nuance.cobra.utils.audio.AudioNode;
import com.nuance.cobra.utils.audio.AudioNodeObserver;
import com.nuance.cobra.utils.recognition.result.RecResultType;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;

import java.util.List;

public class MrecNimClient
        extends SpeechRecoClient
        implements NiMClientListener, AudioNodeObserver {

    public MrecNimClient(CombinedConfiguration requestConfig, Session session, List<DynamicVocabulary> dynamicVocabularies, ResponseHandler responseHandler) {
        super(session.getSessionId());
    }

    @Override
    public void startSpeechRecognition()
    {
    }

    @Override
    public void stopSpeechRecognition()
    {
    }

    @Override
    public void cancelSpeechRecognition(boolean isFinal)
    {
    }

    @Override
    public void startRecognitionTimers()
    {
    }

    @Override
    public boolean setRecResultType(RecResultType recResultType)
    {
        return false;
    }

    @Override
    public float getMaxConfidence()
    {
        return 1000.0f;
    }

    public void onNiMEvent(short event)
    {
    }

    public void onNiMError(JSONObject error)
    {
    }

    public void onNiMResult(JSONObject result)
    {
    }

    public void onNiMRetry(JSONObject retry)
    {
    }

    public void onNiMAudio(byte[] buffer, int offset, int length, boolean isLastBuffer)
    {
    }

    public void onTimeout(AudioNode source) {
    }

    public void onSpeechStarted()
    {
    }

    public void onSpeechEnded()
    {
    }

    @Override
    public void onEvent(AudioNode eventOwner, NodeEvent event) {
    }

    @Override
    public void onAudio(AudioChunk chunk) {
    }

    @Override
    public void finish() {
    }
}
