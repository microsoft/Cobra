package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.engine.pdx.*;
import com.nuance.ncs.nmsp.engine.streamers.ToClientAudioStreamer;
import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.servlet.NcsServletDefaults;
import com.nuance.ncs.nmsp.servlet.NcsServletDefines;
import com.nuance.ncs.nmsp.servlet.authorization.AuthorizationService;
import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import com.nuance.ncs.nmsp.servlet.utils.JsonPdxConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;

public class TransactionEngine {
	private static final Logger log = LogManager.getLogger(TransactionEngine.class);

    private PdxClient pdxClient;
    private ResponseManager responseManager;
    private CallLogData callLogData;
    private RequestData requestData;

    // Only used with TTS params
    private String outCodecMimeType;

    private boolean initSuccessfull = false;

    public boolean setUpRequest(RequestData requestData, ResponseManager responseManager)
	{
    	log.debug("TransactionEngine: Setting up request. Creating PDX client.");

    	this.requestData = requestData;
    	this.responseManager = responseManager;

    	// If not authorized return error
		if (requestData.getCorsOrigin() != null &&
			!AuthorizationService.authorizeCrossOrigin(NcsServletDefaults.getAuthorizedCrossOrigins(), requestData.getCorsOrigin()))
		{
			log.error("TransactionEngine: Nmaid not authorized");
			this.responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST,
					"The provided origin is not an authorized cross-origin.");
			return false;
		}

