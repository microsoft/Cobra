package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.client.response.EndSessionResponse;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * EndSessionCommand command. This command will terminate the session.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/end-session-command.md
 * for documentation.
 */
public class EndSessionCommand
        extends Command
        implements EDFResponse
{
    private static final Logger logger = LogManager.getLogger(EndSessionCommand.class);
    private boolean sessionIsAlreadyEnded = false;

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public EndSessionCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer) {
        super(request, commandConfigs, chainer);
    }

    /**
     * Starts the command
     */
    @Override
    public void executeImplementation()
    {
        logger.trace("EndSessionCommand executeImplementation - sessionId: {}", request.sessionId);
        final var sessionManager = SessionManager.getInstance();

        final var session = sessionManager.getSession(getSessionId(), false);
        currentSession = session;

        if (session == null)
        {
            logger.info("Session doesn't exist, cannot end the session, sessionId is : {}", request.sessionId);
            var endSessionResponse = new EndSessionResponse(ResponseCode.createResponseError(
                    ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR));
            getResponseHandler().onResponse(endSessionResponse);
            return;
        }
        sessionIsAlreadyEnded = session.isSessionEnded();
        session.cancelActiveCommand("End session executed");
        session.setCommand(this);

        //Creation of the client factory
        var clientFactory = getClientFactory();

        CombinedConfiguration requestConfig = session.getConfigWithMerge(request.requestCommand.parameters);
        var sessionClient = clientFactory.createSessionClient(requestConfig, request.sessionId);
        SessionManager.getInstance().getSessionEncryptionStateMap().remove(getSessionId());
        sessionClient.endSession(getResponseHandler(), requestConfig);
    }

    /**
     * Implementation of the receiver because the endSession is a 2 steps
     * command where it needs to end the session with Nina mobile platform
     * but also with sessionManager. Implementing second step here in the receiver.
     *
     * @param response
     *         The formatted response received from the client
     */
    @Override
    public boolean onResponseImplementation(ResponseResult response)
    {
        logger.trace("EndSessionCommand onResponse");
        final var sessionManager = SessionManager.getInstance();

        terminateCommand();

        if (!sessionIsAlreadyEnded && sessionManager.endSession(request.sessionId))
        {
            logger.debug("Session ended properly");
        }
        else
        {
            logger.info("Couldn't end the session with sessionManager. Session was likely already ended.");
        }
        return true;
    }

    /**
     * Creates an EndSession request for session that have timed out. The command
     * will take care of the necessary clean up
     *
     * @param sessionId
     *         The identifier of the session to end
     *
     * @return The Request
     */
    public static Request createEndSessionRequest(String sessionId)
            throws CobraException
    {
        logger.trace("EndSessionCommand createEndSessionRequest");

        JSONObject jsonRequest = new JSONObject()
                .put("type", "command")
                .put("sessionId", sessionId)
                .put("command", new JSONObject()
                        .put("name", "endSession")
                        .put("id", "1001"));

        var result = Request.fromJson(jsonRequest.toString());
        var session = SessionManager.getInstance()
                .getSession(sessionId, false);
        if(session == null)
        {
            return null;
        }
        result.tenantKey = session.getSessionConfig()
                .getString(WebApiConfig.Property.APPLICATION_KEY.key);

        return result;
    }

    @Override
    public CommandLog formatLog(IResponse response)
    {
        //For EndSession command, we don't call the base method because of funny business
        CommandLog baseResponse = super.formatLog(response);

        logger.debug("Attempting to format EDF Response for EndSessionCommand...");


        /*
        We need to do this to pretty-print the EDF response without duplicate parameters
         */
        baseResponse.request.response = prettyFormatResponse((Response)response);


        try {
            var baseResponseString = baseResponse.toJson(false);
            logger.debug("{}--EDF Response : {} ", getCommandName(), baseResponseString);
        } catch (JsonProcessingException e) {
            logger.info("Unable to log baseResponse", e);
        }
        return baseResponse;
    }

    @Override
    public String formatResponse(Object objectResponse)
            throws CobraException
    {
        logger.trace("EndSessionCommand formatResponse - sessionId: {}", request.sessionId);
        String rc = super.formatResponse(objectResponse);
        CobraConfigurationUtils.clearCombinedConfiguration(commandConfigs, request.sessionId);
        return rc;
    }


}
