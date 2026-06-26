package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class WebapiSpeechRecognitionCommandBuilder extends WebapiCommandBuilder
{
    private static Logger logger = LogManager.getLogger(WebapiSpeechRecognitionCommandBuilder.class);

    private String sessionId;
    private String id;

    private String language; // language doesn't appear to be supported by krypton ws
    private String dialogModelApplicationVersion; // not sure what to do with this
    private String speechRecognitionCodec; /** done */
    private Boolean endPointDetection; /** done */
    private String speechDetector; // always krypton gRPC
    private Integer beginNoiseSampleFrames;
    private Double voiceThreshold;
    private Integer startOfSpeechVoicedFrames;
    private Integer startOfSpeechHistoryFrames;
    private Integer endOfSpeechVoicedFrames;
    private Integer endOfSpeechHistoryFrames;
    private Boolean considerNegativeRatios;
    private Boolean stopOnEndOfSpeech; /** done */
    private Boolean wordStream; /** done */
    private String speechDomain; // TODO resource
    private Boolean speechRecognitionVerboseResult;
    private Boolean rawEngineResult;
    private String[] activeDynamicVocabularySets;
    private String sensitivity;
    private Integer startOfSpeechTimeoutSeconds; /** done */
    private Integer utteranceMaxTimeSeconds; /** done */
    private Boolean statistics;
    private Double noiseLevelMean;
    private Double noiseLevelStandardDeviation;
    private String topic;
    // TODO add support for startRecognitionTimers

    public WebapiSpeechRecognitionCommandBuilder(String sessionId, String requestId)
    {
        super("command", "speechRecognition");
        this.sessionId = sessionId;
        id = requestId;
    }

    public JSONObject buildRequest()
    {
        JSONObject rc = new JSONObject();
        JSONObject command = new JSONObject();
        JSONObject parameters = new JSONObject();

        rc.put("type", type);
        rc.put("sessionId", sessionId);
        rc.put("command", command);

        command.put("name", name);
        command.put("id", id);
        command.put("parameters", parameters);

        parameters.put("language", language);
        parameters.put("dialogModelApplicationVersion", dialogModelApplicationVersion);
        parameters.put("speechRecognitionCodec", speechRecognitionCodec);
        parameters.put("endPointDetection", endPointDetection);
        parameters.put("speechDetector", speechDetector);
        parameters.put("beginNoiseSampleFrames", beginNoiseSampleFrames);
        parameters.put("voiceThreshold", voiceThreshold);
        parameters.put("startOfSpeechVoicedFrames", startOfSpeechVoicedFrames);
        parameters.put("startOfSpeechHistoryFrames", startOfSpeechHistoryFrames);
        parameters.put("endOfSpeechVoicedFrames", endOfSpeechVoicedFrames);
        parameters.put("endOfSpeechHistoryFrames", endOfSpeechHistoryFrames);
        parameters.put("considerNegativeRatios", considerNegativeRatios);
        parameters.put("stopOnEndOfSpeech", stopOnEndOfSpeech);
        parameters.put("wordStream", wordStream);
        parameters.put("speechDomain", speechDomain);
        parameters.put("speechRecognitionVerboseResult", speechRecognitionVerboseResult);
        parameters.put("rawEngineResult", rawEngineResult);
        parameters.put("activeDynamicVocabularySets", activeDynamicVocabularySets);
        parameters.put("sensitivity", sensitivity);
        parameters.put("startOfSpeechTimeoutSeconds", startOfSpeechTimeoutSeconds);
        parameters.put("utteranceMaxTimeSeconds", utteranceMaxTimeSeconds);
        parameters.put("statistics", statistics);
        parameters.put("noiseLevelMean", noiseLevelMean);
        parameters.put("noiseLevelStandardDeviation", noiseLevelStandardDeviation);
        parameters.put("topic", topic);

        return rc;
    }

    public WebapiSpeechRecognitionCommandBuilder language(String language)
    {
        this.language = language;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder dialogModelApplicationVersion(String dialogModelApplicationVersion)
    {
        this.dialogModelApplicationVersion = dialogModelApplicationVersion;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder speechRecognitionCodec(String speechRecognitionCodec)
    {
        this.speechRecognitionCodec = speechRecognitionCodec;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder endPointDetection(Boolean endPointDetection)
    {
        this.endPointDetection = endPointDetection;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder speechDetector(String speechDetector)
    {
        this.speechDetector = speechDetector;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder beginNoiseSampleFrames(Integer beginNoiseSampleFrames)
    {
        this.beginNoiseSampleFrames = beginNoiseSampleFrames;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder voiceThreshold(Double voiceThreshold)
    {
        this.voiceThreshold = voiceThreshold;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder startOfSpeechVoicedFrames(Integer startOfSpeechVoicedFrames)
    {
        this.startOfSpeechVoicedFrames = startOfSpeechVoicedFrames;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder startOfSpeechHistoryFrames(Integer startOfSpeechHistoryFrames)
    {
        this.startOfSpeechHistoryFrames = startOfSpeechHistoryFrames;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder endOfSpeechVoicedFrames(Integer endOfSpeechVoicedFrames)
    {
        this.endOfSpeechVoicedFrames = endOfSpeechVoicedFrames;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder endOfSpeechHistoryFrames(Integer endOfSpeechHistoryFrames)
    {
        this.endOfSpeechHistoryFrames = endOfSpeechHistoryFrames;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder considerNegativeRatios(Boolean considerNegativeRatios)
    {
        this.considerNegativeRatios = considerNegativeRatios;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder stopOnEndOfSpeech(Boolean stopOnEndOfSpeech)
    {
        this.stopOnEndOfSpeech = stopOnEndOfSpeech;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder wordStream(Boolean wordStream)
    {
        this.wordStream = wordStream;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder speechDomain(String speechDomain)
    {
        this.speechDomain = speechDomain;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder speechRecognitionVerboseResult(Boolean speechRecognitionVerboseResult)
    {
        this.speechRecognitionVerboseResult = speechRecognitionVerboseResult;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder rawEngineResult(Boolean rawEngineResult)
    {
        this.rawEngineResult = rawEngineResult;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder activeDynamicVocabularySets(String[] activeDynamicVocabularySets)
    {
        this.activeDynamicVocabularySets = activeDynamicVocabularySets;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder sensitivity(String sensitivity)
    {
        this.sensitivity = sensitivity;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder startOfSpeechTimeoutSeconds(Integer startOfSpeechTimeoutSeconds)
    {
        this.startOfSpeechTimeoutSeconds = startOfSpeechTimeoutSeconds;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder utteranceMaxTimeSeconds(Integer utteranceMaxTimeSeconds)
    {
        this.utteranceMaxTimeSeconds = utteranceMaxTimeSeconds;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder statistics(Boolean statistics)
    {
        this.statistics = statistics;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder noiseLevelMean(Double noiseLevelMean)
    {
        this.noiseLevelMean = noiseLevelMean;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder noiseLevelStandardDeviation(Double noiseLevelStandardDeviation)
    {
        this.noiseLevelStandardDeviation = noiseLevelStandardDeviation;
        return this;
    }

    public WebapiSpeechRecognitionCommandBuilder topic(String topic)
    {
        this.topic = topic;
        return this;
    }
}
