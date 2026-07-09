package com.nuance.cobra.utils.krypton;

import com.google.protobuf.util.JsonFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.nuance.cobra.utils.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

@java.lang.SuppressWarnings("java:S3776")
public class KryptonConverter {

    private static final Logger logger = LogManager.getLogger();

    private static final String LEVEL = "level";
    private static final String WORDS = "words";

    private KryptonConverter() {}

    public static Map<String, Object> getVerboseResultFromJson(JSONObject verboseResultMessage)
    {
        JSONObject result = new JSONObject();

        for (String key : verboseResultMessage.keySet())
        {
            switch (key)
            {
                case "nBest":
                    result.put("hypotheses", convertToHypotheses(verboseResultMessage.getJSONArray(key)));
                    break;
                case "utterance_info":
                    result.put("utteranceInfo",
                            convertUtteranceInfoFromWsToGrpc(verboseResultMessage.getJSONObject(key)));
                    break;
                case "absStart":
                case "absEnd":
                    result.put(key + "Ms", verboseResultMessage.get(key));
                    break;
                default:
                    break;
            }
        }

        Gson gson = new GsonBuilder().create(); //Gson preserves key order
        return gson.fromJson(result.toString(), Map.class);// result.toMap(); does not preserve numbers types (Converts doubles to int).
    }

    public static JSONObject convertVerboseResultToKryptonWs(JSONObject verboseResultMessage)
    {
        JSONObject result = new JSONObject();

        for (String key : verboseResultMessage.keySet())
        {
            switch (key)
            {
                case "hypotheses":
                    result.put("nBest", convertToNbest(verboseResultMessage.getJSONArray(key)));
                    break;
                case "utteranceInfo":
                    result.put("utterance_info",
                            convertUtteranceInfoFromGrpcToWs(verboseResultMessage.getJSONObject(key)));
                    break;
                case "absStartMs":
                case "absEndMs":
                    result.put(key.replace("Ms", ""), verboseResultMessage.getInt(key));
                    break;
                default:
                    break;
            }
        }
        result.put("status", "COMPLETE");
        result.put("completion_cause", "SUCCESS");
        result.put("utterance_complete", true);
        return result;
    }

    private static JSONObject convertUtteranceInfoFromWsToGrpc(JSONObject utteranceInfo)
    {
        JSONObject result = new JSONObject();
        JSONObject dsp = new JSONObject();

        dsp.put("snrEstimateDb", utteranceInfo.get("snr_estimate"));
        dsp.put(LEVEL, utteranceInfo.get(LEVEL));
        dsp.put("numChannels", 1);
        dsp.put("initialSilenceMs", utteranceInfo.get("initial_silence"));
        dsp.put("initialEnergy", utteranceInfo.get("initial_energy"));
        dsp.put("finalEnergy", utteranceInfo.get("final_energy"));
        dsp.put("meanEnergy", utteranceInfo.get("mean_energy"));
        result.put("dsp", dsp);

        result.put("durationMs", utteranceInfo.get("duration"));
        result.put("clippingDurationMs", utteranceInfo.get("clipping_duration"));
        result.put("droppedSpeechPackets", utteranceInfo.get("dropped_speech_packets"));
        result.put("droppedNonspeechPackets", utteranceInfo.get("dropped_non_speech_packets"));

        return result;
    }

    private static JSONObject convertUtteranceInfoFromGrpcToWs(JSONObject utteranceInfo)
    {
        JSONObject result = new JSONObject();

        JSONObject dsp = utteranceInfo.getJSONObject("dsp");

        result.put("snr_estimate", dsp.getInt("snrEstimateDb"));
        result.put(LEVEL, dsp.getInt(LEVEL));
        result.put("stereo", false);
        result.put("initial_silence", dsp.getInt("initialSilenceMs"));
        result.put("initial_energy", dsp.get("initialEnergy"));
        result.put("final_energy", dsp.get("finalEnergy"));
        result.put("mean_energy", dsp.get("meanEnergy"));

        result.put("duration", utteranceInfo.getInt("durationMs"));
        result.put("clipping_duration", utteranceInfo.getInt("clippingDurationMs"));
        result.put("dropped_speech_packets", utteranceInfo.getInt("droppedSpeechPackets"));
        result.put("dropped_non_speech_packets", utteranceInfo.getInt("droppedNonspeechPackets"));

        return result;
    }

