package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.ResponseUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InterpretationExecutor extends Executor
{

    private static final String PARAMETERS = "parameters";
    private static final String APPSERVER_DATA = "appserver_data";

    public InterpretationExecutor(Session nmspSession, HashMap<String, Object> queryInfo, String webAPISessionID)
    {
        super(nmspSession, queryInfo, webAPISessionID);
    }

    public static final String WEBAPI_INTERPRETATION_CMD = "interpretation";
    public static JSONObject buildInterpretationCommand(Map<String, Object> queryInfo, Session nmspSession) {
        JSONObject parameters = new JSONObject();
//
//        if (!nmspSession.getWebAPIOrchestrator().getDynamicVocabulary().isEmpty()) {
//            parameters.put("activeDynamicVocabularySets", new String[]{UploadDynamicVocabularyTranslator.VOCABULARY_ID});
//        }

        PdxDictionary dict = (PdxDictionary) queryInfo.get("dict");

        parameters.put("dialogModelApplicationVersion", dict.get("application"));
        parameters.put("language", nmspSession.getDictationLanguage());
        parameters.put("dialogModelApplicationVersion", queryInfo.get("context_tag"));

        parameters.put("maxInterpretations", 1);
        var nlu_nbest_interpretations = (int) dict.getOrDefault("nlu_nbest_interpretations", 0);
        if(nlu_nbest_interpretations == 1){
            parameters.put("maxInterpretations", 10);
        }

        JSONObject command = new JSONObject();
        command.put("parameters", parameters);
        command.put("name", WEBAPI_INTERPRETATION_CMD);

        return command;
    }

    public void execute()
            throws IOException
    {
        JSONArray commands = new JSONArray();

        JSONObject command = buildInterpretationCommand(queryInfo, nmspSession);
        command.put("id", commandID);
        PdxDictionary dict = (PdxDictionary) queryInfo.get("dict");
        command.getJSONObject(PARAMETERS).put("data", ((PdxDictionary) dict.get(APPSERVER_DATA)).getOrDefault("text_input", ""));
        if (((PdxDictionary) dict.get(APPSERVER_DATA)).get("message") != null)
        {
            command.getJSONObject(PARAMETERS).put("data", ((PdxDictionary) dict.get(APPSERVER_DATA)).getOrDefault("message", ""));
        }
        command.getJSONObject(PARAMETERS).put("type", "text/plain");
        commands.put(command);

        command = EndSessionExecutor.buildEndSessionCommand(queryInfo, nmspSession);
        command.put("id", commandID);
        commands.put(command);

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "commandChain");
        webApiRequest.put("sessionId", getWebAPISessionID());
        webApiRequest.put("commands", commands);

        nmspSession.getWebAPIOrchestrator().sendToWebapi(this, webApiRequest.toString(), false);
    }

    public void onResponse(WebApiResponse responseJson)
            throws IOException
    {
        if (responseJson.isResult() && responseJson.isForCommand(WEBAPI_INTERPRETATION_CMD))
        {
            if (responseJson.isSuccess() && responseJson.isFinal())
            {
                doFinal(responseJson);
            }

            if (responseJson.isError())
            {
                ExtractedErrors errors =
                        extractWebApiErrors((ArrayList<HashMap<String, Object>>) responseJson.result.get("errors"));
                nmspSession.getWebAPIOrchestrator()
                           .sendError(nmspSession, ResponseUtils.buildError(nmspSession, errors.message, errors.code));
            }

            if (responseJson.isCanceled())
            {
                if (!isTerminated)
                {
                    nmspSession.getWebAPIOrchestrator()
                               .sendError(nmspSession, ResponseUtils.buildError(nmspSession, "Operation canceled",
                                       ResponseCodes.CompletionCause.CANCELLED));
                }
                else
                {
                    endWebapiSessionWithFinal(null); //just end webapi session
                }
            }
        }
    }

    protected void doFinal(WebApiResponse webApiResponse)
            throws IOException
    {
        endWebapiSessionWithFinal(
                NMSPResponseBuilder.buildPdxNLUResult(webApiResponse, queryInfo, nmspSession, getCommandName())
                                   .toByteArray());
    }

    @Override
    public void onBinary(byte[] data)
    {
        //there should be no Binary form webapi during ASR
    }

    @Override
    public void onClose(CloseReason closeReason)
    {
        //Do nothing
    }

    @Override
    public void cancelCommand()
    {
        sendDirective("cancel", WEBAPI_INTERPRETATION_CMD);
        isTerminated = true;
        terminateStreaming();
    }
}
