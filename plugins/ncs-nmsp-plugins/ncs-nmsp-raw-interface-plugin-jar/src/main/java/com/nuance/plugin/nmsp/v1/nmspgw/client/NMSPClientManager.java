/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.client;

import com.nuance.plugin.ncs.utils.ICodecResource.Codec;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.InvalidMessageException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.handlers.BCPMessageHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.*;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectFailedMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPConnectedMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPDisconnectMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.cop.messages.COPPingRequestMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayBeginMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayEndMessage;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages.VAPPlayMessage;
import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.WebAPIAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Manages the connection with the NMSP client. Handles COP, BCP and VAP messages.
 *
 * @author epigeon
 */
public class NMSPClientManager implements IClientHandler {
    private static final Logger logger = LogManager.getLogger();
    private static NMSPClientManager instance = null;

    private NMSPClientManager() {
    }

    public static void init() {
        instance = new NMSPClientManager();
    }

    public static NMSPClientManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("Unmoral server startup. NMSPClientManager was not initialized");
        }
        return instance;
    }

    /*************** IClientHandler Interface ***************/

    @Override
    public void connect(Session session) {
        // Authenticate the client
        session.setState(Session.STATE.AUTHENTICATING);
        WebAPIAuthenticator.onConnect(session, this);
    }

    @Override
    public void connected(Session session) {
        COPConnectedMessage copConnectedMessage = new COPConnectedMessage(session.getCopVersion(), session.getSessionId());
        if (session.getPingIntervalSecs() > 0) {
            copConnectedMessage.setPingInterval(session.getPingIntervalSecs());
            copConnectedMessage.setPingMissedBeforeDisconnect(session.getMissedPingResponsesBeforeDisconnect());
        }

        write(session, copConnectedMessage);
    }

    @Override
    public void sendDisconnect(Session session, DisconnectReason reasonCode) {
        COPDisconnectMessage copDisconnect = new COPDisconnectMessage(session.getCopVersion());
        switch (reasonCode) {
            case SESSION_TIMEOUT:
                copDisconnect.setReasonCode(COPDisconnectMessage.DISCONNECT_REASON_TIMEOUT);
                break;
            case PING_TIMEOUT:
                copDisconnect.setReasonCode(COPDisconnectMessage.DISCONNECT_REASON_PING_TIMEOUT);
                break;
            case SERVER_SHUTDOWN:
                copDisconnect.setReasonCode(COPDisconnectMessage.DISCONNECT_REASON_SERVER_SHUTDOWN);
                break;
            case CRITICAL_ERROR:
                copDisconnect.setReasonCode(COPDisconnectMessage.DISCONNECT_REASON_CRITICAL_ERROR);
                break;
            case MAX_AUDIO_REACHED:
                copDisconnect.setReasonCode(COPDisconnectMessage.DISCONNECT_REASON_MAX_AUDIO);
                break;
            case AUTHENTICATION_DATA:
                copDisconnect.setReasonCode(COPDisconnectMessage.DISCONNECT_REASON_AUTHENTIFICATION_DATA_ERROR);
                break;
            default:
                copDisconnect.setReasonCode(COPDisconnectMessage.DISCONNECT_REASON_UNKNOWN);
                break;
        }
        // copDisconnect.setData(data);

        session.disconnectingClient();

        writeAndClose(session, copDisconnect);
    }

    @Override
    public void connectFailed(Session session, ConnectFailureReason reasonCode, String reasonText) {
        COPConnectFailedMessage copConnectFailed = new COPConnectFailedMessage(session.getCopVersion());
        switch (reasonCode) {
            case FORBIDDEN:
                copConnectFailed.setReasonCode(COPConnectFailedMessage.CONNECT_FAILURE_FORBIDDEN);
                break;
            case PARAMETERERROR:
                copConnectFailed.setReasonCode(COPConnectFailedMessage.CONNECT_FAILURE_PRE_CONDITION_FAILED);
                break;
        }
        copConnectFailed.setReasonText(reasonText);

        writeAndClose(session, copConnectFailed);
    }

    @Override
    public void confirm(Session session, byte[] clientEncryptSessionId) {
        if (session.getState() != Session.STATE.CHALLENGING) {
            throw new InvalidMessageException("Session in wrong state");
        }

        if (verifyClient(session, clientEncryptSessionId)) {
            acceptClientConnection(session);
            return;
        }

        String reason = "Encrypted session id not valid. The client key used to encrypt the session id may not be valid.";
        logger.warn(reason);
        session.setDisconnectionReason("Authentication failed");
        denyClientConnection(session, reason);
    }

    private boolean verifyClient(Session session, byte[] clientEncryptSessionId) {
        try {
            if (WebAPIAuthenticator.onAcceptClient(session, clientEncryptSessionId)) {
                acceptClientConnection(session);
                return true;
            } else {
                logger.error("No encrypted session IDs found to be able to validate the client.");
            }
        } catch (Exception e) {
            logger.error("Client authentication failed", e);
        }

        return false;
    }

    private void denyClientConnection(Session session, String reason) {
        logger.debug("Authentication REJECTED");

        // Clear the message queue
        session.getMessageQueue().clear();

//        session.setState(Session.STATE.DISCONNECTING);
        connectFailed(session, IClientHandler.ConnectFailureReason.FORBIDDEN, "Authentication failed");
    }

    private void acceptClientConnection(Session session) {

        logger.trace("Authentication ACCEPTED");

        NMSPSessionController.getInstance().clientConnected(session);

        // Empty the BCP message queue
        while (!session.getMessageQueue().isEmpty()) {
            BCPMessageHandler.handleMessage(session, session.getMessageQueue().remove(0), session.getClientSessionListener());
        }

    }

    @Override
    public void sendPingRequest(Session session) {
        int missedPings = session.getMissedPings();
        if (session.getMissedPingResponsesBeforeDisconnect() <= (missedPings + 1)) {
            logger.warn("exceeded number of missed ping request");

            session.getPingRequestScheduledFuture().cancel(true);

            session.setDisconnectionReason("exceeded number of missed ping request");
            sendDisconnect(session, IClientHandler.DisconnectReason.PING_TIMEOUT);
        } else {
            long pingRequestId = session.getPingRequestId();
            session.setLastPingRequest(System.currentTimeMillis());

            COPPingRequestMessage pingRequest = new COPPingRequestMessage(session.getCopVersion());
            pingRequest.setPingRequestId(pingRequestId);
            pingRequest.setPreviousRoundTripTime(session.getRoundTripTime());

            write(session, pingRequest);

            session.setPingRequestId(pingRequestId + 1);
            session.setMissedPings(missedPings + 1);
        }
    }

    @Override
    public void pingResponse(Session session, long requestId, long latencyEstimate) {
        if (session.getState() != Session.STATE.CONNECTED) {
            return;
        }

        session.setMissedPings(0);
        long roundTripTime = System.currentTimeMillis() - session.getLastPingRequest();
        session.setRoundTripTime(roundTripTime);
    }

    @Override
    public void sendEvent(Session session, short transactionId, EventCode eventCode, byte[] payload, boolean closeConnection) {
        BCPEventMessage bcpEvent = new BCPEventMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        switch (eventCode) {
            case CLIENT_PING_TIMEOUT:
                bcpEvent.setEventCode(BCPEventMessage.EVENT_CODE_CLIENT_PING_TIMEOUT);
                break;
            case CLIENT_IDLE_TIMEOUT:
                bcpEvent.setEventCode(BCPEventMessage.EVENT_CODE_CLIENT_IDLE_TIMEOUT);
                break;
            case NSS_DISCONNECT:
                bcpEvent.setEventCode(BCPEventMessage.EVENT_CODE_NSS_DISCONNECT);
                break;
            case NSS_ERROR:
                bcpEvent.setEventCode(BCPEventMessage.EVENT_CODE_NSS_ERROR);
                break;
            case INTERNAL_ERROR:
                bcpEvent.setEventCode(BCPEventMessage.EVENT_CODE_INTERNAL_ERROR);
                break;
            case APPSERVER_DISCONNECT:
                bcpEvent.setEventCode(BCPEventMessage.EVENT_CODE_APPSERVER_DISCONNECT);
                break;
            case APPSERVER_ERROR:
                bcpEvent.setEventCode(BCPEventMessage.EVENT_CODE_APPSERVER_ERROR);
                break;
        }
        bcpEvent.setPayload(payload);

        logger.debug("<====== Sending to client {}", payload);

        if (closeConnection) {
            writeAndClose(session, bcpEvent);
        } else {
            write(session, bcpEvent);
        }
    }

    @Override
    public void sendData(Session session, short transactionId, long requestId, byte[] payload) {
        BCPDataMessage bcpData = new BCPDataMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpData.setRequestId(requestId);
        bcpData.setPayload(payload);

        write(session, bcpData);
    }

    @Override
    public void logData(Session session, short transactionId, long requestId, byte[] payload) {
        logger.trace(new String(payload));
    }

    @Override
    public void logResponse(Session session, short transactionId, long requestId, short statusCode) {
        BCPLogResponseMessage bcpLogResponse = new BCPLogResponseMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpLogResponse.setRequestId(requestId);
        bcpLogResponse.setStatusCode(statusCode);

        write(session, bcpLogResponse);
    }

    @Override
    public void close(Session session) {
        logger.debug("====== ConnectionClosed");

        if (session.getClientChannel() != null) {
            closeNetty(session);
        }
    }


    /*************** SessionListener Interface ***************/

    @Override
    public void resourceLoaded(Session session, short transactionId, long requestId, long resourceId, int completionCause, String resourceAddress) {
        BCPLoadResourceCompleteMessage bcpLoadResourceCompleteMessage = new BCPLoadResourceCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpLoadResourceCompleteMessage.setRequestId(requestId);
        bcpLoadResourceCompleteMessage.setResourceId(resourceId);
        bcpLoadResourceCompleteMessage.setCompletionCause(completionCause);

        write(session, bcpLoadResourceCompleteMessage);
    }

    @Override
    public void resourceDisconnected(Session session, long resourceId, int statusCode) {
        String payload = "<evt>";
        payload += "<resid>" + resourceId + "</resid>";
        payload += "<code>" + statusCode + "</code>";
        payload += "</evt>";

        sendEvent(session, (short) 0, EventCode.RESOURCE_UNLOADED, payload.getBytes(), false);
    }

    @Override
    public void response(Session session, short transactionId, long requestId, int statusCode, int requestState, int completionCause) {
        BCPResponseMessage bcpResponse = new BCPResponseMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpResponse.setRequestId(requestId);
        bcpResponse.setStatusCode(statusCode);
        bcpResponse.setRequestState(requestState);
        bcpResponse.setCompletionCause(completionCause);

        write(session, bcpResponse);
    }

    // @Override
    // public void loadGrammarComplete(short transactionId, long requestId, int resourceId, int completionCause) {
    // BCPLoadGrammarCompleteMessage bcpLoadGrammarComplete = new BCPLoadGrammarCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
    // bcpLoadGrammarComplete.setRequestId(requestId);
    // bcpLoadGrammarComplete.setResourceId(resourceId);
    // bcpLoadGrammarComplete.setCompletionCause(completionCause);
    // session.getClientChannel().write(bcpLoadGrammarComplete);
    // }

    @Override
    public void generateAudioComplete(Session session, short transactionId, long requestId, long audioId, int completionCause) {
        BCPGenerateAudioCompleteMessage bcpGenerateAudioComplete = new BCPGenerateAudioCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpGenerateAudioComplete.setRequestId(requestId);
        bcpGenerateAudioComplete.setAudioId(audioId);
        bcpGenerateAudioComplete.setCompletionCause(completionCause);

        write(session, bcpGenerateAudioComplete);
    }

    @Override
    public void retrieveArtifactsComplete(final Session session, final short transactionId, final long requestId, final long audioId, final int completionCause) {
        write(session,
                new BCPRetrieveArtifactsCompleteMessage(
                        session.getBcpVersion(),
                        session.getSessionId(),
                        transactionId,
                        requestId,
                        audioId,
                        completionCause));
    }

    @Override
    public void startOfSpeech(Session session, short transactionId, long requestId, int requestState) {
        BCPStartOfSpeechMessage startOfSpeech = new BCPStartOfSpeechMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        startOfSpeech.setRequestId(requestId);
        startOfSpeech.setRequestState(requestState);

        write(session, startOfSpeech);
    }

    @Override
    public void endOfSpeech(Session session, short transactionId, long requestId, int requestState, byte[] payload) {
        BCPEndOfSpeechMessage endOfSpeech = new BCPEndOfSpeechMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        endOfSpeech.setRequestId(requestId);
        endOfSpeech.setRequestState(requestState);
        endOfSpeech.setPayload(payload);

        write(session, endOfSpeech);
    }

    @Override
    public void recognitionComplete(Session session, short transactionId, long requestId, int completionCause, byte[] payload, short resultType) {
        BCPRecognitionCompleteMessage bcpRecognize = new BCPRecognitionCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpRecognize.setRequestId(requestId);
        bcpRecognize.setCompletionCause(completionCause);
        bcpRecognize.setPayload(payload);
        bcpRecognize.setResultType(resultType);

        write(session, bcpRecognize);
    }

    @Override
    public void recognitionIntermediateResults(Session session, short transactionId, long requestId, int completionCause, byte[] payload, short resultType) {
        BCPRecognitionIntermediateResultsMessage bcpIntermResults = new BCPRecognitionIntermediateResultsMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpIntermResults.setRequestId(requestId);
        bcpIntermResults.setCompletionCause(completionCause);
        bcpIntermResults.setPayload(payload);
        bcpIntermResults.setResultType(resultType);

        write(session, bcpIntermResults);
    }

    @Override
    public void interpretationComplete(Session session, short transactionId, long requestId, int completionCause, byte[] payload, short resultType) {
        BCPInterpretationCompleteMessage bcpInterpretationComplete = new BCPInterpretationCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpInterpretationComplete.setRequestId(requestId);
        bcpInterpretationComplete.setCompletionCause(completionCause);
        bcpInterpretationComplete.setPayload(payload);
        bcpInterpretationComplete.setResultType(resultType);

        write(session, bcpInterpretationComplete);
    }

    @Override
    public void setParamsComplete(Session session, short transactionId, long requestId, int statusCode, byte[] payload) {
        BCPSetParamsCompleteMessage bcpSetParamsComplete = new BCPSetParamsCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpSetParamsComplete.setRequestId(requestId);
        bcpSetParamsComplete.setStatusCode(statusCode);
        bcpSetParamsComplete.setPayload(payload);

        write(session, bcpSetParamsComplete);
    }

    @Override
    public void getParamsComplete(Session session, short transactionId, long requestId, int statusCode, byte[] payload) {
        BCPGetParamsCompleteMessage bcpGetParamsComplete = new BCPGetParamsCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpGetParamsComplete.setRequestId(requestId);
        bcpGetParamsComplete.setStatusCode(statusCode);
        bcpGetParamsComplete.setPayload(payload);

        write(session, bcpGetParamsComplete);
    }

    @Override
    public void cancelResponse(Session session, short transactionId, long requestId, int statusCode, int completionCause) {
        BCPCancelResponseMessage bcpCancelResponse = new BCPCancelResponseMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpCancelResponse.setRequestId(requestId);
        bcpCancelResponse.setStatusCode(statusCode);
        bcpCancelResponse.setCompletionCause(completionCause);

        write(session, bcpCancelResponse);
    }

    @Override
    public void resourceTunnel(Session session, short transactionId, long resourceId, byte[] payload) {
        BCPResourceTunnelMessage bcpResourceTunnel = new BCPResourceTunnelMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpResourceTunnel.setResourceId(resourceId);
        bcpResourceTunnel.setPayload(payload);

        write(session, bcpResourceTunnel);
    }

    @Override
    public void transcodeComplete(Session session, short transactionId, long requestId, long audioId, int completionCause, byte[] payload) {
        BCPTranscodeCompleteMessage bcpTranscodeComplete = new BCPTranscodeCompleteMessage(session.getBcpVersion(), session.getSessionId(), transactionId);
        bcpTranscodeComplete.setRequestId(requestId);
        bcpTranscodeComplete.setAudioId(audioId);
        bcpTranscodeComplete.setCompletionCause(completionCause);
        bcpTranscodeComplete.setPayload(payload);

        write(session, bcpTranscodeComplete);
    }


    @Override
    public void sendTransactionComplete(Session session, short transactionId) {

    }


    /*************** IAudioListener Interface ***************/

    @Override
    public void audioReceivedStart(Session session, long requestId, long audioId, Codec codec) {
        logger.debug(">====== Audio Received Start");

        VAPPlayBeginMessage vapPlayBegin = new VAPPlayBeginMessage(VAPMessage.VAP_MINIMUM_VERSION);
        vapPlayBegin.setAudioId(audioId);
        vapPlayBegin.setCodec(codec);

        write(session, vapPlayBegin);
    }

    @Override
    public void audioReceived(Session session, long requestId, long audioId, Codec codec, byte[] audio, int startOfSpeechMs, boolean endOfSpeech, boolean segmentEnding) {
        logger.debug(">====== Audio Received");

        if (audio != null && audio.length > 0) {
            VAPPlayMessage vapPlay = new VAPPlayMessage(VAPMessage.VAP_MINIMUM_VERSION);
            vapPlay.setAudioId(audioId);
            vapPlay.setAudio(audio);
            write(session, vapPlay);
        }

        if (endOfSpeech) {
            VAPPlayEndMessage vapPlayEnd = new VAPPlayEndMessage(VAPMessage.VAP_MINIMUM_VERSION);
            vapPlayEnd.setAudioId(audioId);
            write(session, vapPlayEnd);
        }
    }

    @Override
    public void subAudioReceived(Session session, long requestId, long audioId, Codec codec, byte[] audio, int startOfSpeechMs, boolean endOfSpeech, boolean isAudioStart) {
        audioReceived(session, requestId, audioId, codec, audio, startOfSpeechMs, endOfSpeech, false);
    }

    /*************** IAuthenticationListener Interface ***************/

    @Override
    public void authenticationRejected(Session session, String reason) {
        if (session.getState() == Session.STATE.AUTHENTICATING) {
            denyClientConnection(session, reason);
        }
    }

    @Override
    public void authenticationChallenged(Session session) {
        if (session.getState() == Session.STATE.AUTHENTICATING) {
            session.setState(Session.STATE.CHALLENGING);
            connected(session);
        }
    }

    @Override
    public void authenticationAccepted(Session session) {
        if (session.getState() == Session.STATE.AUTHENTICATING) {
            acceptClientConnection(session);
            session.setState(Session.STATE.CONNECTED);
            connected(session);
        }
    }

    private void write(Session session, NMSPMessage nmspMessage) {
        logger.debug("<====== Sending to client {}", nmspMessage);
        if (session.getClientChannel() != null) {
            writeNetty(session, nmspMessage);
        } else {
            // else client is no longer connected
            logger.warn("client not connected, dropping message");
        }
    }

    private void writeAndClose(Session session, NMSPMessage nmspMessage) {
        session.setClientDisconnected(false);
        if (session.getClientChannel() != null) {
            ChannelFuture future = writeNetty(session, nmspMessage);
            if (future != null) {
                future.addListener(ChannelFutureListener.CLOSE);
            } else {
                closeNetty(session);
            }
        } else {
            // else client is no longer connected
            logger.warn("client not connected, dropping message");
        }
    }

    private ChannelFuture writeNetty(Session session, NMSPMessage nmspMessage) {
        Channel channel = session.getClientChannel();;
        if (channel.isActive()
                && channel.isOpen()
                && channel.isWritable()) {
            return channel.writeAndFlush(nmspMessage);
        }
        logger.warn("Channel not connected, dropping message");
        return null;
    }

    private void closeNetty(Session session) {
        session.getClientChannel().close();
    }
}
