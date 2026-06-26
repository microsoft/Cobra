package com.nuance.cobra.client.nim;

import com.nuance.cobra.client.SessionClient;
import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.json.JSONObject;

public class SessionNiMClient
        extends SessionClient
        implements NiMClientListener
{
    private String currentWapiCommand;

    public SessionNiMClient(String sessionId)
    {
        super(sessionId);
    }

    @Override
    public void startSession(ResponseHandler responseHandler,
            CombinedConfiguration requestConfig)
    {
    }

    @Override
    public void endSession(ResponseHandler responseHandler, CombinedConfiguration requestConfig)
    {
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

    public void setCurrentWapiCommand(String command)
    {
        this.currentWapiCommand = command;
    }

    public String getCurrentWapiCommand()
    {
        return currentWapiCommand;
    }
}
