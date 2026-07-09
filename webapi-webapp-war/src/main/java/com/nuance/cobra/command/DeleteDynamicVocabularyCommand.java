package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.EDFResponse;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.session.DynamicVocabularyDb;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.response.CommandChainer;
import org.apache.commons.configuration2.CombinedConfiguration;

/**
 * DeleteDynamicVocabulary command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/delete-dynamic-vocabulary-command.md
 * for documentation.
 */
public class DeleteDynamicVocabularyCommand
        extends Command implements EDFResponse {

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public DeleteDynamicVocabularyCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer)
    {
        super(request, commandConfigs, chainer);
    }

    @Override
    protected void executeImplementation() {
        var session = SessionManager.getInstance().getSession(getSessionId(), true);
        session.incrementStepId();

        DynamicVocabularyDb.deleteUserFromDb(commandConfigs);

        ResponseResult results = new ResponseResult();
        getResponseHandler().onResponse(results);
    }
}
