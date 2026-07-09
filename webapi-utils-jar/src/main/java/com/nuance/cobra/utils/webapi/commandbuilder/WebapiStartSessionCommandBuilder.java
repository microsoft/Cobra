package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class WebapiStartSessionCommandBuilder extends WebapiCommandBuilder
{
    private static Logger logger = LogManager.getLogger(WebapiStartSessionCommandBuilder.class);

    private String id;
    private String applicationKey;
    private String authenticationType;
    private String authenticationData;

    private String speechDetector;
    private Integer beginNoiseSampleFrames;
    private Double voiceThreshold;
    private Integer startOfSpeechVoicedFrames;
    private Integer startOfSpeechHistoryFrames;
    private Integer endOfSpeechVoicedFrames;
    private Integer endOfSpeechHistoryFrames;
    private Boolean considerNegativeRatios;
    private String speechSynthesisCodec;
    private String speechRecognitionCodec;
    private String clientUserId;
    private String clientDeviceId;
    private String clientSessionId;
    private String clientOsName;
    private String clientOsVersion;
    private String clientCarrier;
    private String clientPhoneModel;
    private JSONObject clientDetails;
    private String sensitivity;
    private Boolean statistics;

    public WebapiStartSessionCommandBuilder(String id, String applicationKey, String authenticationType, String authenticationData)
    {
        super("command", "startSession");
        this.id = id;
        this.applicationKey = applicationKey;
        this.authenticationType = authenticationType;
        this.authenticationData = authenticationData;
    }

    public JSONObject buildRequest()
    {
        JSONObject rc = new JSONObject();
        JSONObject command = new JSONObject();
        JSONObject parameters = new JSONObject();
        JSONObject authentication = new JSONObject();

        rc.put("type", type);
        rc.put("command", command);

        command.put("name", name);
        command.put("id", id);
        command.put("parameters", parameters);

        parameters.put("applicationKey", applicationKey);
        parameters.put("authentication", authentication);
        authentication.put("type", authenticationType);
        authentication.put("data", authenticationData);

        parameters.put("speechDetector", speechDetector);
        parameters.put("beginNoiseSampleFrames", beginNoiseSampleFrames);
        parameters.put("voiceThreshold", voiceThreshold);
        parameters.put("startOfSpeechVoicedFrames", startOfSpeechVoicedFrames);
        parameters.put("startOfSpeechHistoryFrames", startOfSpeechHistoryFrames);
        parameters.put("endOfSpeechVoicedFrames", endOfSpeechVoicedFrames);
        parameters.put("endOfSpeechHistoryFrames", endOfSpeechHistoryFrames);
        parameters.put("considerNegativeRatios", considerNegativeRatios);
        parameters.put("speechSynthesisCodec", speechSynthesisCodec);
        parameters.put("speechRecognitionCodec", speechRecognitionCodec);
        parameters.put("clientUserId", clientUserId);
        parameters.put("clientDeviceId", clientDeviceId);
        parameters.put("clientSessionId", clientSessionId);
        parameters.put("clientOsName", clientOsName);
        parameters.put("clientOsVersion", clientOsVersion);
        parameters.put("clientCarrier", clientCarrier);
        parameters.put("clientPhoneModel", clientPhoneModel);
        parameters.put("clientDetails", clientDetails);
        parameters.put("sensitivity", sensitivity);
        parameters.put("statistics", statistics);

        return rc;
    }

    public WebapiStartSessionCommandBuilder speechDetector(String speechDetector)
    {
        this.speechDetector = speechDetector;
        return this;
    }

    public WebapiStartSessionCommandBuilder beginNoiseSampleFrames(Integer beginNoiseSampleFrames)
    {
        this.beginNoiseSampleFrames = beginNoiseSampleFrames;
        return this;
    }

    public WebapiStartSessionCommandBuilder voiceThreshold(Double voiceThreshold)
    {
        this.voiceThreshold = voiceThreshold;
        return this;
    }

    public WebapiStartSessionCommandBuilder startOfSpeechVoicedFrames(Integer startOfSpeechVoicedFrames)
    {
        this.startOfSpeechVoicedFrames = startOfSpeechVoicedFrames;
        return this;
    }

    public WebapiStartSessionCommandBuilder startOfSpeechHistoryFrames(Integer startOfSpeechHistoryFrames)
    {
        this.startOfSpeechHistoryFrames = startOfSpeechHistoryFrames;
        return this;
    }

    public WebapiStartSessionCommandBuilder endOfSpeechVoicedFrames(Integer endOfSpeechVoicedFrames)
    {
        this.endOfSpeechVoicedFrames = endOfSpeechVoicedFrames;
        return this;
    }

    public WebapiStartSessionCommandBuilder endOfSpeechHistoryFrames(Integer endOfSpeechHistoryFrames)
    {
        this.endOfSpeechHistoryFrames = endOfSpeechHistoryFrames;
        return this;
    }

    public WebapiStartSessionCommandBuilder considerNegativeRatios(Boolean considerNegativeRatios)
    {
        this.considerNegativeRatios = considerNegativeRatios;
        return this;
    }

    public WebapiStartSessionCommandBuilder speechSynthesisCodec(String speechSynthesisCodec)
    {
        this.speechSynthesisCodec = speechSynthesisCodec;
        return this;
    }

    public WebapiStartSessionCommandBuilder speechRecognitionCodec(String speechRecognitionCodec)
    {
        this.speechRecognitionCodec = speechRecognitionCodec;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientUserId(String clientUserId)
    {
        this.clientUserId = clientUserId;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientDeviceId(String clientDeviceId)
    {
        this.clientDeviceId = clientDeviceId;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientSessionId(String clientSessionId)
    {
        this.clientSessionId = clientSessionId;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientOsName(String clientOsName)
    {
        this.clientOsName = clientOsName;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientOsVersion(String clientOsVersion)
    {
        this.clientOsVersion = clientOsVersion;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientCarrier(String clientCarrier)
    {
        this.clientCarrier = clientCarrier;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientPhoneModel(String clientPhoneModel)
    {
        this.clientPhoneModel = clientPhoneModel;
        return this;
    }

    public WebapiStartSessionCommandBuilder clientDetails(JSONObject clientDetails)
    {
        this.clientDetails = clientDetails;
        return this;
    }

    public WebapiStartSessionCommandBuilder sensitivity(String sensitivity)
    {
        this.sensitivity = sensitivity;
        return this;
    }

    public WebapiStartSessionCommandBuilder statistics(Boolean statistics)
    {
        this.statistics = statistics;
        return this;
    }
}
