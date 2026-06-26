package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import static com.nuance.plugin.ncs7.translators.NCSResponseBuilder.buildBasicResponse;

public class SessionDeleteTranslator extends AbstractCommandTranslator {
    public static final String NCS_NAME = "NVC_DELETE_SESSION_DATA_CMD";

    public SessionDeleteTranslator(Orchestrator orchestrator, NcsRequest ncsRequest) {
        super(orchestrator, ncsRequest);
    }

    @Override
    public void execute() throws IOException {
        doRequest(ncsRequest);
        waitForResponse();
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        if (webApiResponse.isFinal()) {
            NcsResponse ncsResponse = buildBasicResponse(orchestrator, webApiResponse, NCS_NAME);
            ncsResponse.message = "query_response";

            respondToClient(orchestrator, ncsResponse.toJson());
            sendQueryEnd(orchestrator, webApiResponse);

            return doFinal(orchestrator, webApiResponse);
        }
        return new ExecutionResult(ExecutionStatus.SUCCESS, false);
    }

    protected void doRequest(NcsRequest ncsRequest) throws IOException {
        JSONObject webApiRequest = WebApiCommandsBuilder.buildBasicCommand("sessionDelete", orchestrator.getWebapiSessionID(), getCommandID(ncsRequest));

        JSONArray sessionsToDelete = new JSONArray();
        sessionsToDelete.put(ncsRequest.data_session_id);
        webApiRequest.getJSONObject("command").getJSONObject("parameters").put("sessions", sessionsToDelete);
        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }
}
