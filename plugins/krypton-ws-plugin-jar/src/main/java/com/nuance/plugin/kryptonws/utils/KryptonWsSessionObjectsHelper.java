package com.nuance.plugin.kryptonws.utils;

import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nuance.plugin.kryptonws.utils.exceptions.DuplicateObjectException;
import com.nuance.plugin.kryptonws.utils.exceptions.MissingSessionObjectException;
import com.nuance.plugin.kryptonws.utils.exceptions.InvalidIdException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.nuance.plugin.kryptonws.KryptonWsConfig.Property.KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN;

public class KryptonWsSessionObjectsHelper
{

    private static final Logger logger = LogManager.getLogger();

    public static final String WORDSET_TYPE = "application/x-nuance-wordset";
    public static final String WORDSET_PKG_TYPE = "application/x-nuance-wordset-pkg";
    public static final String WORDSET_NLU_PKG_TYPE = "application/x-nuance-wordset-nle-pkg";
    public static final String DLM_TYPE = "application/x-nuance-domainlm";
    public static final String SEMANTIC_MODEL_TYPE = "application/x-nuance-semantic-model";

    private static final String MISSING_URL = "Missing url";

    private static class LoadObject
    {
        String id;
        String url;
        String body;
    }

    private final KryptonWsSession kryptonWsSession;

    public KryptonWsSessionObjectsHelper(KryptonWsSession kryptonWsSession)
    {
        this.kryptonWsSession = kryptonWsSession;
    }

    /**
     * Merges the list of sessionObjects with non wordsets objects (dlms, wordset-pkg) and returns the uploadDynamicVocab command if needed
     *
     * @param newSessionObjects    The list of objects, expected to be formatted as per the WS API
     * @param loadedObjects        Populates this list with objects that were loaded
     * @param createSessionObjects Objects loaded at create session
     * @return The uploadDynamicVocabulary to perform command if required. Can return null if only dlms are defined
     * @throws DuplicateObjectException      If an id/url/word set is already in use
     * @throws InvalidIdException            If the ID length is less than 1
     * @throws MissingSessionObjectException If there is a missing object: body, url, objects to load
     */
    public JSONArray mergeSessionObjects(JSONArray newSessionObjects, JSONArray loadedObjects,
            JSONArray createSessionObjects)
            throws DuplicateObjectException, InvalidIdException, MissingSessionObjectException {
        if (newSessionObjects == null || newSessionObjects.length() == 0)
        {
            throw new MissingSessionObjectException("No objects to load.");
        }

        Map<String, JSONObject> sessionObjects = kryptonWsSession.getSessionObjects();
        Map<String, JSONObject> wordsetObjects = kryptonWsSession.getWordsetObjects();

        // verify if load request contains duplicate objects
        verifyNoDuplicateObjectsInRequest(newSessionObjects);

        var objectsToMerge = new ArrayList<JSONObject>();
        var dynamicVocabs = new JSONArray();

        processVocabs(newSessionObjects, loadedObjects, createSessionObjects, sessionObjects, objectsToMerge,
                dynamicVocabs);

        // Populate wordset objects from dynamicVocabs array
        for (Object obj : dynamicVocabs) {
            JSONObject dynamicVocab = (JSONObject) obj;
            wordsetObjects.put(dynamicVocab.getString("id"), dynamicVocab);
        }

        // Merges non wordsets to current session objects
        for (JSONObject jsonObject : objectsToMerge)
        {
            sessionObjects.put(jsonObject.getString("id"), jsonObject);
        }

        return dynamicVocabs;
    }

