package com.nuance.cobra.session;

import com.nuance.cobra.utils.db.redis.RedisCommandsWrapper;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class DynamicVocabularyDb
{
    private static final Logger logger = LogManager.getLogger(DynamicVocabularyDb.class);

    DynamicVocabularyDb() { throw new IllegalStateException("Utility class!"); }

    public static String saveDynamicVocabToDb(CombinedConfiguration requestConfig, Map<String, ArrayList<DynamicVocabulary.Vocabulary>> body)
    {
        List<String> userIdAppKeyRedisUri = getUserIdAppKeyRedisUri(requestConfig, "Cannot persist dynamic vocab, {} not provided");
        if(userIdAppKeyRedisUri.isEmpty()) { return ""; }

        String userId = userIdAppKeyRedisUri.get(0);
        String appKey = userIdAppKeyRedisUri.get(1);
        String redisUri = userIdAppKeyRedisUri.get(2);

        String redisKey;

        Map<String, String> msetMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<DynamicVocabulary.Vocabulary>> entry : body.entrySet())
        {
            JSONArray literalSpokenListJson = new JSONArray(entry.getValue());
            JSONObject conceptWithLiteralSpokenList = new JSONObject().put(entry.getKey(), literalSpokenListJson);
            redisKey = getWordsetKey(userId, appKey, entry.getKey());
            logger.debug("Setting in redis: {} = {}", redisKey, conceptWithLiteralSpokenList);
            msetMap.put(redisKey, conceptWithLiteralSpokenList.toString());
        }
        return conceptsAddToRedis(redisUri, appKey, userId, msetMap, body.keySet());
    }

    private static List<String> getUserIdAppKeyRedisUri(CombinedConfiguration requestConfig, String errorStringWithOneParam)
    {
        String redisUri = requestConfig.getString(WebApiConfig.Property.PERSISTENCE_REDIS_URI.key, null);
        if(redisUri == null)
        {
            logger.debug(errorStringWithOneParam, WebApiConfig.Property.PERSISTENCE_REDIS_URI.key);
            return List.of();
        }

        String userId = requestConfig.getString(WebApiConfig.Property.CLIENT_USER_ID.key, WebApiConfig.NOT_DEFINED);
        if(userId.equals(WebApiConfig.NOT_DEFINED))
        {
            logger.error(errorStringWithOneParam, WebApiConfig.Property.CLIENT_USER_ID.key);
            return List.of();
        }

        String appKey = requestConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key);
        return List.of(userId, appKey, redisUri);
    }

    private static String conceptsAddToRedis(String redisUri, String appKey, String userId, Map<String, String> msetMap, Set<String> concepts)
    {
        if(msetMap.isEmpty() || concepts.isEmpty()) { return ""; }

        RedisCommandsWrapper.sadd(getWordsetsKey(userId, appKey), appKey, concepts, redisUri);
        return RedisCommandsWrapper.mset(msetMap, appKey, redisUri);
    }

    private static String getWordsetKey(String userId, String appKey, String conceptName)
    {
        return String.format("userId:%s:tenantId:%s:wordset:%s", userId, appKey, conceptName);
    }

    private static String getWordsetsKey(String userId, String appKey)
    {
        return String.format("userId:%s:tenantId:%s:wordsets", userId, appKey);
    }

    public static Set<String> getPersistedWordsets(CombinedConfiguration config)
    {
        List<String> userIdAppKeyRedisUri = getUserIdAppKeyRedisUri(config, "Cannot read persisted wordsets, {} not provided");
        if(userIdAppKeyRedisUri.isEmpty()) { return Set.of(); }

        String userId = userIdAppKeyRedisUri.get(0);
        String appKey = userIdAppKeyRedisUri.get(1);
        String redisUri = userIdAppKeyRedisUri.get(2);

        String redisKey = String.format("userId:%s:tenantId:%s:wordsets", userId, appKey);

        return RedisCommandsWrapper.smembers(redisKey, appKey, redisUri);
    }

    public static DynamicVocabulary getWordset(String conceptName, CombinedConfiguration config)
    {
        List<String> userIdAppKeyRedisUri = getUserIdAppKeyRedisUri(config, "Cannot get wordset, {} not provided");
        if(userIdAppKeyRedisUri.isEmpty()) { return null; }

        String userId = userIdAppKeyRedisUri.get(0);
        String appKey = userIdAppKeyRedisUri.get(1);
        String redisUri = userIdAppKeyRedisUri.get(2);

        String redisKey = getWordsetKey(userId, appKey, conceptName);
        String jsonStringWordset = RedisCommandsWrapper.get(redisKey, appKey, redisUri);
        JSONObject body = new JSONObject(jsonStringWordset);

        JSONObject dynamicVocabJson = new JSONObject();
        dynamicVocabJson.put("id", String.format("%s-%s-%s", userId, appKey, conceptName));
        dynamicVocabJson.put("type", "application/x-nuance-wordset");
        dynamicVocabJson.put("body", body);

        return DynamicVocabulary.fromJsonMap(dynamicVocabJson.toMap(), false, config);
    }

    public static Long deleteUserFromDb(CombinedConfiguration config)
    {
        List<String> userIdAppKeyRedisUri = getUserIdAppKeyRedisUri(config, "Cannot delete user from db. {} not provided");
        if(userIdAppKeyRedisUri.isEmpty()) { return (long) -1; }

        String userId = userIdAppKeyRedisUri.get(0);
        String appKey = userIdAppKeyRedisUri.get(1);
        String redisUri = userIdAppKeyRedisUri.get(2);

        //thwart key injection attacks
        if(!userId.matches("[A-Za-z0-9_-]+"))
        {
            logger.warn("Tried to delete invalid user id: {}", userId);
            return (long) -2;
        }

        try
        {
            return deleteUserFromDbExec(redisUri, userId, appKey);
        } catch (Exception e)
        {
            logger.warn("Caught exception while trying to delete user", e);
            return (long) -4;
        }
    }

    private static Long deleteUserFromDbExec(String redisUri, String userId, String appKey)
    {
        String keyPattern = String.format("userId:%s:tenantId:%s:*", userId, appKey);

        List<String> keys = RedisCommandsWrapper.keys(keyPattern, appKey, redisUri);
        if(keys.isEmpty()) { return (long) -3; }

        return RedisCommandsWrapper.del(keys, appKey, redisUri);
    }

    public static boolean incrementVocabInDb(CombinedConfiguration requestConfig, String action, Map<String, ArrayList<DynamicVocabulary.Vocabulary>> body)
    {
        List<String> userIdAppKeyRedisUri = getUserIdAppKeyRedisUri(requestConfig, "Cannot incremental add dynamic vocab, {} not provided");
        if(userIdAppKeyRedisUri.isEmpty()) { return false; }

        String userId = userIdAppKeyRedisUri.get(0);
        String appKey = userIdAppKeyRedisUri.get(1);
        String redisUri = userIdAppKeyRedisUri.get(2);

        String redisKey;

        Map<String, String> msetMap = new HashMap<>();
        Set<String> conceptsToAdd = new HashSet<>();
        for (Map.Entry<String, ArrayList<DynamicVocabulary.Vocabulary>> entry : body.entrySet())
        {
            String conceptName = entry.getKey();
            redisKey = getWordsetKey(userId, appKey, conceptName);
            JSONObject conceptWithLiteralSpokenListFromDb = RedisCommandsWrapper.getConceptAsJson(redisKey, appKey, redisUri, conceptName);
            JSONObject conceptWithLiteralSpokenListToSave = incrementConcept(conceptWithLiteralSpokenListFromDb, conceptName, action, entry.getValue());

            if(conceptWithLiteralSpokenListToSave.getJSONArray(conceptName).isEmpty())
            {
                RedisCommandsWrapper.srem(getWordsetsKey(userId, appKey), appKey, Set.of(conceptName), redisUri);
                RedisCommandsWrapper.del(List.of(redisKey), appKey, redisUri);
            } else {
                logger.debug("Setting in redis: {} = {}", redisKey, conceptWithLiteralSpokenListToSave);
                msetMap.put(redisKey, conceptWithLiteralSpokenListToSave.toString());
                conceptsToAdd.add(conceptName);
            }
        }
        conceptsAddToRedis(redisUri, appKey, userId, msetMap, conceptsToAdd);
        return true;
    }

    private static JSONObject incrementConcept(JSONObject conceptFromDb, String conceptName, String action, ArrayList<DynamicVocabulary.Vocabulary> delta)
    {
        String literalIncrement = delta.get(0).getLiteral();
        String spokenIncrement = delta.get(0).getSpoken().get(0);
        String canonicalIncrement = delta.get(0).getCanonical();

        JSONArray literalSpokenCanonicalsForConceptFromDb = conceptFromDb.getJSONArray(conceptName);

        //Find the literal
        for (int i = 0; i < literalSpokenCanonicalsForConceptFromDb.length(); i++)
        {
            JSONObject literalSpokenCanonicalForConceptFromDb = literalSpokenCanonicalsForConceptFromDb.getJSONObject(i);
            if(literalSpokenCanonicalForConceptFromDb.optString("literal").equals(literalIncrement))
            {
                if (action.equals("remove"))
                {
                    literalSpokenCanonicalsForConceptFromDb.remove(i);
                }
                else //add
                {
                    if(spokenIncrement != null)
                    {
                        JSONArray spokenJsonArray = literalSpokenCanonicalForConceptFromDb.optJSONArray("spoken");
                        if(spokenJsonArray == null)
                        {
                            literalSpokenCanonicalForConceptFromDb.put("spoken", spokenJsonArray);
                        } else if (!spokenJsonArray.toList().contains(spokenIncrement))
                        {
                            spokenJsonArray.put(spokenIncrement);
                        }
                    }
                    if(canonicalIncrement != null)
                    {
                        literalSpokenCanonicalForConceptFromDb.put("canonical", canonicalIncrement);
                    }
                }
                return conceptFromDb;
            }
        }

        //Literal was not found
        if (action.equals("add"))
        {
            literalSpokenCanonicalsForConceptFromDb.put(new JSONObject(delta.get(0)));
        }
        return conceptFromDb;
    }
}
