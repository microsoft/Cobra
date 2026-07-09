package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DeleteDynamicVocabularyTranslator
        extends AbstractCommandTranslator
{
    public static final String WEBAPI_NAME = "deleteDynamicVocabulary";
    public static final String NCS_NAME = "NDSP_DELETE_ALL_CONCEPTS_DATA_CMD";

    public DeleteDynamicVocabularyTranslator(Orchestrator orchestrator, NcsRequest ncsRequest)
    {
        super(orchestrator, ncsRequest);
    }

    @Override
    public void execute() throws IOException
    {
        doRequest(ncsRequest);
        waitForResponse();
    }

    private void doRequest(NcsRequest ncsRequest) throws IOException
    {
        orchestrator.clearDynamicVocabulary();
        commandID = ncsRequest.transaction_id;

        String webapiCommandJsonString = String.format("{ \"type\":\"command\", \"command\":{ \"name\":\"%s\" } }", WEBAPI_NAME);
        JSONObject webapiCommandJson = new JSONObject(webapiCommandJsonString);
        webapiCommandJson.put("sessionId", orchestrator.getWebapiSessionID());
        webapiCommandJson.getJSONObject("command").put("id", commandID);

        sendToWebapi(orchestrator, webapiCommandJson.toString(), false);
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException
    {
        NcsResponse ncsResponse = new NcsResponse();

        if (webApiResponse.isSuccess()) {
            ncsResponse.message = "query_response";
        } else {
            AbstractCommandTranslator.ExtractedErrors err = AbstractCommandTranslator.extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
            ncsResponse.message = "query_error";
            ncsResponse.reason = err.message;
            ncsResponse.code = err.code;
        }
        ncsResponse.result_type = NCS_NAME;
        ncsResponse.NMAS_PRFX_SESSION_ID = orchestrator.getSessionId();
        ncsResponse.NMAS_PRFX_TRANSACTION_ID = (String) webApiResponse.command.get("id");
        ncsResponse.transaction_id = Integer.parseInt((String) webApiResponse.command.get("id"));

        respondToClient(orchestrator, ncsResponse.toJson());

        ncsResponse = new NcsResponse();
        ncsResponse.message = "query_end";
        ncsResponse.transaction_id = Integer.parseInt((String) webApiResponse.command.get("id"));

        respondToClient(orchestrator, ncsResponse.toJson());

        return doFinal(orchestrator, webApiResponse);
    }

    @Override
    public void commandCleanup()
    {
        if (streamer != null) {
            streamer.terminate(true);
        }
    }
}
