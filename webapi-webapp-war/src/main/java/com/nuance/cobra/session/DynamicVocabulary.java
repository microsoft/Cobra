package com.nuance.cobra.session;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.utils.CobraException;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.util.*;

/**
 * The object representation of a dynamic vocabulary
 */
public class DynamicVocabulary
{
    @JsonProperty("id")
    private String id;

    /**
     * defines the different types available for dynamic vocabulary
     */
    public enum VocabularyType
    {
        @JsonProperty("application/x-nuance-wordset")APPLICATION_NUANCE_WORDSET,
        @JsonProperty("application/x-nuance-wordset-pkg")APPLICATION_NUANCE_WORDSET_PKG,
        @JsonProperty("application/x-nuance-domainlm")APPLICATION_NUANCE_DOMAINLM,
        @JsonProperty("application/x-nuance-builtin")APPLICATION_NUANCE_BUILTIN
    }

    /**
     * The inner class defining the vocabulary itself defined by a literal and a list of spoken sentences
     */
    public static class Vocabulary
    {
        //The literal expression
        @JsonProperty("literal")
        String literal;

        //The list of spoken sentences that result in the literal definition
        @JsonProperty("spoken")
        ArrayList<String> spoken;

        //The "value"/"canonical" for NLU
        @JsonProperty("canonical")
        String canonincal;

        /**
         * Vocabulary constructor
         */
        public Vocabulary()
        {
            spoken = new ArrayList<>();
        }


        /**
         * Getter for the literal value
         * @return
         *  The value of literal
         */
        public String getLiteral()
        {
            return literal;
        }

        /**
         * Getter for the 'spoken' list
         * @return
         *  The 'spoken' list, if the list is empty, returning the literal value
         */
        public ArrayList<String> getSpoken()
        {
            if (spoken != null && !spoken.isEmpty())
            {
                return spoken;
            }
            else
            {
                ArrayList<String> spokenList = new ArrayList<>();
                spokenList.add(literal);
                return spokenList;
            }
        }

        /**
         * Setter for literal
         * @param literal
         *  The value to be set
         */
        public void setLiteral(String literal)
        {
            this.literal = literal;
        }

        /**
         * Method for adding an element to the list of 'spoken'
         * @param spoken
         *  The element to be added to the list
         */
        public void addSpoken(String spoken)
        {
            this.spoken.add(spoken);
        }

        public void clearSpokenList()
        {
            if(spoken == null) return;
            spoken.clear();
        }

        public void setCanonincal(String canonincal) { this.canonincal = canonincal; }

        public String getCanonical() { return canonincal; }
    }

    //The type of the vocabulary
    @JsonProperty("type")
    private VocabularyType type;

    /**
     * The body is in json format
     */
    @JsonProperty("body")
    private Map<String, ArrayList<Vocabulary>> body;

    //The builtin name
    @JsonProperty("builtin")
    private String builtin;

    //The weight of the vocabulary
    @JsonProperty("weight")
    private Object weight;

    /**
     * either body or uri will be populated depending on the type
     */
    private String uri;

    private ArrayList<String> spokenList;

    CombinedConfiguration requestConfig = null;

    /**
     * DynamicVocabulary constructor.
     *
     * @param id
     *  The id of the dynamicVocabulary
     * @param type
     *  The type of the dynamicVocabulary
     *  @see DynamicVocabulary.VocabularyType
     * @param body
     *  The body in json format transformed as a Map
     */
    public DynamicVocabulary(String id, VocabularyType type, Map<String, ArrayList<Vocabulary>> body)
    {
        this.id = id;
        this.type = type;
        this.body = body;
        populateSpokenList();
    }

    public DynamicVocabulary(String id, VocabularyType type, Map<String, ArrayList<Vocabulary>> body, boolean persist, String incrementalAction, CombinedConfiguration requestConfig)
    {
        this(id, type, body);
        this.requestConfig = requestConfig;

        if(persist)
        {
            if(incrementalAction.equals("add") || incrementalAction.equals("remove"))
            {
                DynamicVocabularyDb.incrementVocabInDb(requestConfig, incrementalAction, body);
            } else
            {
                DynamicVocabularyDb.saveDynamicVocabToDb(requestConfig, body);
            }
        }
    }

    /**
     * DynamicVocabulary constructor. This constructor is private, only used by fromJsonMap method.
     *
     * @param id
     *  The id of the dynamicVocabulary
     * @param type
     *  The type of the dynamicVocabulary
     *  @see DynamicVocabulary.VocabularyType
     * @param uri
     *  The uri of the resource associated with the dynamic vocabulary
     */
    public DynamicVocabulary(String id, VocabularyType type, String uri)
    {
        this.id = id;
        this.type = type;
        this.uri = uri;
    }

