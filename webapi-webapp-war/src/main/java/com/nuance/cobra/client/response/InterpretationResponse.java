package com.nuance.cobra.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.api.response.Views;

import java.util.List;
import java.util.Map;

public class InterpretationResponse extends ResponseResult
{
    public Map<String, Object> getVerboseResult() {
        return verboseResult;
    }

    public void setVerboseResult(Map<String, Object> verboseResult) {
        this.verboseResult = verboseResult;
    }

    public static class Concept
    {
        @JsonView(Views.Visible.class)
        @JsonProperty("concepts")
        private List<Concept> concepts;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("concept")
        private String conceptName;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("value")
        private Object value;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("literal")
        private String literal;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("begin")
        private int begin;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("end")
        private int end;

        public Concept(String concept, Object value, String literal, int begin, int end)
        {
            this.conceptName = concept;
            this.value = value;
            this.literal = literal;
            this.begin = begin;
            this.end = end;
        }

        public List<Concept> getConcepts()
        {
            return concepts;
        }

        public void setConcepts(List<Concept> concepts)
        {
            this.concepts = concepts;
        }

        public String getConceptName()
        {
            return conceptName;
        }

        public void setConceptName(String conceptName)
        {
            this.conceptName = conceptName;
        }

        public Object getValue()
        {
            return value;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        public String getLiteral()
        {
            return literal;
        }

        public void setLiteral(String literal)
        {
            this.literal = literal;
        }

        public int getBegin()
        {
            return begin;
        }

        public void setBegin(int begin)
        {
            this.begin = begin;
        }

        public int getEnd()
        {
            return end;
        }

        public void setEnd(int end)
        {
            this.end = end;
        }
    }

    public static class Interpretation
    {
        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("concepts")
        private Concept[] concepts;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("confidence")
        private int confidence;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("literal")
        private String literal;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("intent")
        private String intent;

        @SuppressWarnings("unused")
        @JsonView(Views.Visible.class)
        @JsonProperty("origin")
        private String origin;

        public Interpretation(Concept[] concepts, int confidence, String literal, String intent, String origin)
        {
            this.concepts = concepts;
            this.confidence = confidence;
            this.literal = literal;
            this.intent = intent;
            this.origin = origin;
        }

        public Concept[] getConcepts()
        {
            return concepts;
        }

        public String getIntent()
        {
            return intent;
        }

        public int getConfidence()
        {
            return confidence;
        }

        public String getLiteral()
        {
            return literal;
        }

        public String getOrigin()
        {
            return origin;
        }

        public void setConcepts(Concept[] concepts)
        {
            this.concepts = concepts;
        }

        public void setConfidence(int confidence)
        {
            this.confidence = confidence;
        }

        public void setIntent(String intent)
        {
            this.intent = intent;
        }

        public void setLiteral(String literal)
        {
            this.literal = literal;
        }

        public void setOrigin(String origin)
        {
            this.origin = origin;
        }
    }

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("interpretations")
    private Interpretation[] interpretations;

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("bestInterpretation")
    private Interpretation bestInterpretation;

    @JsonView(Views.Visible.class)
    @JsonProperty("verboseResult")
    private Map<String, Object> verboseResult;

    public InterpretationResponse(ResponseStatus status, Interpretation[] interpretations, boolean isFinal)
    {
        super(status, isFinal);

        this.interpretations = interpretations;

        if(interpretations != null &&
                interpretations.length > 0)
        {
            bestInterpretation = interpretations[0];
        }
        else
        {
            bestInterpretation = null;
        }
    }

    public InterpretationResponse(boolean isFinal, ResponseCode... errors)
    {
        super(isFinal, errors);

        interpretations = null;
        bestInterpretation = null;
    }

    public InterpretationResponse(ResponseCode... errors)
    {
        super(errors);

        interpretations = null;
        bestInterpretation = null;
    }

    public Interpretation[] getInterpretations()
    {
        return interpretations;
    }
}
