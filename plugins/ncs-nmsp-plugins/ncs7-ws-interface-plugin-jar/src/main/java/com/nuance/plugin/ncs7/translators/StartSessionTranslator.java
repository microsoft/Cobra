package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.hash.CobraHash;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs.utils.HashType;
import com.nuance.plugin.ncs7.Ncs7InterfaceConfig;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.nuance.plugin.ncs7.Ncs7InterfaceConfig.Property.*;

public class StartSessionTranslator extends AbstractCommandTranslator {

    public StartSessionTranslator(Orchestrator orchestrator, NcsRequest ncsRequest) {
        super(orchestrator, ncsRequest);
        orchestrator.setSessionId(UUID.randomUUID().toString());

        JSONObject json = new JSONObject();
        json.put("session_id", orchestrator.getSessionId());
        json.put("message", "connected");
        respondToClient(orchestrator, json.toString());
    }

    @Override
    public void execute() throws IOException {
        //we are starting a new session. Make sure the buffers are empty
        orchestrator.webapiMessageBufferQueue.clear();
        doRequest(ncsRequest);
        waitForResponse();
    }

    @Override
    public void commandCleanup() {
        //nothing for now
    }

    private void doRequest(NcsRequest ncsRequest) throws IOException
    {
        JSONObject auth = new JSONObject();
        String authType = orchestrator.getWebApiFeatures().getConfigs().getString(AUTH_TYPE.getKey(), AUTH_TYPE.getDefaultValue().toString());
        auth.put("type", authType);
        if(authType.equals(Ncs7InterfaceConfig.ValidationTypes.HASHCODE.toString())) {
            auth.put("data", CobraHash.sha256(getRequestOrQueryParam(ncsRequest.app_key, "app_key")));
        }
        else if(authType.equals(Ncs7InterfaceConfig.ValidationTypes.JWS.toString())) {
            auth.put("data", getRequestOrQueryParam(ncsRequest.app_key, "app_key"));
        }

        String appId = getRequestOrQueryParam(ncsRequest.app_id, "app_id");
        JSONObject parameters = new JSONObject();
        parameters.put("authentication", auth);
        parameters.put("applicationKey", appId);
        orchestrator.setAppId(appId);

        String salt = orchestrator.getWebApiFeatures().getConfigs().getString(HASH_SECRET.getKey(), HASH_SECRET.getDefaultValue().toString());
        HashType hashType = HashType.fromValue(orchestrator.getWebApiFeatures().getConfigs().getString(HASH_TYPE.getKey(), HASH_TYPE.getDefaultValue().toString()));

        parameters.put("clientUserId", ncsRequest.getUserId(hashType, salt));
        parameters.put("clientDeviceId", ncsRequest.getDeviceId(hashType, salt));
        parameters.put("clientSubscriberId", ncsRequest.getSubscriberId(hashType, salt));
        parameters.put("clientSessionId", orchestrator.getSessionId());
        parameters.put("clientPhoneModel", ncsRequest.getPhoneModel());

        Map<String, Object> clientMetadata = populateClientMetadata(ncsRequest);
        if (clientMetadata.size() > 0)
        {
            parameters.put("clientDetails", clientMetadata);
        }

        JSONObject command = new JSONObject();
        command.put("parameters", parameters);
        command.put("name", "startSession");
        command.put("id", getCommandID(ncsRequest));

        JSONObject webApiRequest = new JSONObject();
        webApiRequest.put("type", "command");
        webApiRequest.put("command", command);

        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }

    Map<String, Object> populateClientMetadata(NcsRequest ncsRequest)
    {
        Map<String, Object> clientMetadata = new HashMap<>();
        addToMap(clientMetadata, "context_tag", ncsRequest.context_tag);
        addToMap(clientMetadata, "locale", ncsRequest.locale);
        addToMap(clientMetadata, "application_name", ncsRequest.application_name);
        addToMap(clientMetadata, "application", ncsRequest.application);
        addToMap(clientMetadata, "field_id", ncsRequest.field_id);
        addToMap(clientMetadata, "phone_OS", ncsRequest.phone_OS);
        addToMap(clientMetadata, "utterance_number", ncsRequest.utterance_number);
        addToMap(clientMetadata, "ui_language", ncsRequest.ui_language);
        addToMap(clientMetadata, "nmaid", ncsRequest.nmaid);
        addToMap(clientMetadata, "audio_source", ncsRequest.audio_source);
        addToMap(clientMetadata, "location", ncsRequest.location);
        addToMap(clientMetadata, "client_hardware_model", ncsRequest.client_hardware_model);
        addToMap(clientMetadata, "client_handware_model", ncsRequest.client_handware_model);
        addToMap(clientMetadata, "client_hardware_submodel", ncsRequest.client_handware_model);
        addToMap(clientMetadata, "nmt_device_model", ncsRequest.client_handware_model);
        addToMap(clientMetadata, "nmt_connection_type", ncsRequest.client_handware_model);
        addToMap(clientMetadata, "nmt_app_version_build", ncsRequest.client_handware_model);
        addToMap(clientMetadata, "phone_submodel", ncsRequest.client_handware_model);
        addToMap(clientMetadata, "phone_network", ncsRequest.client_handware_model);
        addToMap(clientMetadata, "uid", ncsRequest.uid);
        addToMap(clientMetadata, "application_state_id", ncsRequest.application_state_id);
        addToMap(clientMetadata, "deviceModel", ncsRequest.getDeviceModel());
        addToMap(clientMetadata, "datacollection_prompt", ncsRequest.datacollection_prompt);
        addToMap(clientMetadata, "datacollection_prompt_type", ncsRequest.datacollection_prompt_type);
        addToMap(clientMetadata, "datacollection_name", ncsRequest.datacollection_name);
        addToMap(clientMetadata, "datacollection_participant_id", ncsRequest.datacollection_participant_id);

        if(ncsRequest.getCodecId() != null) {
            addToMap(clientMetadata, "deviceCodec", ncsRequest.getCodecId().toString());
        }
        if(ncsRequest.datacollection_prompt_index != null) {
            addToMap(clientMetadata, "datacollection_prompt_index", ncsRequest.datacollection_prompt_index.toString());
        }
        return clientMetadata;
    }

    private void addToMap(Map<String, Object> clientMetadata, String metadataKey, Object metadataVal)
    {
        if(null != metadataKey && metadataVal != null)
        {
            clientMetadata.put(metadataKey,metadataVal);
        }
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        if (webApiResponse.isSuccess()) {
            orchestrator.setWebapiSessionID(webApiResponse.sessionId);
            // There's no need for a response to the client, the only important info is the sessionId
            return new ExecutionResult(ExecutionStatus.SUCCESS, true);
        } else {
            return doFinal(orchestrator, webApiResponse);
        }
    }
}
