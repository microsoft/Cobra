/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.session;

import com.nuance.plugin.nmsp.v1.nmspgw.client.IAudioListener;
import com.nuance.plugin.nmsp.v1.nmspgw.client.ISessionListener;
import com.nuance.plugin.nmsp.v1.nmspgw.data.*;

import java.util.HashMap;
import java.util.Vector;

/**
 * Defines the interface for handling session level communications
 * 
 * @author epigeon
 * 
 */
public interface ISessionController
{
  /**
   * Client connected. 
   * 
   * @param session
   */
  void clientConnected(Session session);

  /**
   * Client disconnected. 
   * 
   * @param session
   * @param reason
   */
  void clientDisconnected(Session session, int reason);

  /**
   * Client/NMAS loads a resource. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param data
   * @param sessionListener
   */
  void loadResource(Session session, short transactionId, long requestId, long resourceId, LoadResourceData data, ISessionListener sessionListener);

  /**
   * Client/NMAS frees a resource. 
   * 
   * @param session
   * @param transactionId
   * @param resourceId
   * @param timeout
   */
  void freeResource(Session session, short transactionId, long resourceId, long timeout);

  /**
   * Client begins an NMAS transaction. 
   * 
   * @param session
   * @param transactionId
   * @param pdxPayload
   * @param queryBeginInfo
   */
  void beginTransaction(Session session, short transactionId, byte[] pdxPayload, HashMap<String, Object> queryBeginInfo, short version);

  /**
   * Client sends data to NMAS. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param pdxPayload
   */
  void sendDataToNmas(Session session, short transactionId, long requestId, byte[] pdxPayload);

  /**
   * NMAS sends data to the client. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param pdxPayload
   */
  void sendDataToClient(Session session, short transactionId, long requestId, byte[] pdxPayload);

  /**
   * Client/NMAS performs a recognition. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param data
   * @param sessionListener
   */
  void recognize(Session session, short transactionId, long requestId, long resourceId, RecognizeData data, ISessionListener sessionListener);

  /**
   * Client/NMAS sets a parameters. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param parameters
   * @param sendResponse
   * @param sessionListener
   */
  void setParams(Session session, short transactionId, long requestId, long resourceId, Vector<Parameter> parameters, boolean sendResponse, ISessionListener sessionListener);

  /**
   * Client/NMAS gets a parameters. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param parameters
   * @param sessionListener
   */
  void getParams(Session session, short transactionId, long requestId, long resourceId, Vector<Parameter> parameters, ISessionListener sessionListener);

  /**
   * Client/NMAS generates audio. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param data
   * @param sessionListener
   */
  void generateAudio(Session session, short transactionId, long requestId, long resourceId, GenerateAudioData data, ISessionListener sessionListener);

  
  /**
   * Client/NMAS generates audio. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param data
   * @param sessionListener
   */
  void generateAudioViaTTSService(Session session, short transactionId, long requestId, long resourceId, GenerateAudioData data, ISessionListener sessionListener);

  
  
  void retrieveArtifacts(Session session, short transactionId, long requestId, long resourceId, RetrieveArtifactsData data, ISessionListener sessionListener);

  /**
   * Client/NMAS performs an interpretation. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param data
   * @param sessionListener
   */
  void interpret(Session session, short transactionId, long requestId, long resourceId, InterpretData data, ISessionListener sessionListener);

  /**
   * Client/NMAS loads a grammar. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param data
   * @param sessionListener
   */
  void loadGrammar(Session session, short transactionId, long requestId, long resourceId, LoadGrammarData data, ISessionListener sessionListener);

  /**
   * Client sends data to S2. 
   * 
   * @param session
   * @param transactionId
   * @param resourceId
   * @param payload
   * @param sessionListener
   */
  void resourceTunnel(Session session, short transactionId, long resourceId, byte[] payload, ISessionListener sessionListener);

  /**
   * Client/NMAS transcodes audio. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param data
   * @param audioListener
   * @param sessionListener
   */
  void transcode(Session session, short transactionId, long requestId, TranscodeData data, IAudioListener audioListener, ISessionListener sessionListener);

  /**
   * Session timeout has expired. 
   * 
   * @param session
   * @param timeoutReason
   */
  void sessionIdleTimeout(Session session, final String timeoutReason);

  /**
   * Client/NMAS cancels a request. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param requestIdToCancel
   * @param sessionListener
   */
  void cancel(Session session, short transactionId, long requestId, long requestIdToCancel, ISessionListener sessionListener);

  /**
   * An internal error has occured. 
   * 
   * @param session
   */
  void handleInternalError(Session session);
  
  /**
   * Send a message to MDS
   * 
   * @param session
   * @param mdsMessageData
   */
  void sendMdsMessage(Session session, MdsMessageData mdsMessageData);
  
  /**
   * Received an MDS message response
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param statusCode
   */
  void mdsMessageResponse(Session session, short transactionId, long requestId, short statusCode);
}
