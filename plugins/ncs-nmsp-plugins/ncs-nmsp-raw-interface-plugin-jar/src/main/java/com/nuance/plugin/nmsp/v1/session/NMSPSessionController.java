/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.session;

import com.nuance.plugin.nmsp.v1.nmspgw.client.IAudioListener;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.client.IClientHandler.DisconnectReason;
import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.*;
import com.nuance.plugin.nmsp.v1.session.Session.STATE;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * Implements the core logic of the GW. Implements the ISessionController methods and listens to event from NMAS and
 * NSS.
 *
 * @author epigeon
 */
public class NMSPSessionController implements ISessionController {
    private static Logger logger = ESAPI.getLogger(NMSPSessionController.class);

    private NMSPSessionController() {
    }

    public static NMSPSessionController getInstance() {
        return NMSPSessionControllerHolder.singleton;
    }

    /*************** ISessionController Interface ***************/
    public void clientConnected(Session session) {
        // Change timeout from connect timeout to session idle timeout
        session.setSessionTimeoutSecs(480);
        session.start();
    }

    @Override
    public void clientDisconnected(Session session, int reason) {
        if (session.getState() == STATE.DISCONNECTED) {
            return;
        }

        session.end(reason);
    }

    @Override
    public void loadResource(Session session, short transactionId, long requestId, long resourceId, LoadResourceData loadResourceData,
                             ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void freeResource(Session session, short transactionId, long resourceId, long timeout) {
        // Do nothing
    }

    @Override
    public void beginTransaction(Session session, short transactionId, byte[] pdxPayload, HashMap<String, Object> queryBeginInfo,
                                 short version) {
        if (session.getState() == Session.STATE.CONNECTED) {

            int sessionNumTransactions = session.getNumTransactions();
            if (sessionNumTransactions >= 256) {
                logger.warning(Logger.EVENT_UNSPECIFIED, "Maximum number of transactions per session reached " + sessionNumTransactions);
                session.getClient().sendDisconnect(session, DisconnectReason.CRITICAL_ERROR);
                return;
            }
            if (sessionNumTransactions >= 60) {
                long currentTime = System.currentTimeMillis();
                long sessionStartTime = session.getSessionCreationTime();
                float sessionDurationInSecs = (currentTime - sessionStartTime) / 1000f;
                float transactionRatePerMinute = 60 * sessionNumTransactions / sessionDurationInSecs;
                if (transactionRatePerMinute > 60) {
                    logger.warning(Logger.EVENT_UNSPECIFIED, "Maximum number of transactions per minute reached " + transactionRatePerMinute + ", " + sessionNumTransactions + " in " + sessionDurationInSecs + " seconds");
                    session.getClient().sendDisconnect(session, DisconnectReason.CRITICAL_ERROR);
                    return;
                }
            }
            session.setNumTransactions(sessionNumTransactions + 1);

            // Reset session idle timeout in case a previous command changed it in BCP_END
            session.setSessionTimeoutSecs(480);

            // Wait until we get a response to increase this metric

            session.setCurrentTransactionId(transactionId);

            // Legacy workaround for specific NMAIDs (PTNCS-24050)
            if (session.getNmaid().equals("NCS_PateoPSA_VoiceRecognitionPCS20150206210026") || session.getNmaid().equals("NCS_PateoDPCA_VoiceRecognitionPCS20150512082451")) {
                queryBeginInfo.put("CONFIDENTIAL_INFO_PATEO_VIN_PTNCS_24050", session.getUserId());
            }
            // End legacy workaround (PTNCS-24050)

            //for new tts service. ToCodecString in session is mandantory for invoking tts speak so we need handle it here. For httpv2 requests,
            //toCodecString should have been set in previous logic, however for httpv1 requests, we should set it here since session is not
            //accessible in previous logic(specifically in NMDPTTSCMDServlet.sendNMASRequest method.

            //for HTTP tts requests, the flag should have been set. If not set yet, the request should be from NMSP SDK,
            //so we set it to true. Also this is the only one case which would keep nuance frames.
            if (StringUtils.isEmpty((String) queryBeginInfo.get("tts_keep_nuance_frame"))) {
                queryBeginInfo.put("tts_keep_nuance_frame", "true");
            }
            session.putBcpMessagePdxDicts(queryBeginInfo);
        }
    }

    @Override
    public void sendDataToNmas(Session session, short transactionId, long requestId, byte[] pdxPayload) {
        // Do nothing
    }

    @Override
    public void sendDataToClient(Session session, short transactionId, long requestId, byte[] pdxPayload) {
        IClientHandler clientHandler = session.getClient();
        if (clientHandler != null) {
            clientHandler.sendData(session, transactionId, requestId, pdxPayload);
        } else {
            logger.debug(Logger.EVENT_UNSPECIFIED, "client no longer connected " + session.getSessionIdString());
        }
    }

    @Override
    public void recognize(Session session, short transactionId, long requestId, long resourceId, RecognizeData recognizeData,
                          ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void setParams(Session session, short transactionId, long requestId, long resourceId, Vector<Parameter> parameters,
                          boolean sendResponse, ISessionListener sessionListener) {
        int statusCode = 200;
        StringBuilder payload = new StringBuilder();
        boolean onlyLogParams = true;
        boolean hasInvalidParams = false;

        List<Parameter> nssParameters = new ArrayList<>();
        HashMap<String, String> gwCallLogParams = new HashMap<>();

        for (Parameter param : parameters) {
            switch (param.getType()) {
                case XML:
                case URI:
                case SIP:
                case SDP:
                case EXT:
                    hasInvalidParams = true;
                    appendParam(payload, param);
                    break;
                case NSSLOG:
                case SLOG:
                    nssParameters.add(param);
                    break;
                case GWLOG:
                    gwCallLogParams.put(param.getName(), param.getValue());
                    break;
                case NMSP:
                    onlyLogParams = false;
                    Integer newStatusCode = handleNMSPParams(session, param);
                    if(newStatusCode != null) {
                        hasInvalidParams = true;
                        statusCode = newStatusCode;
                        appendParam(payload, param);
                    }
                    break;
                case APP:
                    onlyLogParams = false;
                    hasInvalidParams = true; // We currently support no APP parameters
                    appendParam(payload, param);
                    break;
                case NSS:
                case SVSP:
                    onlyLogParams = false;
                    nssParameters.add(param);
                    break;
            }
        }

        logGwCallParams(gwCallLogParams);

        // Send a response only if sendResponse was true and at least one param wasn't a logging param
        sendResponse = sendResponse && !onlyLogParams;

        if (!nssParameters.isEmpty()) {
            for (Parameter param : nssParameters) {
                appendParam(payload, param);
            }
            nssParameters.addAll(session.getResourceParameters());
        } else if (sendResponse) { // No NSS params
            if (hasInvalidParams) { // some parameters valid and some invalid
                statusCode = 201;
            }
            sessionListener.response(session, transactionId, requestId, 200, 0x0001, 0x0000);
            sessionListener.setParamsComplete(session, transactionId, requestId, statusCode, payload.toString().getBytes());
        }

    }

    private Integer handleNMSPParams(Session session, Parameter param) {
        try {
            if (param.getName().equalsIgnoreCase(Session.PING_INTERVAL_SECS_STRING)) {
                session.setPingIntervalSecs(Integer.parseInt(param.getValue()));
            } else if (param.getName().equalsIgnoreCase(Session.PING_MISSED_RESPONSES_BEFORE_DISCONNECT)) {
                session.setMissedPingResponsesBeforeDisconnect(Integer.parseInt(param.getValue()));
            } else if (param.getName().equalsIgnoreCase(Session.SESSION_TIMEOUT_SECS_PARAM_NAME)) {
                session.setSessionTimeoutSecs(Long.parseLong(param.getValue()));
            } else {
                return 403;
            }
            return null;
        } catch (NumberFormatException e) {
            return 404;
        }
    }

    private void logGwCallParams(HashMap<String, String> gwCallLogParams) {
        if (!gwCallLogParams.isEmpty() &&
                logger.isTraceEnabled()) {
            for (Entry<String, String> param : gwCallLogParams.entrySet()) {
                logger.trace(Logger.EVENT_UNSPECIFIED, param.getKey() + "=" + param.getValue());
            }
        }
    }

    @Override
    public void getParams(Session session, short transactionId, long requestId, long resourceId, Vector<Parameter> parameters,
                          ISessionListener sessionListener) {
        int statusCode = 200;
        boolean hasInvalidParams = false;
        StringBuilder payload = new StringBuilder();
        List<Parameter> nssParameters = new ArrayList<>();

        for (Parameter param : parameters) {
            switch (param.getType()) {
                case XML:
                case URI:
                case SLOG:
                case NSSLOG:
                case GWLOG:
                case EXT:
                case SIP:
                case SDP:
                case APP:
                    hasInvalidParams = true;
                    appendParam(payload, param);
                    break;
                case NMSP:
                    if (param.getName().equalsIgnoreCase(Session.PING_INTERVAL_SECS_STRING)) {
                        prependSemiColon(payload);
                        payload.append(String.valueOf(session.getPingIntervalSecs()));
                    } else if (param.getName().equalsIgnoreCase(Session.PING_MISSED_RESPONSES_BEFORE_DISCONNECT)) {
                        prependSemiColon(payload);
                        payload.append(String.valueOf(session.getMissedPingResponsesBeforeDisconnect()));
                    } else if (param.getName().equalsIgnoreCase(Session.SESSION_TIMEOUT_SECS_PARAM_NAME)) {
                        prependSemiColon(payload);
                        payload.append(String.valueOf(session.getSessionTimeoutSecs()));
                    } else {
                        hasInvalidParams = true;
                        statusCode = 403;
                        appendParam(payload, param);
                    }
                    break;
                case NSS:
                case SVSP:
                    nssParameters.add(param);
                    break;
            }
        }

        if (!nssParameters.isEmpty()) {
            for (Parameter param : nssParameters) {
                appendParam(payload, param);
            }
        } else {
            if (hasInvalidParams) { // some parameters valid and some invalid
                statusCode = 201;
            }
            sessionListener.response(session, transactionId, requestId, 200, 0x0001, 0x0000);
            sessionListener.getParamsComplete(session, transactionId, requestId, statusCode, payload.toString().getBytes());
        }

    }

    private void appendParam(StringBuilder payload, Parameter param) {
        prependSemiColon(payload);
        payload.append(param.getType()).append(".").append(param.getName());
    }

    private void prependSemiColon(StringBuilder payload) {
        if (payload.length() != 0) {
            payload.append(";");
        }
    }

    @Override
    public void generateAudio(Session session, short transactionId, long requestId, long resourceId,
                              GenerateAudioData generateAudioData, ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void generateAudioViaTTSService(Session session, short transactionId, long requestId, long resourceId,
                                           GenerateAudioData data, ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void retrieveArtifacts(
            final Session session,
            final short transactionId,
            final long requestId,
            final long resourceId,
            final RetrieveArtifactsData retrieveArtifactsData,
            final ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void interpret(Session session, short transactionId, long requestId, long resourceId, InterpretData interpretData,
                          ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void loadGrammar(Session session, short transactionId, long requestId, long resourceId, LoadGrammarData loadGrammarData,
                            ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void resourceTunnel(Session session, short transactionId, long resourceId, byte[] payload, ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void transcode(Session session, short transactionId, long requestId, TranscodeData data, IAudioListener audioListener,
                          ISessionListener sessionListener) {
        // Do nothing
    }

    @Override
    public void sessionIdleTimeout(Session session, final String timeoutReason) {
        if(logger.isDebugEnabled()) {
            logger.debug(Logger.EVENT_UNSPECIFIED, "Session state: " + session.getState().name());
        }

        if (session.getState() != Session.STATE.DISCONNECTED && session.getState() != Session.STATE.DISCONNECTING) {
            session.setDisconnectionReason(timeoutReason);
            session.getClient().sendDisconnect(session, IClientHandler.DisconnectReason.SESSION_TIMEOUT);
        }
    }

    @Override
    public void cancel(Session session, short transactionId, long requestId, long requestIdToCancel, ISessionListener sessionListener) {

        /* If we want to support cancel we'll need to update the protocol
         * to add the resource id. Hard-coding resourceId to 0 for now
         */
    }

    @Override
    public void handleInternalError(Session session) {
        if (session.getState() != Session.STATE.DISCONNECTED && session.getState() != Session.STATE.DISCONNECTING) {
            session.getClient().sendEvent(session, (short) 0, IClientHandler.EventCode.INTERNAL_ERROR, null, true);
        }
    }

    @Override
    public void sendMdsMessage(Session session, MdsMessageData mdsMessageData) {
        // Do nothing
    }

    @Override
    public void mdsMessageResponse(Session session, short transactionId, long requestId, short statusCode) {
        if (statusCode != 0) {
                logger.warning(Logger.EVENT_UNSPECIFIED, "MDS message response returned an error: " + statusCode);
        }
    }

    private static class NMSPSessionControllerHolder {
        private static final NMSPSessionController singleton = new NMSPSessionController();
    }


}