    /**
     * DynamicVocabulary constructor. This constructor is private, only used by fromJsonMap method.
     *
     * @param id
     *  The id of the dynamicVocabulary
     * @param type
     *  The type of the dynamicVocabulary
     *  @see DynamicVocabulary.VocabularyType
     * @param uri
     *  The uri of the resource associated with the dynamic vocabulary
     * @param builtin
     *  The name of the builtin associated with the dynamic vocabulary
     * @param weight
     *  The weight associated with the dynamic vocabulary
     */
    public DynamicVocabulary(String id, VocabularyType type, String uri, String builtin, Object weight)
    {
        this.id = id;
        this.type = type;
        this.uri = uri;
        this.builtin = builtin;
        this.weight = weight;
    }

    /**
     * Getter for the builtin
     * @return
     *  The builtin of the dynamicVocabulary
     */
    public String getBuiltin()
    {
        return builtin;
    }

    /**
     * Getter for the weight
     * @return
     *  The builtin of the dynamicVocabulary
     */
    public Object getWeight()
    {
        return weight;
    }

    /**
     * Getter for the Id
     * @return
     *  The Id of the dynamicVocabulary
     */
    public String getId()
    {
        return id;
    }

    /**
     * Getter for the type
     * @return
     *  The type of the dynamicVocabulary
     */
    public VocabularyType getType()
    {
        return type;
    }

    /**
     * Setter for the Id of the dynamicVocabulary
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Method that returns the list of spoken sentences
     * @return
     */
    public ArrayList<String> getSpokenList()
    {
        return spokenList;
    }

    public int getSpokenListSize()
    {
        if(spokenList == null)
        {
            return 0;
        }

        return spokenList.size();
    }

    /**
     * Getter for the uri
     * @return
     *  The uri of the dynamicVocabulary (for wordset-pkg and domainlm)
     */
    public String getUri()
    {
        return uri;
    }

    /**
     * Method that populates the spokenList for the whole dynamicVocabulary. This is needed when the vocabulary is
     * activated during a speechRecognition. Done within the constructor to avoid computing each time there is a
     * speech recognition.
     */
    private void populateSpokenList()
    {
        spokenList = new ArrayList<>();
        for (Object o : body.entrySet())
        {
            Map.Entry pair = (Map.Entry) o;
            @SuppressWarnings("unchecked") List<Vocabulary> vocabularyList = (ArrayList<Vocabulary>) pair.getValue();

            for (Vocabulary vocabulary : vocabularyList)
            {
                spokenList.addAll(vocabulary.getSpoken());
            }
        }
    }


    public static DynamicVocabulary fromJsonMap(Map<String, Object> dynamicVocabulary, boolean isApplicationVocabulary, CombinedConfiguration requestConfig)
    {
        String id = isApplicationVocabulary ? null:validateStringNotNullOrEmpty(dynamicVocabulary.get("id"), "id");

        String type = validateStringNotNullOrEmpty(dynamicVocabulary.get("type"), "type");

        if( type.equals("application/x-nuance-wordset"))
        {
            return buildWordsetDynamicVocabularyObject(id, dynamicVocabulary, requestConfig);
        }
        if( type.equals("application/x-nuance-wordset-pkg") || type.equals("application/x-nuance-wordset-nle-pkg"))
        {
            return buildWordsetPkgDynamicVocabularyObject(id, dynamicVocabulary);
        }
        if( type.equals("application/x-nuance-builtin"))
        {
            return new DynamicVocabulary(id, VocabularyType.APPLICATION_NUANCE_BUILTIN, null, validateStringNotNullOrEmpty(dynamicVocabulary.get("builtin"), "builtin"), dynamicVocabulary.get("weight"));
        }
        if( type.equals("application/x-nuance-domainlm"))
        {
            return new DynamicVocabulary(id, VocabularyType.APPLICATION_NUANCE_DOMAINLM, validateStringNotNullOrEmpty(dynamicVocabulary.get("uri"), "uri"),null, dynamicVocabulary.get("weight"));
        }
        throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, type is not supported");
    }

    private static DynamicVocabulary buildWordsetPkgDynamicVocabularyObject(String id, Map<String, Object> dynamicVocabulary)
    {
        return new DynamicVocabulary(id, VocabularyType.APPLICATION_NUANCE_WORDSET_PKG, validateStringNotNullOrEmpty(dynamicVocabulary.get("uri"), "uri"));
    }

