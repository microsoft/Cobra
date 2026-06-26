package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.EDFResponse;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.response.CommandChainer;
import org.apache.commons.configuration2.CombinedConfiguration;

/**
 * SessionDelete command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/session-delete-command.md
 * for documentation.
 */
public class SessionDeleteCommand extends Command implements EDFResponse {

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public SessionDeleteCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer)
    {
        super(request, commandConfigs, chainer);
    }

    @Override
    protected void executeImplementation() {
        var session = SessionManager.getInstance().getSession(getSessionId(), true);
        session.incrementStepId();

        ResponseResult results = new ResponseResult();
        getResponseHandler().onResponse(results);
    }
}