    private static JSONArray convertToHypotheses(JSONArray wordsList)
    {
        JSONArray result = new JSONArray();
        for (Object wordsMapList : wordsList)
        {
            JSONObject transcriptMap = (JSONObject) wordsMapList;
            JSONObject resultMap = new JSONObject();
            result.put(resultMap);
            for (String key : transcriptMap.keySet())
            {
                if (key.toLowerCase()
                       .contains("confidence"))
                {
                    resultMap.put(key, Tools.reMap(transcriptMap.getDouble(key), 0, 1000, 0, 1));
                }
                else if (key.equals(WORDS))
                {
                    JSONArray resultWords = new JSONArray();
                    resultMap.put(key, resultWords);

                    for (Object wordInList : transcriptMap.getJSONArray(WORDS))
                    {
                        JSONObject wordDict = (JSONObject) wordInList;
                        JSONObject resultDict = new JSONObject();
                        resultWords.put(resultDict);

                        for (String wordKey : wordDict.keySet())
                        {
                            if (wordKey.toLowerCase()
                                       .contains("confidence"))
                            {
                                resultDict.put(wordKey, Tools.reMap(wordDict.getDouble(wordKey), 0, 1000, 0, 1));
                            }
                            else if (wordKey.equals("silence_after_word") || wordKey.equals("start") ||
                                     wordKey.equals("end"))
                            {
                                resultDict.put(wordKey + "Ms", wordDict.get(wordKey));
                            }
                            else
                            {
                                resultDict.put(wordKey, wordDict.get(wordKey));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static JSONArray convertToNbest(JSONArray wordsList)
    {
        JSONArray result = new JSONArray();
        for (Object wordsMapList : wordsList)
        {
            JSONObject grpcTranscriptMap = (JSONObject) wordsMapList;
            JSONObject grpcResultMap = new JSONObject();
            result.put(grpcResultMap);
            for (String grpcKey : grpcTranscriptMap.keySet())
            {
                if (grpcKey.contains("onfidence")) //averageConfidence and confidence
                {
                    grpcResultMap.put(grpcKey, Tools.reMap(grpcTranscriptMap.getDouble(grpcKey), 0, 1, 0, 1000).intValue());
                }
                else if (grpcKey.equals("encryptedTokenization"))
                {
                    grpcResultMap.put("tokenization", grpcTranscriptMap.get(grpcKey));
                }
                else if (grpcKey.equals(WORDS))
                {
                    JSONArray grpcResultWords = new JSONArray();
                    grpcResultMap.put(grpcKey, grpcResultWords);

                    for (Object wordInList : grpcTranscriptMap.getJSONArray(WORDS))
                    {
                        JSONObject grpcWordDict = (JSONObject) wordInList;
                        JSONObject resultDict = new JSONObject();
                        grpcResultWords.put(resultDict);

                        for (String wordKey : grpcWordDict.keySet())
                        {
                            if (wordKey.contains("onfidence"))
                            {
                                resultDict.put(wordKey, Tools.reMap(grpcWordDict.getDouble(wordKey), 0, 1, 0, 1000).intValue());
                            }
                            else if (wordKey.equals("silenceAfterWordMs"))
                            {
                                resultDict.put("silence_after_word", grpcWordDict.getInt(wordKey));
                            }
                            else if (wordKey.equals("startMs") || wordKey.equals("endMs"))
                            {
                                resultDict.put(wordKey.replace("Ms", ""), grpcWordDict.getInt(wordKey));
                            }
                            else
                            {
                                resultDict.put(wordKey, grpcWordDict.get(wordKey));
                            }
                        }
                    }
                }
                else
                {
                    grpcResultMap.put(grpcKey, grpcTranscriptMap.get(grpcKey));
                }
            }
        }
        return result;
    }

    public static Map<String, Object> getVerboseResultFromGRPC(MessageOrBuilder verboseResultMessage)
    {
        Gson gson = new GsonBuilder().create(); //Gson preserves key order
        return gson.fromJson(grpcMessageToJsonString(verboseResultMessage), Map.class);
    }

    public static String grpcMessageToJsonString(MessageOrBuilder messageOrBuilder)
    {
        String rc;
        try
        {
            rc = JsonFormat
                    .printer()
                    .includingDefaultValueFields()
                    .omittingInsignificantWhitespace()
                    .print(messageOrBuilder);

           logger.trace("grpcMessageToJsonString = {}", rc);
        }
        catch (InvalidProtocolBufferException e)
        {
            rc = null;
        }
        return rc;
    }
}
