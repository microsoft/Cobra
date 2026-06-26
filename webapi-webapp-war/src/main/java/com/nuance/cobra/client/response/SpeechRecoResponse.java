package com.nuance.cobra.client.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.api.response.Views;
import com.nuance.cobra.utils.audio.NoiseLevel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class SpeechRecoResponse extends ResponseResult
{
    public static class Word
    {
        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("confidence")
        private final float confidence;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("word")
        private final String word;

        public Word(String word, float confidence)
        {
            this.word = word;
            this.confidence = confidence;
        }
    }

    @JsonPropertyOrder({"transcriptions","confidence","averageWeightedConfidences","words"})
    public static class Data
    {
        @JsonProperty("transcriptions")
        @JsonView(Views.Visible.class)
        public Object getTranscriptionsProperty()
        {
            if (encryptedTranscriptions!=null)
            {
                return encryptedTranscriptions;
            }
            else
            {
                return transcriptions;
            }
        }

        @SuppressWarnings("unused")
        private String encryptedTranscriptions = null;

        @SuppressWarnings("unused")
        private String[] transcriptions;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("confidence")
        private final Integer confidence;
        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("averageWeightedConfidences")
        private int[] averageWeightedConfidences;


        @SuppressWarnings("unused")
        private String encryptedWords = null;

        @SuppressWarnings("unused")
        private Word[][] words;

        public Data(String[] transcriptions, int[] averageWeightedConfidences, int confidence, Word[][] words, int maxSize)
        {
            this.confidence = confidence;
            this.transcriptions = transcriptions;
            this.averageWeightedConfidences = averageWeightedConfidences;
            this.words = words;

            limitListsToMaxLength(maxSize);
        }

        @JsonIgnore
        public String[] getTranscriptions()
        {
            return transcriptions;
        }

        @JsonIgnore
        public Word[][] getWords() { return words; }

        @JsonIgnore
        public int[] getAverageWeightedConfidences() { return averageWeightedConfidences; }


        @JsonProperty("words")
        @JsonView(Views.Visible.class)
        public Object getWordsProperty()
        {
            if (encryptedWords != null)
            {
                return encryptedWords;
            }
            else
            {
                return words;
            }
        }

        public void setEncryptedTranscriptions(String encryptedTranscriptions)
        {
            this.transcriptions = null;
            this.encryptedTranscriptions = encryptedTranscriptions;
        }

        public void setEncryptedWords(String encryptedWords)
        {
            this.words = null;
            this.encryptedWords = encryptedWords;
        }

        protected void limitListsToMaxLength(int maxLength) {
            if(maxLength < 1) {
                return;
            }
            if ( transcriptions.length > maxLength ) {
                String[] newTranscriptions = new String[maxLength];
                System.arraycopy(transcriptions, 0, newTranscriptions, 0, maxLength);
                transcriptions = newTranscriptions;
            }
            if ( averageWeightedConfidences.length > maxLength ) {
                int[] newAverageWeightedConfidences = new int[maxLength];
                System.arraycopy(averageWeightedConfidences, 0, newAverageWeightedConfidences, 0, maxLength);
                averageWeightedConfidences = newAverageWeightedConfidences;
            }
            if ( words.length > maxLength ) {
                Word[][] newWords = new Word[maxLength][];
                System.arraycopy(words, 0, newWords, 0, maxLength);
                words = newWords;
            }
        }
    }

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("data")
    private Data data;

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("bestResult")
    private String bestResult;

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("noiseLevel")
    private NoiseLevel noiseLevel;

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("verboseResult")
    private Map<String, Object> verboseResult;

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("engineRawResult")
    private Map<String, Object> engineRawResult;

    private String nlsmlResult = null;

    public SpeechRecoResponse(boolean isFinal, NoiseLevel noiseLevel, ResponseCode... errors)
    {
        super(isFinal, errors);
        data = null;
        bestResult = null;
        this.noiseLevel = noiseLevel;
    }

    public SpeechRecoResponse(boolean isFinal, Data data, NoiseLevel noiseLevel)
    {
        super(ResponseStatus.SUCCESS, isFinal);
        this.data = data;

        if(data != null &&
           data.transcriptions != null &&
           data.transcriptions.length > 0)
        {
            bestResult = data.transcriptions[0];
        }
        else
        {
            bestResult = null;
        }

        this.noiseLevel = noiseLevel;
    }

    public SpeechRecoResponse(ResponseStatus status, NoiseLevel noiseLevel) {
        this(true, status, noiseLevel);
    }

    public SpeechRecoResponse(boolean isFinal, ResponseStatus status, NoiseLevel noiseLevel)
    {
        super(status, isFinal);
        data = null;
        bestResult = null;
        this.noiseLevel = noiseLevel;
    }

    public Data getData()
    {
        return data;
    }

    public void setData(Data pData)
    {
        data = pData;
    }

    public String getBestResult()
    {
        return bestResult;
    }

    public void setBestResult(String pBestResult)
    {
        bestResult = pBestResult;
    }

    public NoiseLevel getNoiseLevel() { return noiseLevel; }

    public String getNlsmlResult() { return nlsmlResult; }

    public void setNlsmlResult(String nlsmlResult) { this.nlsmlResult = nlsmlResult; }

    public Map<String, Object> getVerboseResult() { return verboseResult; }

    public void setVerboseResult(Map<String, Object> verboseResult)
    {
        this.verboseResult = verboseResult;
    }

    public Map<String, Object> getRawResult() { return engineRawResult; }
    public void setRawResult(Map<String, Object> verboseResult) {
        this.engineRawResult = verboseResult;
    }

    /*
    public class UttInfo
    {
        Status status;
        int duration;

        UttInfo(Status status, int duration)
        {
            this.status = status;
            this.duration = duration;
        }
    }
    UttInfo utteranceInfo;
    */
}
