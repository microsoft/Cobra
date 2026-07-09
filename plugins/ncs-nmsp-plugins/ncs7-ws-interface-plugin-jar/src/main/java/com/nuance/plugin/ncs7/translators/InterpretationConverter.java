package com.nuance.plugin.ncs7.translators;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

/**
 * Converts Cobra 2.26 Interpretations to Mix v2 format. MixInterpretation objects are serializable (Object -> JSON String) via com.fasterxml.jackson
 * Mix v2 NLU specification (see Mix documentation for details).
 */
public class InterpretationConverter {

    private InterpretationConverter() {
        throw new IllegalStateException("Utility class");
    }

    // Cobra 2.26 interpretation
    // Meant to be instantiated via JacksonXML's ObjectMapper
    public static class CobraInterpretation {
        private String literal;
        private String intent;
        private Integer confidence; // range is 0 to 100 (inclusive)
        private List<CobraConcept> concepts = new ArrayList<>();

        public String getLiteral() {
            return literal;
        }

        public String getIntent() {
            return intent;
        }

        /**
         * range: 0 to 100 (inclusive)
         */
        public Integer getConfidence() {
            return confidence;
        }

        public List<CobraConcept> getConcepts() {
            return concepts;
        }
    }

    public static class CobraConcept {
        private String literal;
        private Integer begin;
        private Integer end;

        private String concept;
        private Object value; // usually a String but can be a Map, for example when the concept is of type nuance_CALENDARX

        private List<CobraConcept> concepts = new ArrayList<>(); // sub-concepts

        public String getLiteral() {
            return literal;
        }

        public Integer getBegin() {
            return begin;
        }

        public Integer getEnd() {
            return end;
        }

        public String getConcept() {
            return concept;
        }

        public Object getValue() {
            return value;
        }

        public List<CobraConcept> getConcepts() {
            return concepts;
        }
    }

    // Mix v2 interpretation
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class MixInterpretation {
        private String literal;
        private MixAction action;
        private Map<String, List<MixConcept>> concepts;

        public String getLiteral() {
            return literal;
        }

        public void setLiteral(String literal) {
            this.literal = literal;
        }

        public MixAction getAction() {
            return action;
        }

        public void setAction(MixAction action) {
            this.action = action;
        }

        public Map<String, List<MixConcept>> getConcepts() {
            return concepts;
        }

        public void setConcepts(Map<String, List<MixConcept>> concepts) {
            this.concepts = concepts;
        }
    }

    public static class MixAction {
        private MixIntent intent;

        public MixIntent getIntent() {
            return intent;
        }

        public void setIntent(MixIntent intent) {
            this.intent = intent;
        }
    }

