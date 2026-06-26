package com.nuance.cobra.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.utils.LoggingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResponseStatistics
{
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class StatisticsHostInfo
    {
        @JsonView(Views.Visible.class)
        @JsonProperty("region")
        public String region;

        @JsonView(Views.Visible.class)
        @JsonProperty("environment")
        public String environment;

        @JsonView(Views.Visible.class)
        @JsonProperty("version")
        public String version;

        StatisticsHostInfo(Command.StatisticsHostInfo hostInfo)
        {
            region = hostInfo.region;
            environment = hostInfo.environment;
            version = hostInfo.version;
        }
    }


    @JsonView(Views.Visible.class)
    @JsonProperty("hostInfo")
    public StatisticsHostInfo hostInfo;

    @JsonView(Views.Visible.class)
    @JsonProperty("startTime")
    public String startTime;

    @JsonView(Views.Visible.class)
    @JsonProperty("endTime")
    public String endTime;

    @JsonView(Views.Visible.class)
    @JsonProperty("runTimeMs")
    public Long runTimeMs;


    @JsonView(Views.Visible.class)
    @JsonProperty("engineBufferedAudioChunks")
    public Integer engineBufferedAudioChunks;

    @JsonView(Views.Visible.class)
    @JsonProperty("engineSentAudioChunks")
    public Integer engineSentAudioChunks;

    @JsonView(Views.Visible.class)
    @JsonProperty("engineFirstAudioChunk")
    public String engineFirstAudioChunk;

    @JsonView(Views.Visible.class)
    @JsonProperty("engineLastAudioChunk")
    public String engineLastAudioChunk;

    @JsonView(Views.Visible.class)
    @JsonProperty("firstIntermediateResultTime")
    public String firstIntermediateResultTime;

    @JsonView(Views.Visible.class)
    @JsonProperty("lastIntermediateResultTime")
    public String lastIntermediateResultTime;

    @JsonView(Views.Visible.class)
    @JsonProperty("firstAudioBufferReceivedTime")
    public String firstAudioBufferReceivedTime;

    @JsonView(Views.Visible.class)
    @JsonProperty("lastAudioBufferReceivedTime")
    public String lastAudioBufferReceivedTime;

    @JsonView(Views.Visible.class)
    @JsonProperty("firstAudioBufferSentTime")
    public String firstAudioBufferSentTime;

    @JsonView(Views.Visible.class)
    @JsonProperty("lastAudioBufferSentTime")
    public String lastAudioBufferSentTime;


    @JsonView(Views.Visible.class)
    @JsonProperty("modelVersion")
    public String modelVersion;

    @JsonView(Views.Visible.class)
    @JsonProperty("voiceQuality")
    public String voiceQuality;

    @JsonView(Views.Visible.class)
    @JsonProperty("voiceModel")
    public String voiceModel;

    @JsonView(Views.Visible.class)
    @JsonProperty("speechSynthesisTuningBundle")
    public String speechSynthesisTuningBundle;

    @JsonView(Views.Visible.class)
    @JsonProperty("directives")
    public List<ResponseDirectiveStatistics> directives;

    @JsonView(Views.Visible.class)
    @JsonProperty("events")
    public List<ResponseEventStatistics> events;

    @JsonView(Views.Visible.class)
    @JsonProperty("enableSpeechRecognitionSpeakerProfile")
    public boolean enableSpeechRecognitionSpeakerProfile;

    @JsonView(Views.Visible.class)
    @JsonProperty("clientUserID")
    public String clientUserID;

    @JsonView(Views.Visible.class)
    @JsonProperty("mediaDuration")
    public Integer mediaDuration;

    @JsonView(Views.Visible.class)
    @JsonProperty("mediaSize")
    public Integer mediaSize;

    @JsonView(Views.Visible.class)
    @JsonProperty("asrEngineMetrics")
    public Map<String, Map<String, Object>> asrEngineMetrics;

    @JsonView(Views.Visible.class)
    @JsonProperty("engineRequests")
    public Map<String, Object> engineRequests;

    @JsonView(Views.Visible.class)
    @JsonProperty("engineResponses")
    public Map<String, Object> engineResponses;

    @JsonView(Views.Visible.class)
    @JsonProperty("metadata")
    public Map<String, Object> metadata;

    public ResponseStatistics(Command.CommandStatistics statistics)
    {
        enableSpeechRecognitionSpeakerProfile = statistics.enableSpeechRecognitionSpeakerProfile;

        if(statistics.mediaDuration != null) {
            mediaDuration = statistics.mediaDuration;
        }
        if(statistics.mediaSize != null) {
            mediaSize = statistics.mediaSize;
        }

        if (statistics.asrEngineMetrics != null) {
            asrEngineMetrics = statistics.asrEngineMetrics;
        }
        if(statistics.metadata != null) {
            metadata = statistics.metadata;
        }

        if (statistics.clientUserID != null) {
            clientUserID = statistics.clientUserID;
        }

        if(statistics.getModelVersion() != null && !statistics.getModelVersion().trim().isEmpty()) {
            modelVersion = statistics.getModelVersion();
        }
        if(statistics.getVoiceQuality() != null && !statistics.getVoiceQuality().trim().isEmpty()) {
            voiceQuality = statistics.getVoiceQuality();
        }
        if(statistics.getVoiceModel() != null && !statistics.getVoiceModel().trim().isEmpty()) {
            voiceModel = statistics.getVoiceModel();
        }
        if(statistics.getSpeechSynthesisTuningBundle() != null && !statistics.getSpeechSynthesisTuningBundle().trim().isEmpty()){
            speechSynthesisTuningBundle = statistics.getSpeechSynthesisTuningBundle();
        }

        populateTimestamps(statistics);

        populateEngineFields(statistics);

        populateDirectives(statistics);

        populateEvents(statistics);
    }

    private void populateEngineFields(Command.CommandStatistics statistics) {
        if (statistics.engineRequests != null) {
            engineRequests = statistics.engineRequests;
        }
        if (statistics.engineResponses != null) {
            engineResponses = statistics.engineResponses;
        }
        if(statistics.engineBufferedAudioChunks != null) {
            engineBufferedAudioChunks = statistics.engineBufferedAudioChunks;
        }
        if(statistics.engineSentAudioChunks != null) {
            engineSentAudioChunks = statistics.engineSentAudioChunks;
        }
    }

    private void populateEvents(Command.CommandStatistics statistics) {
        if(!statistics.events.isEmpty())
        {
            events = new ArrayList<>();
            for(var i = 0; i < statistics.events.size(); i++)
            {
                events.add(new ResponseEventStatistics(statistics.events.get(i)));
            }
        }
    }

    private void populateDirectives(Command.CommandStatistics statistics) {
        if(!statistics.directives.isEmpty())
        {
            directives = new ArrayList<>();
            for(var i = 0; i < statistics.directives.size(); i++)
            {
                directives.add(new ResponseDirectiveStatistics(statistics.directives.get(i)));
            }
        }
    }

    private void populateTimestamps(Command.CommandStatistics statistics) {
        if(statistics.hostInfo != null) {
            hostInfo = new StatisticsHostInfo(statistics.hostInfo);
        }
        if(statistics.startTime != null) {
            startTime = LoggingUtils.getUTCFormattedTimestamp(statistics.startTime);
        }
        if(statistics.endTime != null) {
            endTime = LoggingUtils.getUTCFormattedTimestamp(statistics.endTime);
        }
        if(statistics.runTimeMs != null) {
            runTimeMs = statistics.runTimeMs;
        }
        if(statistics.firstAudioBufferReceivedTime != null) {
            firstAudioBufferReceivedTime = LoggingUtils.getUTCFormattedTimestamp(statistics.firstAudioBufferReceivedTime);
        }
        if(statistics.lastAudioBufferReceivedTime != null) {
            lastAudioBufferReceivedTime = LoggingUtils.getUTCFormattedTimestamp(statistics.lastAudioBufferReceivedTime);
        }
        if(statistics.firstIntermediateResultTime != null) {
            firstIntermediateResultTime = LoggingUtils.getUTCFormattedTimestamp(statistics.firstIntermediateResultTime);
        }
        if(statistics.lastIntermediateResultTime != null) {
            lastIntermediateResultTime = LoggingUtils.getUTCFormattedTimestamp(statistics.lastIntermediateResultTime);
        }
        if(statistics.firstAudioBufferSentTime != null) {
            firstAudioBufferSentTime = LoggingUtils.getUTCFormattedTimestamp(statistics.firstAudioBufferSentTime);
        }
        if(statistics.lastAudioBufferSentTime != null) {
            lastAudioBufferSentTime = LoggingUtils.getUTCFormattedTimestamp(statistics.lastAudioBufferSentTime);
        }
        if(statistics.engineFirstAudioChunk != null) {
            engineFirstAudioChunk = LoggingUtils.getUTCFormattedTimestamp(statistics.engineFirstAudioChunk);
        }
        if(statistics.engineLastAudioChunk != null) {
            engineLastAudioChunk = LoggingUtils.getUTCFormattedTimestamp(statistics.engineLastAudioChunk);
        }
    }

    public class ResponseDirectiveStatistics
    {
        @JsonView(Views.Visible.class)
        @JsonProperty("name")
        public String name;

        @JsonView(Views.Visible.class)
        @JsonProperty("startTime")
        public String startTime;

        public ResponseDirectiveStatistics(Command.DirectiveStatistics directiveStatistics)
        {
            this.name = directiveStatistics.name;
            startTime = LoggingUtils.getUTCFormattedTimestamp(directiveStatistics.startTime);
        }
    }

    public class ResponseEventStatistics
    {
        @JsonView(Views.Visible.class)
        @JsonProperty("name")
        public String name;

        @JsonView(Views.Visible.class)
        @JsonProperty("startTime")
        public String startTime;

        public ResponseEventStatistics(Command.EventStatistics eventStatistics)
        {
            this.name = eventStatistics.name;
            startTime = LoggingUtils.getUTCFormattedTimestamp(eventStatistics.startTime);
        }
    }
}