    @java.lang.SuppressWarnings("java:S3776")
    private List<JSONObject> processVocabs(JSONArray newSessionObjects, JSONArray loadedObjects,
            JSONArray createSessionObjects, Map<String, JSONObject> sessionObjects, List<JSONObject> objectsToMerge,
            JSONArray dynamicVocabs)
            throws InvalidIdException, DuplicateObjectException, MissingSessionObjectException
    {
        List<String> existingUrls = retrieveExistingUrls(sessionObjects);
        List<String> newIds = new ArrayList<>();
        List<String> newUrls = new ArrayList<>();

        for (var i = 0; i < newSessionObjects.length(); i++)
        {
            var jsonObject = newSessionObjects.getJSONObject(i);
            var loadObject = fromJson(jsonObject);

            verifyNoDuplicatesWithExistingObjects(loadObject, existingUrls, sessionObjects);

            if (!isCreateSessionDuplicate(loadObject, newUrls, newIds, jsonObject, createSessionObjects)
                && (createSessionObjects != newSessionObjects))
            {
                continue;
            }

            // Type is also optional for fetched resources
            var type = jsonObject.optString("type", null);

            if (type != null && type.equals(WORDSET_TYPE))
            {
                JSONObject parsedWordSet = parseWordSet(jsonObject);
                kryptonWsSession.addWordSet(parsedWordSet.getString("id"));
                loadedObjects.put(parsedWordSet); // Builds the list to send to Cobra
                dynamicVocabs.put(jsonObject);
            }
            else if (type != null && (type.equals(WORDSET_PKG_TYPE) || type.equals(WORDSET_NLU_PKG_TYPE)) )
            {
                JSONObject parsedWordSetPkg = parseWordsetPkg(jsonObject, type);
                kryptonWsSession.addWordSet(parsedWordSetPkg.getString("id"));
                loadedObjects.put(parsedWordSetPkg);
                dynamicVocabs.put(parsedWordSetPkg);
            }
            else // Fetched resources can be untyped
            {
                if (isSemanticModel(type, jsonObject))
                {
                    if (kryptonWsSession.getSemanticModel() == null)
                    { // Ignore new semantic models
                        loadedObjects.put(parseDlm(jsonObject));
                        kryptonWsSession.setSemanticModel(jsonObject);
                    }
                }
                else
                {
                    loadedObjects.put(parseDlm(jsonObject));
                    objectsToMerge.add(jsonObject);
                }
            }
        }
        return objectsToMerge;
    }

    private void verifyNoDuplicatesWithExistingObjects(LoadObject loadObject, List<String> existingUrls,
            Map<String, JSONObject> sessionObjects)
            throws DuplicateObjectException
    {
        if ((existingUrls.contains(loadObject.url)
             || sessionObjects.containsKey(loadObject.id)) &&
            !containsCreateSessionDuplicate(loadObject, kryptonWsSession.getCreateSessionObjects()))
        {

            String duplicateKey = (existingUrls.contains(loadObject.url)) ? "url" : "id";
            String duplicateValue = (duplicateKey.equals("url")) ? loadObject.url : loadObject.id;

            throw new DuplicateObjectException(duplicateKey).setExtra(duplicateValue);
        }
    }

    private void verifyNoDuplicateObjectsInRequest(JSONArray objectsToLoad)
            throws DuplicateObjectException, InvalidIdException, MissingSessionObjectException
    {
        List<String> ids = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        for (Object json : objectsToLoad)
        {
            var loadObject = fromJson((JSONObject) json);
            JSONObject objectAsJson = (JSONObject) json;

            if (loadObject.id.isEmpty())
            {
                throw new InvalidIdException();
            }
            String type = objectAsJson.optString("type");
            if (loadObject.body.isEmpty() &&
                (
                    type.equals(DLM_TYPE) ||
                    type.equals(WORDSET_PKG_TYPE) ||
                    type.equals(WORDSET_NLU_PKG_TYPE)
                )
                && loadObject.url.isEmpty())
            {
                throw new MissingSessionObjectException(MISSING_URL);
            }
            else if (loadObject.body.isEmpty() && type.equals(WORDSET_TYPE) && loadObject.url.isEmpty())
            {
                throw new MissingSessionObjectException("Missing body");
            }

            if (ids.contains(loadObject.id))
            {
                throw new DuplicateObjectException("id").setExtra(loadObject.id);
            }
            else if (urls.contains(loadObject.url))
            {
                throw new DuplicateObjectException("url").setExtra(loadObject.url);
            }

            ids.add(loadObject.id);
            urls.add(loadObject.url);
        }

    }

    private LoadObject fromJson(JSONObject objectToBuild)
            throws InvalidIdException
    {
        var loadObject = new LoadObject();
        loadObject.id = objectToBuild.optString("id", UUID.randomUUID()
                                                          .toString());
        loadObject.url = objectToBuild.optString("url", "");
        loadObject.body = objectToBuild.optString("body", "");

        if (loadObject.id.isEmpty())
        {
            throw new InvalidIdException();
        }
        return loadObject;
    }

