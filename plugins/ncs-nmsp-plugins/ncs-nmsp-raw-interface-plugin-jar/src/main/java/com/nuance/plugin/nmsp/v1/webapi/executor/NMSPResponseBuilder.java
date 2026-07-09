package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.nuance.cobra.utils.encryption.Utils;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.messages.PDXQueryResult;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.Dictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.JsonPDXConverter;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXSequence;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NMSPResponseBuilder
{
    private static final Logger logger = LogManager.getLogger();

    private static final String REJECTED = "rejected";
    private static final String CONFIDENCE = "confidence";
    private static final String WORDS = "words";
    private static final String RESULT_FORMAT = "result_format";
    private static final String FINAL_RESPONSE = "final_response";
    private static final String NMAS_PRFX_SESSION_ID = "NMAS_PRFX_SESSION_ID";
    private static final String NMAS_PRFX_TRANSACTION_ID = "NMAS_PRFX_TRANSACTION_ID";
    private static final String RESULT_TYPE = "result_type";
    private static final String APPSERVER_RESULTS = "appserver_results";
    private static final String PAYLOAD = "payload";
    private static final String DIAGNOSTIC_INFO = "diagnostic_info";
    private static final String CONTEXT_TAG = "context_tag";
    private static final String INTERPRETATIONS = "interpretations";
    private static final String LITERAL = "literal";
    private static final String ACTION = "action";
    private static final String INTENT = "intent";
    private static final String VALUE = "value";
    private static final String CONCEPTS = "concepts";
    private static final String CONCEPT = "concept";

    private NMSPResponseBuilder() {

    }

    public static PDXQueryResult buildPdxASRResult(WebApiResponse webApiResponse, Map<String, Object> queryInfo, Session nmspSession, String commandName, boolean finalResult, double confidenceTuning, boolean confidenceOverwrite) throws IOException {
        PDXDictionary response = createGenericRespondFields(nmspSession, commandName, finalResult);

        //add transcripts
        if (isBinaryResult((PdxDictionary) queryInfo.get("dict"))) {
            buildBinaryTranscriptionResult(nmspSession, response, (HashMap) webApiResponse.result.get("verboseResult"), confidenceTuning, confidenceOverwrite);
        } else {
            buildClearTextTranscriptionResult(response, (HashMap) webApiResponse.result.get("verboseResult"), confidenceTuning, confidenceOverwrite);
        }


        PDXQueryResult result = new PDXQueryResult(response.toByteArray());
        logger.debug("====== PDX Query Result {}", result);
        return result;
    }

    private static boolean isBinaryResult(PdxDictionary dict) {
        return dict != null && dict.get("binary_results") != null && (int) dict.get("binary_results") != 0;
    }

    @SuppressWarnings({"java:S125", "java:S3776"})
    private static void buildBinaryTranscriptionResult(Session nmspSession, PDXDictionary response, Map<String, Object> responseData, double confidenceTuning, boolean confidenceOverwrite) throws IOException {
        @SuppressWarnings("java:S1481")
        final int WORD_TIMINGS = 1;
        final int SENTENCES_CONFIDENCES = 3;
        final int ENCODING_EXTENSION_ID = 4;

        ByteArrayOutputStream resultByteArray = new ByteArrayOutputStream();
        ResponseUtils.ReverseEndianDataOutputStream resultOutputStream = new ResponseUtils.ReverseEndianDataOutputStream(resultByteArray);

        List<Object> wordsList;
        wordsList = (List) responseData.get("hypotheses");

        int count = wordsList.size();

        List<HashMap<String, Object>> transcriptsList = new ArrayList<>();
        List<List<Short>> confidenceExtension = new ArrayList<>();
        int confidenceExtensionSize = 4; //start with the size of number of sentances

        List<String> uniqueWords = new ArrayList<>();
        for (Object wordsMapList : wordsList.subList(0, count)) {
            HashMap<String, Object> transcriptMap = (HashMap<String, Object>) wordsMapList;
            if (transcriptMap.containsKey(REJECTED) && (boolean)transcriptMap.get(REJECTED)) {
                continue;
            }

            Double utteranceConfidence = Math.max((double)transcriptMap.getOrDefault(CONFIDENCE, 0.0), (double)transcriptMap.getOrDefault("averageConfidence", 0.0));
            utteranceConfidence = remapConfidence(utteranceConfidence * 1000, confidenceTuning); //remap confidence to the 0 1000;
            transcriptMap.put(CONFIDENCE, utteranceConfidence);

            transcriptsList.add(transcriptMap);
            List<Short> wordConfidence = new ArrayList<>();
            confidenceExtension.add(wordConfidence);
            confidenceExtensionSize += 4; //size for number of words
            for (Object wordInList : (List) transcriptMap.get(WORDS)) {
                HashMap<String, Object> wordDict = (HashMap<String, Object>) wordInList;
                String word = (String) wordDict.get("text");
                if (wordDict.get(CONFIDENCE) != null) {
                    double confidence =remapConfidence(((Double) wordDict.get(CONFIDENCE) * 1000), confidenceTuning); //binary format wants confidence in 0~1000 range.
                    if (confidenceOverwrite) {
                        confidence = utteranceConfidence;
                    }

                    wordConfidence.add((short) confidence);
                } else {
                    wordConfidence.add((short) 0);
                }
                confidenceExtensionSize += 2; //2 bytes for each short value

                if (!uniqueWords.contains(word)) {
                    uniqueWords.add(word);
                }
            }
        }

        resultOutputStream.reverseWriteShort((short) uniqueWords.size()); //number of unique words
        for (String word : uniqueWords) {
            resultOutputStream.write(word.getBytes(StandardCharsets.UTF_8));
            resultOutputStream.writeByte(0); //null separated words.
        }

        resultOutputStream.reverseWriteInt(transcriptsList.size()); //number of transcriptions
        for (HashMap<String, Object> transcription : transcriptsList) {
            List<HashMap<String, Object>> words = (List<HashMap<String, Object>>) transcription.get(WORDS);
            resultOutputStream.reverseWriteInt(words.size());
            for (HashMap<String, Object> wordInList : words) {
                String word = (String) wordInList.get("text");
                resultOutputStream.reverseWriteShort((short) (uniqueWords.indexOf(word) + 1)); //1 based list
                resultOutputStream.reverseWriteShort(((Double) wordInList.get("startMs")).shortValue()); //word Start Time
                resultOutputStream.reverseWriteShort(((Double) wordInList.get("endMs")).shortValue()); //Word end time
            }
        }

        //build Key-Value pair
        HashMap<String, String> keyValuePair = new HashMap<>();
        HashMap<String, Object> utteranceInfo = (HashMap<String, Object>) responseData.get("utteranceInfo");
        if (utteranceInfo != null) {
            HashMap<String, Object> dsp = (HashMap<String, Object>) utteranceInfo.get("dsp");
            keyValuePair.put("SNR", String.valueOf(dsp.get("snrEstimateDb")));
            keyValuePair.put("initial-energy", String.valueOf(dsp.get("initialEnergy")));
            keyValuePair.put("final-energy", String.valueOf(dsp.get("finalEnergy")));
            keyValuePair.put("mean-energy", String.valueOf(dsp.get("meanEnergy")));
            keyValuePair.put("initial-silence", String.valueOf(dsp.get("initialSilenceMs")));

            keyValuePair.put("clipping", String.valueOf(utteranceInfo.get("clippingDurationMs")));
            keyValuePair.put("dpversion", "WebAPI_plugin");
            keyValuePair.put("utterance-length", String.valueOf(utteranceInfo.get("durationMs")));
            keyValuePair.put("utterance-number", String.valueOf(nmspSession.getWebAPIOrchestrator().getAudioId()));
            keyValuePair.put("utterance-offset", "0");
        }

        int i = 0;
        for (HashMap<String, Object> transcription : transcriptsList) {
            Double confidence = (double)transcription.getOrDefault(CONFIDENCE, 0.0);
            keyValuePair.put("CFD" + i, String.valueOf(confidence));
            ++i;
        }

        //write key-value pairs into the stream.
        resultOutputStream.reverseWriteShort((short) keyValuePair.size());
        for (Map.Entry<String, String> entry : keyValuePair.entrySet()) {
            resultOutputStream.writeBytes(entry.getKey() + "=" + entry.getValue());
            resultOutputStream.writeByte(0); //null separated list.
        }

//        //encode Extensions
//        //SENTENCES_CONFIDENCES extension ID=3
        resultOutputStream.reverseWriteInt(SENTENCES_CONFIDENCES);
        resultOutputStream.reverseWriteInt(confidenceExtensionSize);
        resultOutputStream.reverseWriteInt(count); //number of sentences
        for (List<Short> sentenceConfList: confidenceExtension) {
            resultOutputStream.reverseWriteInt(sentenceConfList.size());
            for (Short conf : sentenceConfList) {
                resultOutputStream.reverseWriteShort(conf);
            }
        }

        // SENTENCES_CONFIDENCES extension ID=4
        resultOutputStream.reverseWriteInt(ENCODING_EXTENSION_ID);
        resultOutputStream.reverseWriteInt("UTF-8".length() + 1); //empty extension. I have no idea why we need the +1
        resultOutputStream.writeBytes("UTF-8"); //empty extension

        resultOutputStream.flush();
        response.addByteString("transcription", ByteBuffer.wrap(resultByteArray.toByteArray()).order(ByteOrder.BIG_ENDIAN).array());
        response.addUTF8String(RESULT_FORMAT, "rec_binary_results");
    }

    @SuppressWarnings("java:S125")
    private static void buildClearTextTranscriptionResult(PDXDictionary response, Map<String, Object> responseData, double confidenceTuning, boolean confidenceOverwrite) {
        List<Object> hypotheses = (List) responseData.get("hypotheses");

        int count = hypotheses.size();
        PDXSequence pdxTranscriptions = new PDXSequence();
        PDXSequence pdxConfidences = new PDXSequence();
        PDXSequence pdxWords = new PDXSequence();

        for (int i = 0; i < count; ++i) {
            Map<String, Object> entry = (HashMap) hypotheses.get(i);
            if (entry.containsKey(REJECTED) && (boolean)entry.get(REJECTED)) {
                continue;
            }

            pdxTranscriptions.addUTF8String((String) entry.getOrDefault("formattedText", ""));

            Double confidence = Math.max((double)entry.getOrDefault(CONFIDENCE, 0.0), (double)entry.getOrDefault("averageConfidence", 0.0));
            confidence = remapConfidence(confidence * 1000, confidenceTuning); //remap confidence to the 0 1000;
            pdxConfidences.addInteger(confidence.intValue());

            PDXSequence wordsSequence = new PDXSequence();
            List<Object> words = (List) entry.get(WORDS);
            for (Object wordInList : words) {
                HashMap<String, Object> wordDict = (HashMap<String, Object>) wordInList;
                PDXDictionary word = new PDXDictionary();

                Double wordConfidence;
                if (confidenceOverwrite) {
                    wordConfidence = confidence / 1000;
                } else {
                    wordConfidence =(double) wordDict.getOrDefault(CONFIDENCE, 0.0);
                    wordConfidence = remapConfidence(wordConfidence * 1000, confidenceTuning) / 1000;
                }

                word.addUTF8String(CONFIDENCE, String.valueOf(wordConfidence));
                word.addUTF8String("word", (String) wordDict.get("text"));
                wordsSequence.addDictionary(word);
            }
            pdxWords.addSequence(wordsSequence);
        }

        response.addSequence("transcriptions", pdxTranscriptions);
        response.addSequence("confidences", pdxConfidences);
        response.addSequence(WORDS, pdxWords);
        response.addUTF8String(RESULT_FORMAT, "rec_text_results");
    }

    private static Double remapConfidence(double confidence, double confidenceTuning) {
        if (confidenceTuning != 0) {
            double weight = (1000 - confidence);
            double result = confidence + (confidenceTuning * weight);
            return Math.min(Math.max(result, 0), 1000);
        } else {
            return confidence;
        }
    }

    public static PDXQueryResult buildPdxASREmptyResponseResult(Session nmspSession, String commandName) {
        PDXDictionary response = createGenericRespondFields(nmspSession, commandName, true);
        response.addUTF8String(RESULT_FORMAT, "empty_final_response");
        response.addSequence("transcriptions", new PDXSequence());

        PDXQueryResult result = new PDXQueryResult(response.toByteArray());
        logger.debug("====== PDX Query Result {}", result);

        return result;
    }

    private static PDXDictionary createGenericRespondFields(Session nmspSession, String commandName, boolean finalResult) {
        PDXDictionary response = new PDXDictionary();
        response.addUTF8String("_resultType", "NmspQuery");
        response.addUTF8String("_status", "result");
        response.addInteger(FINAL_RESPONSE, finalResult ? 1 : 0);
        response.addUTF8String("cadence_regulatable_result", finalResult ? "completeRecognition" : "partialRecognition");

        response.addUTF8String(NMAS_PRFX_SESSION_ID, nmspSession.getSessionIdString());
        response.addUTF8String(NMAS_PRFX_TRANSACTION_ID, String.valueOf(nmspSession.getWebAPIOrchestrator().getTransactionID()));
        response.addUTF8String(RESULT_TYPE, commandName);
        response.addUTF8String("prompt", ""); //this is a field that the client looks for, and fails if it is not there
        response.addInteger("status_code", ResponseCodes.CompletionCause.SUCCESS.getCause());
        response.addDictionary("audio_transfer_info", nmspSession.getWebAPIOrchestrator().getAudioInfo());

        return response;
    }

    public static PDXQueryResult buildPdxNLUResult(WebApiResponse webApiResponse, Map<String, Object> queryInfo, Session nmspSession, String commandName) throws PdxException
    {
        if(webApiResponse.result.get(APPSERVER_RESULTS) != null)
        {
            return buildAppServerPostResult(webApiResponse, nmspSession, commandName);
        }

        PDXDictionary response = new PDXDictionary();
        response.addUTF8String(NMAS_PRFX_SESSION_ID, nmspSession.getSessionIdString());
        response.addUTF8String(NMAS_PRFX_TRANSACTION_ID, String.valueOf(nmspSession.getWebAPIOrchestrator().getTransactionID()));
        response.addUTF8String(RESULT_TYPE, commandName);
        response.addUTF8String(RESULT_FORMAT, "nlu_interpretation_results");
        response.addInteger(FINAL_RESPONSE, 1);

        // Build interpretation results
        PDXDictionary interpretationResults = new PDXDictionary();
        response.addDictionary("nlu_interpretation_results", interpretationResults);

        interpretationResults.addInteger(FINAL_RESPONSE, 1);
        interpretationResults.addUTF8String("status", "success");
        interpretationResults.addUTF8String("payload_version", "1.0");
        interpretationResults.addUTF8String("payload_format", "nlu-base");
        // Build payload
        PDXDictionary payload = new PDXDictionary();
        interpretationResults.addDictionary(PAYLOAD, payload);
        payload.addUTF8String("type", "nlu-1.0");
        // Build dictation info
        PDXDictionary dictationInfo = new PDXDictionary();
        payload.addDictionary(DIAGNOSTIC_INFO, dictationInfo);
        dictationInfo.addUTF8String("nlu_language", nmspSession.getDictationLanguage());
        dictationInfo.addUTF8String("nlu_version", nmspSession.getWebAPIOrchestrator().getSessionConfigs().getString("apiVersion"));
        dictationInfo.addUTF8String("nmaid", nmspSession.getNmaid());
        if(queryInfo.get(CONTEXT_TAG) != null)
        {
            dictationInfo.addUTF8String(CONTEXT_TAG, (String) queryInfo.get(CONTEXT_TAG));
        }
        // DONE Build dictation info
        // Build interpretations
        if(webApiResponse.result.get(INTERPRETATIONS) != null)
        {
            List<Object> webApiInterpretations = (List<Object>) webApiResponse.result.get(INTERPRETATIONS);
            PDXSequence interpretations = buildPDXInterpretationResult(webApiInterpretations);
            payload.addSequence(INTERPRETATIONS, interpretations);
        }

        // DONE Build interpretations
        // DONE Build interpretations
        // DONE Build payload
        // DONE Build interpretation results

        return new PDXQueryResult(response.toByteArray());
    }

    static PDXQueryResult buildAppServerPostResult(WebApiResponse webApiResponse, Session nmspSession, String commandName)
    {
        PDXDictionary response = new PDXDictionary();
        response.addUTF8String(NMAS_PRFX_SESSION_ID, nmspSession.getSessionIdString());
        response.addUTF8String(NMAS_PRFX_TRANSACTION_ID, String.valueOf(nmspSession.getWebAPIOrchestrator().getTransactionID()));
        response.addUTF8String(RESULT_TYPE, commandName);
        response.addUTF8String(RESULT_FORMAT, "appserver_post_results");
        response.addInteger(FINAL_RESPONSE, 1);

        Map<String, Object> appServerResultsMap = (Map<String, Object>) webApiResponse.result.get(APPSERVER_RESULTS);
        JSONObject appserverDataJsonObject = new JSONObject(appServerResultsMap);

        if(commandName.equalsIgnoreCase("DRAGON_NLU_ASR_CMD"))
        {
            response.addInteger("status_code", ResponseCodes.CompletionCause.SUCCESS.getCause());
            response.addUTF8String("prompt", "");
            response.addUTF8String("cadence_regulatable_result", "completeRecognition");

            JSONObject payload = appserverDataJsonObject.getJSONObject(PAYLOAD);
            JSONObject diagnosticInfo = payload.getJSONObject(DIAGNOSTIC_INFO);
            diagnosticInfo.put("audio_id", "1");

            JSONObject timing = new JSONObject();
            SecureRandom secureRandom = Utils.getSecureRandom();
            int zeroToOneHundred = secureRandom.nextInt(100);
            int zeroToOneThousand = secureRandom.nextInt(1000);
            timing.put("finalRespSentDelay", Integer.toString(zeroToOneHundred));
            timing.put("intermediateRespSentDelay", Integer.toString(zeroToOneThousand));
            diagnosticInfo.put("timing", timing);

            payload.put(DIAGNOSTIC_INFO, diagnosticInfo);
            appserverDataJsonObject.put(PAYLOAD, payload);
        }

        try
        {
            Dictionary appServerResultsDictionary = JsonPDXConverter.jsonObjectToPdxDictionary(appserverDataJsonObject);
            response.addDictionary(APPSERVER_RESULTS, appServerResultsDictionary);
        }
        catch (Exception e)
        {
            logger.trace("Exception while converting appserverDataJsonObject to Dictionary", e);
        }

        return new PDXQueryResult(response.toByteArray());
    }

    private static PDXSequence buildPDXInterpretationResult(List<Object> webApiInterpretations) {
        PDXSequence result = new PDXSequence();
        for (Object entry: webApiInterpretations) {
            PDXDictionary interpretDic = new PDXDictionary();
            result.addDictionary(interpretDic);

            Map<String, Object> interpretObj = (Map<String, Object>) entry;
            interpretDic.addUTF8String(LITERAL, (String) interpretObj.get(LITERAL));
            interpretDic.addDictionary(ACTION, new PDXDictionary());
            interpretDic.getDictionary(ACTION).addDictionary(INTENT, new PDXDictionary());
            interpretDic.getDictionary(ACTION).getDictionary(INTENT).addDouble(CONFIDENCE, (int) interpretObj.get(CONFIDENCE));
            interpretDic.getDictionary(ACTION).getDictionary(INTENT).addUTF8String(VALUE, (String)interpretObj.get(INTENT));

            if (interpretObj.containsKey(CONCEPTS)) {
                PDXDictionary conceptsDic = new PDXDictionary();
                interpretDic.addDictionary(CONCEPTS, conceptsDic);
                for (Object concept : (List<Object>) interpretObj.get(CONCEPTS)) {
                    Map<String, Object> conceptObj = (Map<String, Object>) concept;
                    PDXSequence conceptSeq;
                    if (conceptsDic.containsKey((String) conceptObj.get(CONCEPT))) {
                        conceptSeq = (PDXSequence) conceptsDic.getSequence((String) conceptObj.get(CONCEPT));
                    } else {
                        conceptSeq = new PDXSequence();
                        conceptsDic.addSequence((String) conceptObj.get(CONCEPT), conceptSeq);
                    }
                    PDXDictionary conceptData = new PDXDictionary();
                    conceptSeq.addDictionary(conceptData);

                    conceptData.addUTF8String(LITERAL, (String) conceptObj.get(LITERAL));
                    conceptData.addUTF8String(VALUE, (String) conceptObj.get(VALUE));
                    conceptData.addSequence("ranges", new PDXSequence());
                    PDXSequence ranges = new PDXSequence();
                    ranges.addInteger((Integer) conceptObj.get("begin"));
                    ranges.addInteger((Integer) conceptObj.get("end"));
                    conceptData.getSequence("ranges").addSequence(ranges);
                }
            }
        }
        return result;
    }
}
