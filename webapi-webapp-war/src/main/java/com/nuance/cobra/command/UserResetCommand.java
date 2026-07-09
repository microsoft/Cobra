package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.EDFResponse;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.UserResetClient;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.response.CommandChainer;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * UserReset command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/user-reset-command.md
 * for documentation.
 */
public class UserResetCommand extends Command implements EDFResponse {

    protected static final Logger logger = LogManager.getLogger(UserResetCommand.class);

    private ClientFactory clientFactory = new ClientFactory();
    protected UserResetClient client;

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public UserResetCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer)
    {
        super(request, commandConfigs, chainer);
    }

    /**
     * Used in testing to replace the client factory by a mock.
     */
    public void setClientFactory(ClientFactory clientFactory)
    {
        this.clientFactory = clientFactory;
    }

    @Override
    protected void executeImplementation() {
        var session = SessionManager.getInstance().getSession(getSessionId(), true);
        session.incrementStepId();

        // Get all parameters for the command
        CombinedConfiguration requestConfig;
        try
        {
            requestConfig = session.getConfigWithMerge(request.requestCommand.parameters);
        }
        catch (IllegalArgumentException e)
        {
            var error = ResponseCode.createResponseError(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR, e.getMessage());
            getErrorHandler(logger).handleError(new ResponseResult(error));
            return;
        }

        String clientUserId = requestConfig.getString(WebApiConfig.Property.CLIENT_USER_ID.key);
        if(StringUtils.isEmpty(clientUserId) || clientUserId.equalsIgnoreCase("notDefined")) {
            var error = ResponseCode.createResponseError(ExternalResponseErrorCode.REQUEST_PARSE_ERROR, "Missing clientUserId");
            getErrorHandler(logger).handleError(new ResponseResult(error));
            return;
        }

        session.setCommand(this);

        try {
            client = clientFactory.createUserResetClient(commandConfigs, getSessionId(), getResponseHandler());
            if(client != null) {
                client.startUserReset();
            }
            else {
                sendResult(new ResponseResult());
            }
        }
        catch(Exception e) {
            logger.error("execute -- Exception", e);
            var error = ResponseCode.createResponseError(ExternalResponseErrorCode.USER_RESET_COMMAND_EXECUTE_ERROR);
            getErrorHandler(logger).handleError(new ResponseResult(error));
        }
    }

    /**
     * Check if the command is done then reset active command
     *
     * @param response The formatted response
     */
    @Override
    public boolean onResponseImplementation(ResponseResult response)
    {
        // Reset active command if the command is done, make sure first that client exists
        if (client != null)
        {
            resetSessionActiveCommand();
        }
        return true;
    }
}