    private boolean isCreateSessionDuplicate(LoadObject loadObject,
            List<String> newUrls,
            List<String> newIds,
            JSONObject objectToBuild,
            JSONArray createSessionObjects)
    {

        if (containsCreateSessionDuplicate(loadObject, createSessionObjects))
        {
            return false;
        }

        objectToBuild.put("id", loadObject.id); // must add ID, else nullPointer when merging

        if (loadObject.url.length() > 0)
        {
            newUrls.add(loadObject.url);
        }

        newIds.add(loadObject.id);

        return true;
    }

    private boolean containsCreateSessionDuplicate(LoadObject loadObject, JSONArray createSessionObjects)
    {
        for (Object obj : createSessionObjects)
        {
            Map<String, Object> objectAsJson = ((JSONObject) obj).toMap();

            if (objectAsJson.containsValue(loadObject.id)
                || objectAsJson.containsValue(loadObject.url)
                || objectAsJson.containsValue(loadObject.body))
            {
                return true;
            }
        }

        return false;
    }

    private List<String> retrieveExistingUrls(Map<String, JSONObject> givenObjects)
    {
        List<String> urls = new ArrayList<>();

        givenObjects.forEach(
                (k, v) -> {
                    if (v.has("url"))
                    {
                        urls.add(v.getString("url"));
                    }
                }
        );

        return urls;
    }

    private JSONObject parseWordSet(JSONObject jsonObject)
            throws MissingSessionObjectException
    {
        var dynamicVocab = new JSONObject();

        // Word sets require a body
        if (jsonObject.has("body"))
        {
            // Id is optional
            var id = jsonObject.optString("id", UUID.randomUUID()
                                                    .toString());
            dynamicVocab.put("id", id);

            // But we'll set it for the object to be loaded by cobra too if needed
            if (!jsonObject.has("id"))
            {
                jsonObject.put("id", id);
            }

            jsonObject.put("body", new JSONObject(jsonObject.getString("body")));
        }
        else
        {
            throw (new MissingSessionObjectException("Missing body"));
        }

        return dynamicVocab;
    }

    private JSONObject parseWordsetPkg(JSONObject jsonObject, String type)
            throws MissingSessionObjectException
    {
        var loadedObject = new JSONObject();

        processIds(jsonObject, loadedObject);

        if (!jsonObject.has("url"))
        {
            throw (new MissingSessionObjectException(MISSING_URL));
        }

        var url = jsonObject.getString("url");
        if (!url.matches("^urn:.*"))
        {
            throw (new MissingSessionObjectException("Only urns are supported for wordset pkg"));
        }

        loadedObject.put("url", replaceUrnSlashSlash(url));

        processCookies(jsonObject, loadedObject);

        loadedObject.put("type", type);

        return loadedObject;
    }

    private JSONObject parseDlm(JSONObject jsonObject)
            throws MissingSessionObjectException
    {
        var loadedObject = new JSONObject();

        processIds(jsonObject, loadedObject);

        // Dlms require an url
        if (jsonObject.has("url"))
        {
            processCookies(jsonObject, loadedObject);

            try
            {
                var uri = getObjectURI(jsonObject);
                if (uri.getScheme()
                       .equalsIgnoreCase("urn"))
                {
                    logger.trace(uri.getQuery());
                    Map<String, String> params = getObjectParams(uri);
                    String[] tokens = uri.getSchemeSpecificPart()
                                         .split("(\\?=?|;)");
                    jsonObject.put("url", tokens[0]);
                    if (params.containsKey("dlm_weight"))
                    {
                        jsonObject.put("weight", params.get("dlm_weight"));
                    }
                    var langKey = "language";
                    jsonObject.put("url",
                            String.format("%s:%s?=%s=%s", uri.getScheme(), tokens[0], langKey, params.get(langKey)));
                }
            }
            catch (URISyntaxException e)
            {
                logger.trace("Couldn't parse dlm uri {}", jsonObject.getString("uri"));
            }
        }
        else
        {
            throw (new MissingSessionObjectException(MISSING_URL));
        }
        return loadedObject;
    }

    private void processIds(JSONObject jsonObject, JSONObject loadedObject)
    {
        // Id is optional
        loadedObject.put("id", jsonObject.optString("id", UUID.randomUUID()
                                                              .toString()));
        jsonObject.put("id", loadedObject.getString("id")); // In case we had to generate the id
    }

