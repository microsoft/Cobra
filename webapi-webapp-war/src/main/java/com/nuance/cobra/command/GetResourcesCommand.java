package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.EDFResponse;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseStatistics;
import com.nuance.cobra.client.response.ResourcesResponse;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.response.CommandChainer;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * GetResources command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/get-resources-command.md
 * for documentation.
 */
public class GetResourcesCommand extends Command implements EDFResponse {

    private static final Logger logger = LogManager.getLogger(GetResourcesCommand.class);

    public interface IResources {
        String getName();

        Map<String, Object> getResourcesMap();
    }

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public GetResourcesCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer)
    {
        super(request, commandConfigs, chainer);
    }

    @Override
    protected void executeImplementation() {
        try {
            var session = SessionManager.getInstance().getSession(getSessionId(), true);
            CombinedConfiguration requestConfig = session.getSessionConfig();
            session.incrementStepId();

            var resourcesResponse = new ResourcesResponse(requestConfig);

            resourcesResponse.setStatistics(new ResponseStatistics(getStatistics()));

            getResponseHandler().onResponse(resourcesResponse);
        }catch (Exception e){
            logger.error("There was an exception getting resources. \n {}", e.getMessage());
        }
    }

    @Override
    public String formatResponse(Object objectResponse) throws CobraException {
        var response = Response.fromRequest(request);
        response.setResult((ResourcesResponse)objectResponse);

        return response.toJson();
    }
}
