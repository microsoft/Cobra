package com.nuance.cobra.utils.nle;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.rpc.nlu.v1.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NLEConverter {
    private static final Logger logger = LogManager.getLogger(NLEConverter.class);
    private static final String VALUE_KEY = "value";
    private static final String CONCEPT_KEY = "concept";
    private static final String CONCEPTS_KEY = "concepts";
    private static final String LITERAL_KEY = "literal";
    private static final String FORMATTED_LITERAL_KEY = "formattedLiteral";
    private static final String BEGIN_KEY = "begin";
    private static final String FORMATTED_BEGIN_KEY = "formattedBegin";
    private static final String END_KEY = "end";
    private static final String FORMATTED_END_KEY = "formattedEnd";
    private static final String MENTIONS_KEY = "mentions";
    private static final String INTENTION_KEY = "intention";
    private static final String INTERPRETATIONS_KEY = "interpretations";
    private static final String INTENT_KEY = "intent";
    private NLEConverter(){}

    public static JSONObject convertWebapiInterpretationToNle(WebApiResponse webApiResponse, boolean hadAsrResultAsInput) {
        JSONObject jsonToReturn = new JSONObject();
        List<JSONObject> interpretationsToReturn = new ArrayList<>();

        webApiResponse.getInterpretations()
                .forEach(
                        interpretation -> interpretationsToReturn.add(parseNleInterpretation((JSONObject) interpretation, hadAsrResultAsInput))
                );

        jsonToReturn.put(INTERPRETATIONS_KEY, interpretationsToReturn);
        return jsonToReturn;
    }

    private static JSONObject parseNleInterpretation(JSONObject givenInterpretationObj, boolean hadAsrResultAsInput) {
        JSONArray concepts = givenInterpretationObj.optJSONArray(CONCEPTS_KEY);

        if (concepts != null) {
            processConcepts(concepts, hadAsrResultAsInput);
        }

        if (hadAsrResultAsInput && givenInterpretationObj.has(LITERAL_KEY)) {
            givenInterpretationObj.put(FORMATTED_LITERAL_KEY, givenInterpretationObj.get(LITERAL_KEY));
        }

        return givenInterpretationObj
                .put(MENTIONS_KEY, givenInterpretationObj.remove(CONCEPTS_KEY))
                .put(INTENTION_KEY, givenInterpretationObj.remove(INTENT_KEY));
    }

    private static void processConcepts(JSONArray concepts, boolean hadAsrResultAsInput) {
        for (int i = 0; i < concepts.length(); i++) {
            JSONObject concept = concepts.getJSONObject(i);
            processSubConcepts(concept);
            formatConceptAsrInput(concept, hadAsrResultAsInput);
        }
    }

    private static void processSubConcepts(JSONObject concept) {
        if (concept.has(CONCEPTS_KEY)) {
            JSONObject firstSubConcept = concept.getJSONArray(CONCEPTS_KEY).getJSONObject(0);
            if (firstSubConcept.has(VALUE_KEY)) {
                JSONObject value = new JSONObject();
                value.put(firstSubConcept.getString(CONCEPT_KEY), firstSubConcept.get(VALUE_KEY));
                concept.put(VALUE_KEY, value.toString());
            }
            concept.remove(CONCEPTS_KEY);
        }
    }

    private static void formatConceptAsrInput(JSONObject concept, boolean hadAsrResultAsInput) {
        if (hadAsrResultAsInput) {
            if (concept.has(LITERAL_KEY)) concept.put(FORMATTED_LITERAL_KEY, concept.get(LITERAL_KEY));
            if (concept.has(BEGIN_KEY)) concept.put(FORMATTED_BEGIN_KEY, concept.get(BEGIN_KEY));
            if (concept.has(END_KEY)) concept.put(FORMATTED_END_KEY, concept.get(END_KEY));
        }
    }
}