    private void processCookies(JSONObject jsonObject, JSONObject loadedObject)
    {
        var cookiesKey = "cookies";

        // Krypton WS returns cookies if defined
        if (jsonObject.has(cookiesKey))
        {
            loadedObject.put(cookiesKey, jsonObject.get(cookiesKey));
        }
    }

    /**
     * Sets the list of dlms in the session config for speech reco.
     *
     * @param dlmIds Map of (id,weight)
     */
    @java.lang.SuppressWarnings({"java:S3740", "java:S3776"})
    public void setSessionObjectsForSpeechRecognition(Map<String, Object> dlmIds)
    {
        CombinedConfiguration sessionConfig = kryptonWsSession.getWebapiSessionConfig();
        sessionConfig.removeConfiguration("kryptonWsPluginDlmConfig");

        Map<String, JSONObject> sessionObjects = kryptonWsSession.getSessionObjects();
        if (sessionObjects.size() > 0)
        {
            var language = kryptonWsSession.getCreateSessionLanguagePack()
                                           .getString("language");

            List<Map<String, Object>> domainLms = new ArrayList<>(sessionObjects.size());
            var weightKey = "weight";

            // Going through all the session objects
            for (Map.Entry<String, JSONObject> sessionObjectEntry : sessionObjects.entrySet())
            {
                String dlmId = sessionObjectEntry.getKey();
                JSONObject sessionObject = sessionObjectEntry.getValue();
                Map<String, Object> newResource = new HashMap<>();

                var url = sessionObject.getString("url");
                if (isDatapackOnlyUrl(url))
                {
                    logger.info("Skipping {}", url);
                    continue;
                }
                newResource.put("uri", url);
                newResource.put("id", dlmId);
                // If the speech reco defines weight, they override those set in the session
                if (dlmIds.containsKey(dlmId))
                {
                    newResource.put(weightKey, dlmIds.get(dlmId));
                }
                else
                {
                    if (sessionObject.has(weightKey))
                    {
                        newResource.put(weightKey, sessionObject.get(weightKey));
                    }
                }
                var requestTimeoutKey = "requestTimeout";
                if (sessionObject.has(requestTimeoutKey))
                {
                    newResource.put(requestTimeoutKey, sessionObject.get(requestTimeoutKey));
                }
                domainLms.add(newResource);
            }

            var nodeBuilder = new ImmutableNode.Builder().name(language)
                                                         .value(domainLms);

            var baseConfig = new WebApiJSONConfiguration();
            baseConfig.addNodes("kryptonDomainLmsPerLanguage", List.of(nodeBuilder.create()));
            sessionConfig.addConfiguration(baseConfig, "kryptonWsPluginDlmConfig");
        }
    }

    private boolean isDatapackOnlyUrl(String url)
    {
        var pattern = kryptonWsSession.getPluginConfig()
                                      .getString(KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN.getKey());

        logger.trace("Datapack only pattern = {}. Url = {}", pattern, url);

        return url.matches(pattern);
    }

    private URI getObjectURI(JSONObject jsonObject)
            throws URISyntaxException
    {
        var url = jsonObject.getString("url");
        url = replaceUrnSlashSlash(url);
        return new URI(url);
    }

    private String replaceUrnSlashSlash(String urn)
    {
        return urn.replaceAll("^urn://", "urn:");
    }

    private Map<String, String> getObjectParams(URI uri)
    {
        String[] tokens = uri.getSchemeSpecificPart()
                             .split("(\\?=?|;)");
        Map<String, String> params = new HashMap<>();
        for (var i = 1; i < tokens.length; i++)
        {
            String[] pair = tokens[i].split("=");
            params.put(pair[0], pair[1]);
        }
        return params;
    }

    private boolean isSemanticModel(String type, JSONObject jsonObject)
    {
        if (type != null && type.equals(SEMANTIC_MODEL_TYPE))
        {
            return true;
        }

        try
        {
            var uri = getObjectURI(jsonObject);
            if (uri.getScheme()
                   .equalsIgnoreCase("urn"))
            {
                logger.trace(uri.getQuery());
                Map<String, String> params = getObjectParams(uri);
                if (params.containsKey("nlptype") && params.get("nlptype")
                                                           .equalsIgnoreCase("nle"))
                {
                    return true;
                }
            }
        }
        catch (URISyntaxException e)
        {
            logger.trace("Couldn't parse dlm uri {}", jsonObject.getString("uri"));
        }


        return false;
    }
}
