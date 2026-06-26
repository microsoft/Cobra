package com.nuance.plugin.kryptonws.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.cobra.utils.webapi.commandbuilder.WebapiUploadDynamicVocabularyCommandBuilder;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.utils.KryptonWsSessionObjectsHelper;
import com.nuance.plugin.kryptonws.utils.exceptions.DuplicateObjectException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class KryptonWsLoadMethod
        extends KryptonWsMethod
{
    private static final Logger logger = LogManager.getLogger(KryptonWsLoadMethod.class);
    private JSONArray loadedObjects = new JSONArray();
    public static final String NAME = "Load";

    public KryptonWsLoadMethod(JSONObject jsonObject, KryptonWsSession kryptonWsSession)
    {
        super(jsonObject, kryptonWsSession);
        logger.debug("KryptonWsLoadMethod constructor");
    }

    @Override
    protected void onAudioImplementation(byte[] audio)
    {
        logger.trace("Load ignoring {} audio bytes", audio.length);
    }

    @Override
    public void process()
    {
        process(this);
    }

    public boolean process(KryptonWsMethod methodToReceiveReply)
    {
        logger.trace("process");

        super.process();

        try
        {

            if (methodToReceiveReply == this)
            {
                sendInitialResponse(true);
            }

            var kryptonWsSessionObjectsHelper = createObjectsParser(kryptonWsSession);
            JSONArray uploadDynamicVocabList =
                    kryptonWsSessionObjectsHelper.mergeSessionObjects(methodJson.optJSONArray("objects"), loadedObjects,
                            kryptonWsSession.getCreateSessionObjects());

            convertUrlToUri(uploadDynamicVocabList);

            kryptonWsResponseBuilder.setObjects(loadedObjects.toList());

            if (uploadDynamicVocabList.length() > 0)
            {
                var webapiUploadDynamicVocabularyCommandBuilder = new WebapiUploadDynamicVocabularyCommandBuilder(kryptonWsSession.getWebapiSessionId(),
                        Integer.toString(kryptonWsSession.getNextWebapiStepId()));

                webapiUploadDynamicVocabularyCommandBuilder.dynamicVocabularies(uploadDynamicVocabList);

                JSONObject webapiUploadDynamicVocabularyCommandJson =
                        webapiUploadDynamicVocabularyCommandBuilder.buildRequest();
                sendToWebapi(webapiUploadDynamicVocabularyCommandJson.toString(), methodToReceiveReply);
                return true;
            }
            else
            {
                if (methodToReceiveReply == this)
                {
                    sendCompleteResponse();
                    cleanup();
                }
            }
        }
        catch (DuplicateObjectException doe)
        {
            sendErrorResponse(doe.getMessage(), doe.getExtra());
            this.kryptonWsResponseBuilder.setErrors(null);
            this.kryptonWsResponseBuilder.setObjects(null);
            sendCompleteResponse();
            cleanup();
        }
        catch (Exception e)
        {
            sendError(e.getMessage());
        }
        return false;
    }

    private void convertUrlToUri(JSONArray uploadDynamicVocabList)
    {
        for (var i = 0; i < uploadDynamicVocabList.length(); i++)
        {
            var uploadDynamicVocab = uploadDynamicVocabList.getJSONObject(i);
            if (uploadDynamicVocab.has("url"))
            {
                var uri = uploadDynamicVocab.getString("url");
                uploadDynamicVocab.remove("url");
                uploadDynamicVocab.put("uri", uri);
            }
        }
    }

    public KryptonWsSessionObjectsHelper createObjectsParser(KryptonWsSession kryptonWsSession)
    {
        return new KryptonWsSessionObjectsHelper(kryptonWsSession);
    }

    public void sendError(String message)
    {
        sendErrorResponse(message, null);
        cleanup();
    }

    @Override
    public void processWebapiResponse(String webapiResponse)
    {
        WebApiResponse webApiResponse;

        try
        {
            webApiResponse = WebApiResponse.fromJson(webapiResponse);
        }
        catch (JsonProcessingException e)
        {
            sendError("Unable to parse webapi response json");
            return;
        }

        if (webApiResponse.isError())
        {
            sendErrorResponse(webApiResponse.getErrorsAsString(), null);
            cleanup();
            return;
        }

        sendCompleteResponse();
        cleanup();
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    // For unit tests
    protected void setLoadedObjects(JSONArray loadedObjects)
    {
        this.loadedObjects = loadedObjects;
    }

    protected JSONArray getLoadedObjects()
    {
        return loadedObjects;
    }
}