    @SuppressWarnings("java:S3776")
    private static DynamicVocabulary buildWordsetDynamicVocabularyObject(String id, Map<String, Object> dynamicVocabulary, CombinedConfiguration requestConfig)
    {
        //The actual body that will be put in the DynamicVocabulary object
        HashMap<String, ArrayList<Vocabulary>> realBody = new HashMap<>();

        @SuppressWarnings("unchecked") Map<Object, Object> body = validateMapNotNullOrEmpty(dynamicVocabulary.get("body"), "body");

        for (Object o : body.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            ArrayList<Vocabulary> realVocList = new ArrayList<>();
            //Validate that pair value is a List

            @SuppressWarnings("unchecked") List<Object> vocList = validateListNotNullOrEmpty(pair.getValue(), "in body key:value pair, value");
            if (vocList == null || vocList.isEmpty())
                throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, vocabulary list is empty or null");


            for (Object vocHash : vocList) {
                //Validate that items are Maps

                @SuppressWarnings("unchecked") Iterator it2 = (validateMapNotNullOrEmpty(vocHash, "in body key:value pair, one of the value")).entrySet()
                                                                                                                                              .iterator();
                Vocabulary tempVoc = new Vocabulary();
                if (!it2.hasNext())
                    throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object");

                while (it2.hasNext()) {
                    Map.Entry pair2 = (Map.Entry) it2.next();
                    switch ((String) pair2.getKey()) {
                        case "literal":
                            tempVoc.setLiteral(validateStringNotNullOrEmpty(pair2.getValue(), "literal"));

                            break;
                        case "spoken":
                            @SuppressWarnings("unchecked") List<Object> localSpokenList =
                                    validateListNotNullOrEmpty(pair2.getValue(), "spoken");
                            for (Object spokenWord : localSpokenList) {
                                tempVoc.addSpoken(validateStringNotNullOrEmpty(spokenWord, "elements from spoken List"));
                            }
                            break;
                        case "canonical":
                            if(pair2.getValue() != null)
                            {
                                tempVoc.setCanonincal(validateStringNotNullOrEmpty(pair2.getValue(), "canonical"));
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, argument - " + (String) pair2.getKey() + " - is not a valid argument");

                    }
                }

                //Literal is mandatory
                if (tempVoc.getLiteral() == null || tempVoc.getLiteral().isEmpty())
                    throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, missing literal value");
                realVocList.add(tempVoc);
            }
            realBody.put((String) pair.getKey(), realVocList);

        }

        boolean persist = (boolean) dynamicVocabulary.getOrDefault("persist", false);
        String incrementalAction = (String) dynamicVocabulary.getOrDefault("incremental_action", "");

        VocabularyType vocabularyType = VocabularyType.APPLICATION_NUANCE_WORDSET;
        return new DynamicVocabulary(id, vocabularyType, realBody, persist, incrementalAction, requestConfig);
    }

    private ObjectMapper getObjectMapper()
    {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        return mapper;
    }

    public String getJsonBody() throws CobraException
    {
        ObjectMapper mapper = getObjectMapper();

        //Object to JSON in String
        String jsonInString;
        try
        {
            jsonInString = mapper.writeValueAsString(body);
        }
        catch (JsonProcessingException e)
        {
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.DYNAMIC_VOCABULARY_SERIALIZE_ERROR));
        }

        return jsonInString;
    }

    public static String validateStringNotNullOrEmpty(Object value, String name)
    {
        if(value != null && !(value instanceof String))
        {
            throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should be a String");
        }
        if(value == null || ((String)value).isEmpty())
        {
            throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty");
        }

        return (String)value;
    }

    public static Map<Object, Object> validateMap(Object value, String name)
    {
        if(value != null && !(value instanceof Map))
        {
            throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should be a Map");
        }

        return (Map<Object, Object>)value;
    }
    public static Map<Object, Object> validateMapNotNullOrEmpty(Object value, String name)
    {
        Map<Object, Object>tmpObj = validateMap(value, name);

        if(tmpObj == null || tmpObj.isEmpty())
        {
            throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty");
        }

        return tmpObj;
    }

    public static List<Object> validateList(Object value, String name)
    {
        if(value != null && !(value instanceof List))
        {
            throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, " + name + " is of invalid type, should have a List");
        }

        return (List<Object>)value;
    }
    public static List<Object> validateListNotNullOrEmpty(Object value, String name)
    {
        List<Object>tmpObj = validateList(value, name);

        if(tmpObj == null || tmpObj.isEmpty())
        {
            throw new IllegalArgumentException("Couldn't parse dynamicVocabularies object, " + name + " is missing or empty");
        }

        return tmpObj;
    }

    public Map<String, ArrayList<Vocabulary>> getBody()
    {
        return body;
    }
}
