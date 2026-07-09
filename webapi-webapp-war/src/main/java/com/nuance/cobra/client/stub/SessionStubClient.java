package com.nuance.cobra.client.stub;

import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.SessionClient;
import com.nuance.cobra.client.response.EndSessionResponse;
import com.nuance.cobra.client.response.StartSessionResponse;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionStubClient
        extends SessionClient
{
    private static final Logger logger = LogManager.getLogger(SessionStubClient.class);

    public SessionStubClient(String sessionId)
    {
        super(sessionId);
    }

    @Override
    public void startSession(ResponseHandler responseHandler, CombinedConfiguration requestConfig)
    {
        logger.debug("Using stubbed startSession for session {}", sessionId);

        (new Thread(new ThreadContextRunnable() {
            @Override
            public void runWithContext() {
                responseHandler.onResponse(new StartSessionResponse(ResponseResult.ResponseStatus.SUCCESS, true));
            }
        })).start();
    }

    @Override
    public void endSession(ResponseHandler responseHandler, CombinedConfiguration requestConfig)
    {
        logger.debug("Using stubbed endSession for session {}", sessionId);

        (new Thread(new ThreadContextRunnable() {
            @Override
            public void runWithContext() {
                responseHandler.onResponse(new EndSessionResponse(ResponseResult.ResponseStatus.SUCCESS, true));
            }
        })).start();
    }


}