    	// If not authorized return error
    	if (!AuthorizationService.authorizeAppIdAndKey(NcsServletDefaults.AUTHORIZED_NMAIDS, requestData.getAppID(), requestData.getAppKey())) {
    		log.error("TransactionEngine: Nmaid not authorized");
        	this.responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "The provided credentials are incorrect or not authorized to use this service. Please contact customer support.");
            return false;
    	}

    	// If not authorized return error
    	if (!AuthorizationService.authorizeGwIp(NcsServletDefaults.getAuthorizedIps(), requestData.getServerIP())) {
    		log.error("TransactionEngine: IP not authorized");
        	this.responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "The IP address is incorrect or not authorized to use this service. Please contact customer support.");
            return false;
    	}

    	outCodecMimeType = requestData.getOutCodecMimeType();

        boolean sslEnabled = "TRUE".equalsIgnoreCase(NcsServletDefaults.NMSP_SSL_SOCKET_ENABLE);
        boolean sslSelfSigned = "TRUE".equalsIgnoreCase(NcsServletDefaults.NMSP_SSL_SELF_SIGNED_CERT);

        pdxClient = new PdxClient(
                requestData.getServerIP(),
                requestData.getServerPort(),
                requestData.getAppID(),
                requestData.getAppKey(),
                requestData.getUserId(),
                requestData.getInCodec(),
                requestData.getOutCodec(),
                sslEnabled,
                sslSelfSigned,
                requestData.getCommandTimeout(),
                buildResponseListener()
        );

        boolean connected = pdxClient.connect();
        if (!connected) {
            return false;
        }

        try {
            pdxClient.loadResource(requestData.getAppName());
            pdxClient.beginCommand(
                    requestData.getCommandName(),
                    requestData.getAppName(),
                    requestData.getApplicationVersion(),
                    requestData.getDictationLanguage(),
                    requestData.getCarrier(),
                    requestData.getPhoneModel(),
                    requestData.getOptionalKeys()
            );
        } catch (Exception e) {
        	log.error("TransactionEngine: command creation failed with message: ", e);
        	this.responseManager.sendErrorReply(HttpServletResponse.SC_BAD_REQUEST, "Could not initiate request.");
            return false;
        }

        callLogData = new CallLogData(this, this.responseManager, this.requestData.getCorsOrigin());
        initSuccessfull = true;
        return true;
    }

    private PdxResponseListener buildResponseListener() {
        TransactionEngine self = this;
        return new PdxResponseListener() {
            private String sessionId = null;
            private boolean queryReplyReceived = false;

            @Override
            public void onConnected(String sid) {
                sessionId = sid;
                self.setSessionId(sid);
            }

            @Override
            public void onConnectionFailed(short reasonCode) {
                log.error("Connection to the GW failed reasonCode [" + reasonCode + "]!");
                if (sessionId == null)
                    self.sendMajorError(HttpServletResponse.SC_FORBIDDEN, "Connection refused. Reason: " + reasonCode + ". Please check credentials used.");
            }

            @Override
            public void onDisconnected(short reasonCode) {
                log.debug("Disconnected from the GW! Reason code is: " + reasonCode);
                if (sessionId == null)
                    self.sendMajorError(HttpServletResponse.SC_BAD_GATEWAY, "Connection terminated. Reason: " + reasonCode + ". Please check credentials used.");
                else if (!queryReplyReceived)
                    self.sendMajorError(HttpServletResponse.SC_BAD_REQUEST, "Connection terminated. Reason: " + reasonCode + ". The request sent is probably malformed or does not contain parameters.");
            }

            @Override
            public void onQueryResult(PdxQueryResult result) {
                log.debug("Received QueryResult, isFinal=" + result.isFinal());
                self.sendQueryResult(JsonPdxConverter.pdxDictionaryToJsonObject(result.getData()), result.isFinal());
                if (result.isFinal()) queryReplyReceived = true;
            }

            @Override
            public void onQueryError(PdxQueryError error) {
                log.debug("Received QueryError: " + error.getDescription());
                JSONObject json = new JSONObject();
                json.put("Error", error.getError());
                json.put("Description", error.getDescription());
                self.sendQueryError(json);
                queryReplyReceived = true;
            }

            @Override
            public void onQueryRetry(PdxQueryRetry retry) {
                log.debug("Received QueryRetry: " + retry.getName());
                JSONObject json = new JSONObject();
                json.put("Cause", retry.getCause());
                json.put("Name", retry.getName());
                json.put("Prompt", retry.getPrompt());
                self.sendQueryRetry(json);
                queryReplyReceived = true;
            }

            @Override
            public void onAudioReceived(byte[] audioData) {
                log.trace("onAudioReceived: " + audioData.length + " bytes");
            }

            @Override
            public void onError(Throwable cause) {
                log.error("Channel error: ", cause);
                if (!queryReplyReceived)
                    self.sendMajorError(HttpServletResponse.SC_BAD_GATEWAY, "Network error: " + cause.getMessage());
            }
        };
    }

    public PdxAudioSink sendAudioParam(String paramName)
    {
    	log.debug("TransactionEngine: Sending AudioParam");
    	if (!initSuccessfull) return null;
    	return pdxClient.createAudioSink(paramName);
    }

    public boolean sendChoiceParam(String paramName, String choice)
    {
    	log.trace("TransactionEngine: Sending ChoiceParam");
    	if (!initSuccessfull) return false;
    	return pdxClient.sendChoiceParam(paramName, choice);
    }

    public boolean sendDataParam(String paramName, byte[] data)
    {
    	log.trace("TransactionEngine: Sending DataParam");
    	if (!initSuccessfull) return false;
    	return pdxClient.sendDataParam(paramName, data);
    }

    public boolean sendTextParam(String paramName, String text)
    {
    	log.trace("TransactionEngine: Sending TextParam");
    	if (!initSuccessfull) return false;
    	return pdxClient.sendTextParam(paramName, text);
    }

    public boolean sendDictParam(String paramName, NmspDictionary dict)
    {
    	log.trace("TransactionEngine: Sending DictParam");
    	if (!initSuccessfull) return false;
    	return pdxClient.sendDictParam(paramName, dict);
    }

    public boolean sendSeqStartParam(String paramName, NmspDictionary dict)
    {
    	log.trace("TransactionEngine: Sending SeqStartParam");
    	if (!initSuccessfull) return false;
    	return pdxClient.sendSeqStartParam(paramName, dict);
    }

    public boolean sendSeqChunkParam(String paramName, NmspDictionary dict)
    {
    	log.trace("TransactionEngine: Sending SeqChunkParam");
    	if (!initSuccessfull) return false;
    	return pdxClient.sendSeqChunkParam(paramName, dict);
    }

    public boolean sendSeqEndParam(String paramName, NmspDictionary dict)
    {
    	log.trace("TransactionEngine: Sending SeqEndParam");
    	if (!initSuccessfull) return false;
    	return pdxClient.sendSeqEndParam(paramName, dict);
    }

    public boolean sendTtsParam(String paramName, NmspDictionary dict)
    {
    	log.trace("TransactionEngine: Sending TtsParam");
    	ToClientAudioStreamer audioSink = new ToClientAudioStreamer(this, paramName, outCodecMimeType);
    	if (!initSuccessfull) return false;
    	return pdxClient.sendTtsParam(paramName, dict, audioSink);
    }

    public boolean addCallLog(String jsonCallLog)
    {
    	log.trace("TransactionEngine: Adding CallLog");
    	if (callLogData != null)
    		return callLogData.addCallLog(jsonCallLog);

    	log.error("Attempting to add CallLog while callLog is disabled.");
    	return false;
    }

    public void endCommand()
    {
    	if (pdxClient == null) return;
    	pdxClient.endCommand();
    }

	public void sendQueryResult(JSONObject data, boolean finalResponse)
	{
		responseManager.sendJsonPart(NcsServletDefines.NCS_QUERY_RESULT, data, finalResponse, requestData.getCorsOrigin());
	}

	public void sendQueryError(JSONObject data)
	{
		responseManager.sendJsonPart(NcsServletDefines.NCS_QUERY_ERROR, data, true, requestData.getCorsOrigin());
	}

	public void sendQueryRetry(JSONObject data)
	{
		responseManager.sendJsonPart(NcsServletDefines.NCS_QUERY_RETRY, data, true, requestData.getCorsOrigin());
	}

	public void sendCallLogBytes(byte[] bytes)
	{
		if (pdxClient != null) {
			pdxClient.sendBcpLog(bytes);
		}
	}

    public void finalizeTransaction() {
    	if (pdxClient != null) {
    		pdxClient.freeResource();
    		pdxClient.shutdown();
    		pdxClient = null;
    	}
    }

	void sendMajorError(int status, String errorMsg) {
		log.error("Major error: " + errorMsg);
		responseManager.sendErrorReply(status, errorMsg);
	}

	void setSessionId(String sessionId) {
		responseManager.setSessionId(sessionId);
	}

	public void sendAudioPart(byte[] buffer, String paramName, String mimeType) {
		responseManager.sendAudioPart("Audio", buffer, paramName, mimeType, requestData.getCorsOrigin());
	}
}
