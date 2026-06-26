/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.client;

import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * Defines the interface to listen to session related events.
 * 
 * @author epigeon
 * 
 */
public interface ISessionListener
{
  /**
   * Informs the listener if the resource was loaded or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param resourceId
   * @param completionCause
   * @param resourceAddress
   */
  void resourceLoaded(Session session, short transactionId, long requestId, long resourceId, int completionCause, String resourceAddress);

  /**
   * Invoked when the resource crashes, or if it disconnects
   * 
   * @param session
   * @param resourceId
   * @param statusCode
   */
  void resourceDisconnected(Session session, long resourceId, int statusCode);
  
  /**
   * Acknowledgment of a request. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param statusCode
   * @param requestState
   * @param completionCause
   */
  void response(Session session, short transactionId, long requestId, int statusCode, int requestState, int completionCause);

  /**
   * Informs the listener if the generateAudio was successful or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param audioId
   * @param completionCause
   */
  void generateAudioComplete(Session session, short transactionId, long requestId, long audioId, int completionCause);

  void retrieveArtifactsComplete(Session session, short transactionId, long requestId, long audioId, int completionCause);

  /**
   * Informs the listener about the start of speech. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param requestState
   */
  void startOfSpeech(Session session, short transactionId, long requestId, int requestState);

  /**
   * Informs the listener about the end of speech. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param requestState
   * @param payload
   */
  void endOfSpeech(Session session, short transactionId, long requestId, int requestState, byte[] payload);

  /**
   * Informs the listener if the recognize was successful or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param completionCause
   * @param payload
   * @param resultType
   */
  void recognitionComplete(Session session, short transactionId, long requestId, int completionCause, byte[] payload, short resultType);

  /**
   * Informs the listener about recognition intermediate results. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param completionCause
   * @param payload
   * @param resultType
   */
  void recognitionIntermediateResults(Session session, short transactionId, long requestId, int completionCause, byte[] payload, short resultType);

  /**
   * Informs the listener if the interpret was successful or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param completionCause
   * @param payload
   * @param resultType
   */
  void interpretationComplete(Session session, short transactionId, long requestId, int completionCause, byte[] payload, short resultType);

  /**
   * Informs the listener if the setParams was successful or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param statusCode
   * @param payload
   */
  void setParamsComplete(Session session, short transactionId, long requestId, int statusCode, byte[] payload);

  /**
   * Informs the listener if the getParams was successful or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param statusCode
   * @param payload
   */
  void getParamsComplete(Session session, short transactionId, long requestId, int statusCode, byte[] payload);

  /**
   * Informs the listener if the cancel was successful or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param statusCode
   * @param completionCause
   */
  void cancelResponse(Session session, short transactionId, long requestId, int statusCode, int completionCause);

  /**
   * Informs the listener about tunnel responses. 
   * 
   * @param session
   * @param transactionId
   * @param resourceId
   * @param payload
   */
  void resourceTunnel(Session session, short transactionId, long resourceId, byte[] payload);

  /**
   * Informs the listener if the transcode was successful or not. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param audioId
   * @param completionCause
   * @param payload
   */
  void transcodeComplete(Session session, short transactionId, long requestId, long audioId, int completionCause, byte[] payload);
}