    public static class MixIntent {
        private String value;
        private Double confidence;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }
    }

    /** Mix v2 concepts either have a value or concepts, but never both. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class MixConcept {
        private String literal;
        private final List<List<Integer>> ranges = new ArrayList<>();
        private Object value; // usually a String but can be a Map, for example when the concept is of type nuance_CALENDARX
        private Object concepts; // sub-concepts

        public String getLiteral() {
            return literal;
        }

        public void setLiteral(String literal) {
            this.literal = literal;
        }

        public List<List<Integer>> getRanges() {
            return ranges;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getConcepts() {
            return concepts;
        }

        public void setConcepts(Object concepts) {
            this.concepts = concepts;
        }
    }

    public static List<MixInterpretation> convertToMixInterpretations(boolean useNewConceptFormat, List<CobraInterpretation> cobraInterpretationList) {
        List<MixInterpretation> mixInterpretationList = new ArrayList<>();

        for (CobraInterpretation cobraInterpretation : cobraInterpretationList) {
            MixInterpretation mixInterpretation = new MixInterpretation();

            mixInterpretation.setLiteral(cobraInterpretation.getLiteral());
            mixInterpretation.setAction(new MixAction());
            mixInterpretation.getAction().setIntent(new MixIntent());
            mixInterpretation.getAction().getIntent().setValue(cobraInterpretation.getIntent());
            mixInterpretation.getAction().getIntent().setConfidence((double) cobraInterpretation.getConfidence() / 100.0);
            if(useNewConceptFormat) {
                mixInterpretation.setConcepts(convertToMixConcepts(cobraInterpretation.getConcepts()));
            }
            else {
                mixInterpretation.setConcepts(oldConvertToMixConcepts(cobraInterpretation.getConcepts()));
            }

            mixInterpretationList.add(mixInterpretation);
        }
        return mixInterpretationList;
    }

    private static Map<String, List<MixConcept>> oldConvertToMixConcepts(List<CobraConcept> cobraConceptList) {
        Map<String, List<MixConcept>> mixConcepts = new HashMap<>();

        for (CobraConcept cobraConcept : cobraConceptList) {

            MixConcept newMixConcept = new MixConcept();
            newMixConcept.setLiteral(cobraConcept.getLiteral());
            newMixConcept.getRanges().add(Arrays.asList(cobraConcept.getBegin(), cobraConcept.getEnd()));
            if (!cobraConcept.getConcepts().isEmpty()) {
                newMixConcept.setConcepts(oldConvertToMixConcepts(cobraConcept.getConcepts()));
            } else {
                newMixConcept.setValue(cobraConcept.getValue());
            }

            List<MixConcept> mixConceptList = getMixConceptList(mixConcepts, cobraConcept);
            mixConceptList.add(newMixConcept);
        }

        return mixConcepts;
    }

    private static Map<String, List<MixConcept>> convertToMixConcepts(List<CobraConcept> cobraConceptList) {
        Map<String, List<MixConcept>> mixConcepts = new HashMap<>();

        for (CobraConcept cobraConcept : cobraConceptList) {

            MixConcept newMixConcept = new MixConcept();
            if(!(cobraConcept.getValue() instanceof Map)) { // If the value is a struct,
                newMixConcept.setLiteral(cobraConcept.getLiteral());
                newMixConcept.getRanges().add(Arrays.asList(cobraConcept.getBegin(), cobraConcept.getEnd()));
                if (!cobraConcept.getConcepts().isEmpty()) {
                    newMixConcept.setConcepts(convertToMixConcepts(cobraConcept.getConcepts()));
                } else {
                    newMixConcept.setValue(cobraConcept.getValue());
                }
            } else {
                Map<String, Object> cobraValue = (Map)cobraConcept.getValue();
                Map<String, Object> subConcepts = new HashMap<>();
                for (Map.Entry<String, Object> subconcept: cobraValue.entrySet()) {
                    String root = subconcept.getKey();

                    MixConcept newMixSubConcept = new MixConcept();
                    newMixSubConcept.setLiteral(cobraConcept.getLiteral());
                    newMixSubConcept.getRanges().add(Arrays.asList(cobraConcept.getBegin(), cobraConcept.getEnd()));
                    newMixSubConcept.value = subconcept.getValue();

                    List<MixConcept> subConceptArray = new ArrayList<>();
                    if(subConcepts.containsKey(root)) {
                        subConceptArray = (List<MixConcept>)subConcepts.get(root);
                    }
                    subConceptArray.add(newMixSubConcept);
                    subConcepts.put(root, subConceptArray);
                }
                newMixConcept.setConcepts(subConcepts);
            }

            List<MixConcept> mixConceptList = getMixConceptList(mixConcepts, cobraConcept);
            mixConceptList.add(newMixConcept);
        }

        return mixConcepts;
    }

    private static List<MixConcept> getMixConceptList(Map<String, List<MixConcept>> mixConcepts, CobraConcept cobraConcept) {
        List<MixConcept> mixConceptList;
        if (mixConcepts.containsKey(cobraConcept.getConcept())) {
            mixConceptList = mixConcepts.get(cobraConcept.getConcept());
        } else {
            mixConceptList = new ArrayList<>();
            mixConcepts.put(cobraConcept.getConcept(), mixConceptList);
        }
        return mixConceptList;
    }
}
