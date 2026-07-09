package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import com.nuance.plugin.ncs7.json.NcsResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class UploadDynamicVocabularyTranslator extends AbstractCommandTranslator {
    public static final String WEBAPI_NAME = "uploadDynamicVocabulary";
    public static final String NCS_NAME = "NDSP_CONCEPT_UPLOAD_FULL_CMD";
    public static final String INCREMENTAL_NCS_NAME = "NDSP_CONCEPT_UPLOAD_INCREMENTAL_CMD";
    public static final String VOCABULARY_ID = "CONTENT_DATA";
    public static final String LITERAL = "literal";

    private String commandName;

    public UploadDynamicVocabularyTranslator(Orchestrator orchestrator, NcsRequest ncsRequest) {
        super(orchestrator, ncsRequest);
    }

    @Override
    public void execute() throws IOException
    {
        doRequest(ncsRequest);
        waitForResponse();
    }

    private void doRequest(NcsRequest ncsRequest)
            throws IOException
    {
        JSONObject parameters = new JSONObject();

        parameters.put("dynamicVocabularies", new JSONArray().put(createDictionaryBody(ncsRequest)));

        JSONObject command = new JSONObject();
        command.put("parameters", parameters);
        command.put("name", WEBAPI_NAME);
        command.put("id", getCommandID(ncsRequest));

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "command");
        webApiRequest.put("sessionId", orchestrator.getWebapiSessionID());
        webApiRequest.put("command", command);

        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }

    private JSONObject createDictionaryBody(NcsRequest ncsRequest) {
        String conceptId = ncsRequest.concept_id;

        JSONObject vocabulary = new JSONObject();
        JSONObject body = new JSONObject();
        JSONArray entries = new JSONArray();
        body.put(conceptId, entries);

        commandName = ncsRequest.command;

        if (ncsRequest.command.equalsIgnoreCase(NCS_NAME)) {
            ArrayList<Map<String, String>> appData = (ArrayList<Map<String, String>>) ncsRequest.dictionary.get("items");

            Map<String, String> extractedData = new HashMap<>();
            for (Map<String, String> inputEntry : appData) {
                String key = inputEntry.get(LITERAL);
                extractedData.put(key, inputEntry.get("value"));
            }

            extractedData.keySet().forEach(literal -> buildWebapiEntry(entries, literal, extractedData.get(literal)));

        } else if (ncsRequest.command.equalsIgnoreCase(INCREMENTAL_NCS_NAME)) {
            String action = ((String)ncsRequest.dictionary.getOrDefault("action", "add")).toLowerCase();
            vocabulary.put("incremental_action", action);

            Object canonical = ncsRequest.dictionary.get("value");
            buildWebapiEntry(entries, ncsRequest.dictionary.get(LITERAL), canonical);
        }

        vocabulary.put("id", VOCABULARY_ID);
        vocabulary.put("type", "application/x-nuance-wordset");
        vocabulary.put("body", body);
        vocabulary.put("persist", true);
        return vocabulary;
    }

    private void buildWebapiEntry(JSONArray entries, Object literal, Object canonical) {
        JSONObject entry = new JSONObject();
        entry.put(LITERAL, literal);
        entry.put("canonical", canonical);
        entries.put(entry);
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        NcsResponse ncsResponse = new NcsResponse();

        if (webApiResponse.isFinal()) {
            if (webApiResponse.isSuccess()) {
                ncsResponse.message = "query_response";
            } else {
                AbstractCommandTranslator.ExtractedErrors err = AbstractCommandTranslator.extractWebApiErrors((ArrayList<HashMap<String, Object>>) webApiResponse.result.get("errors"));
                ncsResponse.message = "query_error";
                ncsResponse.reason = err.message;
                ncsResponse.code = err.code;
            }
            ncsResponse.result_type = commandName;
            ncsResponse.NMAS_PRFX_SESSION_ID = orchestrator.getSessionId();
            ncsResponse.NMAS_PRFX_TRANSACTION_ID = (String) webApiResponse.command.get("id");
            ncsResponse.transaction_id = Integer.parseInt((String) webApiResponse.command.get("id"));

            respondToClient(orchestrator, ncsResponse.toJson());

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
