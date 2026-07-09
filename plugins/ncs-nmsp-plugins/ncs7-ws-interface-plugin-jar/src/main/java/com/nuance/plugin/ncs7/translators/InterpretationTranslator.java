package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class InterpretationTranslator extends AbstractCommandTranslator {
    public static final String NCS_NAME = "NDSP_APP_CMD";

    public InterpretationTranslator(Orchestrator orchestrator, NcsRequest ncsRequest) {
        super(orchestrator, ncsRequest);
    }

    @Override
    public void execute() throws IOException {
        doRequest(ncsRequest);
        waitForResponse();
    }

    protected void doRequest(NcsRequest ncsRequest) throws IOException {
        JSONObject command = WebApiCommandsBuilder.buildInterpretationCommand(orchestrator, ncsRequest);
        command.put("id", getCommandID(ncsRequest));

        HashMap<String, String> appData = (HashMap<String, String>) ncsRequest.dictionary.get("application_data");
        JSONObject parameters = command.getJSONObject("parameters");
        parameters.put("data", appData.get("text_input"));
        parameters.put("interpretationVerboseResult", true);
        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "command");
        webApiRequest.put("sessionId", orchestrator.getWebapiSessionID());
        webApiRequest.put("command", command);

        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        if (webApiResponse.isFinal()) {
            respondToClient(orchestrator, NCSResponseBuilder.nluFinalResponseBuilder(orchestrator, webApiResponse, NCS_NAME).toJson());
            sendQueryEnd(orchestrator, webApiResponse);

            return doFinal(orchestrator, webApiResponse);
        }
        return new ExecutionResult(ExecutionStatus.SUCCESS, false);
    }

    @Override
    public void commandCleanup() {
        if (streamer != null) {
            streamer.terminate(true);
        }
    }
}
