package com.nuance.cobra.client.nim;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.client.SpeechSynthesisClient;
import com.nuance.cobra.client.response.SpeechSynthesisResponse;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;

public class VocalizerNimClient
        extends SpeechSynthesisClient
        implements NiMClientListener
{
    private final ResponseHandler responseHandler;
    private ClientState state;

    public VocalizerNimClient(String sessionId, CombinedConfiguration params,
            ResponseHandler responseHandler)
    {
        super(sessionId);
        this.responseHandler = responseHandler;
        setState(ClientState.IDLE);
    }

    protected void setState(ClientState state)
    {
        this.state = state;
    }

    protected ClientState getState()
    {
        return this.state;
    }

    @Override
    public void startSpeechSynthesis()
    {
        responseHandler.onResponse(new SpeechSynthesisResponse(
                ResponseCode.createResponseError(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR)));
    }

    @Override
    public void stopSpeechSynthesis()
    {
    }

    @Override
    public void cancelSpeechSynthesis(boolean isFinal)
    {
        responseHandler.onResponse(new SpeechSynthesisResponse(
                ResponseCode.createResponseError(ExternalResponseErrorCode.CANNOT_EXECUTE_DIRECTIVE_ERROR)));
    }

    @Override
    public boolean isSpeechSynthesisDone()
    {
        return (getState() == ClientState.DONE || getState() == ClientState.STOPPED);
    }

    public void onNiMEvent(short event)
    {
    }

    public void onNiMError(JSONObject result)
    {
    }

    public void onNiMResult(JSONObject result)
    {
    }

    public void onNiMRetry(JSONObject result)
    {
    }

    public void onNiMAudio(byte[] buffer, int offset, int length, boolean isLastBuffer)
    {
    }
}
