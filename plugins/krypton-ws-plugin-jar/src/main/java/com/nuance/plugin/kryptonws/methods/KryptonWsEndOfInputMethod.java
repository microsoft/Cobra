package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.commandbuilder.WebapiStopDirectiveBuilder;
import com.nuance.plugin.kryptonws.response.KryptonWsEvent;
import com.nuance.plugin.kryptonws.response.KryptonWsEventBuilder;
import com.nuance.plugin.kryptonws.response.KryptonWsMessage;
import com.nuance.plugin.kryptonws.response.KryptonWsResponseBuilder;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class KryptonWsEndOfInputMethod
        extends KryptonWsMethod
{
    KryptonWsRecognizeMethod recognizeMethod;

    private KryptonWsEventBuilder recognizeFinalResultBuilder;
    protected Calendar interpretationStartTime;

    private static final Logger logger = LogManager.getLogger();
    public static final String NAME = "EndOfInput";

    protected KryptonWsEndOfInputMethod(JSONObject jsonObject, KryptonWsSession kryptonWsSession) {
        super(jsonObject, kryptonWsSession);
        logger.debug("KryptonWsEndOfInputMethod constructor");
    }

    @Override
    protected void onAudioImplementation(byte[] audio) {
        // Ignored
    }

    @Override
    public void process() {
        super.process();

        KryptonWsMethod currentMethod = kryptonWsSession.getWebapiResponseListener().getCurrentMethod();
        if (currentMethod instanceof KryptonWsRecognizeMethod)
        {
            recognizeMethod = (KryptonWsRecognizeMethod) currentMethod;
            recognizeMethod.stopRecognitionTimer();

            // if recognition was NOT stopped
            if (recognizeMethod.recognitionWasStopped.compareAndSet(false, true))
            {
                WebapiStopDirectiveBuilder webapiStopDirectiveBuilder =
                        new WebapiStopDirectiveBuilder(
                                kryptonWsSession.getWebapiSessionId(),
                                kryptonWsSession
                                        .getWebapiSession()
                                        .getCommand()
                                        .getCommandId(),
                                "speechRecognition");

                JSONObject webapiRequest = webapiStopDirectiveBuilder.buildRequest();

                String formattedRequest = webapiRequest.toString(4);
                logger.info("Sending Stop Directive to webapi:\n{}", formattedRequest);

                sendToWebapi(webapiRequest.toString());
            }
        }
        else
        {
            sendNoActionResponse();
        }
    }

    @Override
    public void processWebapiResponse(String webapiResponse)
    {
        logger.info("Got webapi response: {}", webapiResponse);

        WebApiResponse responseObject;

        try
        {
            responseObject = WebApiResponse.fromJson(webapiResponse);
        }
        catch (JsonProcessingException e)
        {
            String errorMessage = "Unable to parse webapi response json";
            logger.warn("{}: {}", errorMessage, webapiResponse);
            sendErrorResponse(errorMessage, null);
            cleanup();
            return;
        }

        if (responseObject.command.get("name").equals("interpretation"))
        {
            if (responseObject.isResult() && responseObject.isFinal())
            {
                handleInterpretationResponse(webapiResponse, responseObject);
            }
            return;
        }

        if (responseObject.isError())
        {
            JSONObject resultJson = new JSONObject(responseObject.result);

            // KryptonWs handles a "no active command" by sending "no-action"
            if (isNoActiveCommand(resultJson))
            {
                sendResponse(KryptonWsMessage.STATUS.NO_ACTION);
            }
            else
            {
                List<Object> errors = resultJson.getJSONArray("errors").toList();
                errors.forEach(error -> kryptonWsResponseBuilder.addError((String) ((Map<Object, Object>) error).get("message"), null));
                sendResponse(KryptonWsMessage.STATUS.FAILED);
            }
            cleanup();
            return;
        }
        else if (responseObject.isCanceled())
        {
            sendResponse(KryptonWsMessage.STATUS.FAILED);
            cleanup();
            return;
        }
        else
        {
            kryptonWsResponseBuilder.setStatus(KryptonWsMessage.STATUS.COMPLETE);
        }

        String eventMethodName = "Event";

        if (responseObject.isFinal())
        {
            KryptonWsEventBuilder finalResultEventBuilder = new KryptonWsEventBuilder()
                    .setMethod(eventMethodName)
                    .setEvent(KryptonWsEvent.EVENT.FINAL_RESULT)
                    .setVersion("1.0")
                    .setSessionId(this.getKryptonWsSessionId())
                    .setRequestId(this.getKryptonWsRequestId())
                    .setStatus(KryptonWsMessage.STATUS.COMPLETE);

            // Do nothing on no input and no match
            if (responseObject.isSuccess())
            {
                if (kryptonWsSession.getWebapiSessionConfig().getBoolean("autoEnd", true))
                {
                    finalResultEventBuilder.setObjectsFromWebapiResponse(responseObject, true);
                }
                else
                {
                    sendPartialResult(responseObject, true);
                }
                finalResultEventBuilder.setCause(KryptonWsMessage.COMPLETION_CAUSE.END_OF_STREAM);

                JSONObject semanticModel = kryptonWsSession.getSemanticModel();
                if (semanticModel != null) // if should interpret
                {
                    recognizeFinalResultBuilder = finalResultEventBuilder;
                    KryptonWsInterpretMethod interpretMethod = KryptonWsRecognizeMethod.createInterpretMethod(kryptonWsSession, recognizeMethod, responseObject, semanticModel);
                    interpretationStartTime = getCurrentUTCTime();
                    interpretMethod.process(this);
                    return;
                }
            }
            setCompletionCauseNoAudio(finalResultEventBuilder, responseObject.isNoInput());

            sendFinalResultAndResponse(responseObject, finalResultEventBuilder);
        }
        else if (responseObject.isSuccess())
        {
            sendPartialResult(responseObject, false);
        }
    }

    private void setCompletionCauseNoAudio(KryptonWsEventBuilder finalResultEventBuilder, boolean hasNoAudio){
        if(hasNoAudio){
            finalResultEventBuilder.setCause(KryptonWsMessage.COMPLETION_CAUSE.END_OF_STREAM);
        }
    }

    private void sendFinalResultAndResponse(WebApiResponse responseObject, KryptonWsEventBuilder finalResultEventBuilder)
    {
        sendCompleteResponse();

        sendEvent(responseObject, finalResultEventBuilder);

        KryptonWsResponseBuilder recognizeResponseBuilder = new KryptonWsResponseBuilder(KryptonWsRecognizeMethod.NAME, this.getKryptonWsSessionId(), this.getKryptonWsRequestId())
                .setVersion("1.0")
                .setStatus(KryptonWsMessage.STATUS.COMPLETE);
        if (areMetricsEnabled)
        {
            recognizeResponseBuilder.setMetrics(recognizeMethod.buildCommonMetrics());
        }
        kryptonWsSession.getKryptonWsResponseSender().sendResponse(recognizeResponseBuilder.build().toJson());
        cleanup();
    }

    private void handleInterpretationResponse(String webapiResponse, WebApiResponse responseObject)
    {
        KryptonWsEventBuilder kryptonWsEventBuilder = KryptonWsRecognizeMethod
                .populateRecognizeEventWithInterpretationResult(
                        webapiResponse,
                        responseObject,
                        recognizeFinalResultBuilder,
                        areMetricsEnabled,
                        interpretationStartTime);

        sendFinalResultAndResponse(responseObject, kryptonWsEventBuilder);
    }

    private void sendPartialResult(WebApiResponse responseObject, boolean isUtteranceComplete) {
        KryptonWsEventBuilder partialResultEventBuilder = new KryptonWsEventBuilder()
                .setCause(KryptonWsMessage.COMPLETION_CAUSE.SUCCESS)
                .setObjectsFromWebapiResponse(responseObject, false)
                .setStatus(KryptonWsMessage.STATUS.IN_PROGRESS)
                .setUtteranceComplete(isUtteranceComplete)
                .setEvent(KryptonWsEvent.EVENT.PARTIAL_RESULT)
                .setMethod("Event")
                .setRequestId(this.getKryptonWsRequestId())
                .setSessionId(this.getKryptonWsSessionId())
                .setVersion("1.0");
        sendEvent(responseObject, partialResultEventBuilder);
    }

    private void sendEvent(WebApiResponse webApiResponse, KryptonWsEventBuilder eventBuilder) {
        recognizeMethod.addEventMetrics(webApiResponse, eventBuilder, true);

        kryptonWsSession.getKryptonWsResponseSender().sendResponse(eventBuilder.build().toJson());
    }

    /**
     * @param webapiResponseJson
     *              The received Webapi Response as a JsonObject
     * @return Boolean checking if there is an active SpeechReco command
     */
    private boolean isNoActiveCommand(JSONObject webapiResponseJson){
        try {
            return webapiResponseJson
                    .getJSONArray("errors")
                    .getJSONObject(0)
                    .getString("message")
                    .equalsIgnoreCase("no active command.");
        }catch (JSONException jsonException){
            return false;
        }
    }

    @Override
    public boolean canRunWhile(KryptonWsMethod commandThatIsAlreadyRunning)
    {
        if (commandThatIsAlreadyRunning == null)
        {
            return true;
        }
        else
        {
            return commandThatIsAlreadyRunning.getMethodName().equals(KryptonWsRecognizeMethod.NAME);
        }
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
