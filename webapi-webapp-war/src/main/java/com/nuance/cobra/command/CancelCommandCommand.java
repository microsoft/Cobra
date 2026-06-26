package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestDirective;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CancelCommand command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/cancel-command-command.md
 * for documentation.
 */
public class CancelCommandCommand
        extends Command
        implements EDFResponse
{
    private static final Logger logger = LogManager.getLogger(CancelCommandCommand.class);

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public CancelCommandCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer) {
        super(request, commandConfigs, chainer);
    }

    @Override
    protected void executeImplementation() {
        final var sessionManager = SessionManager.getInstance();
        final var session = sessionManager.getSession(request.sessionId, true);

        if (null == session)
        {
            sendErrorResponse(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR));
            return;
        }
        session.incrementStepId();

        var currentCommand = session.getCommand();

        if (currentCommand != null) {
            currentCommand.cancel("Cancel command received");
        }

        var cancelResponse = new ResponseResult();
        getResponseHandler().onResponse(cancelResponse);
    }

    @Override
    public CommandLog formatLog(IResponse response)
    {
        CommandLog baseResponse = super.formatLog(response);

        logger.debug("Attempting to format EDF Response for CancelCommandCommand...");

        // We need to do this to pretty-print the EDF response without duplicate parameters
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
    protected void onDirectiveImplementation(RequestDirective requestDirective, boolean isFinal) {
        // Nothing to do
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(ResponseCode error)
    {
        getErrorHandler(logger).handleError(new ResponseResult(null, error));
    }
}
