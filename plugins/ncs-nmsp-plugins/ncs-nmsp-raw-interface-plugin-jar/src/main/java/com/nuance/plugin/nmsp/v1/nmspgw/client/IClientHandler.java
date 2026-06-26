/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.client;

import com.nuance.plugin.nmsp.v1.session.Session;

/**
 * @author epigeon
 */
public interface IClientHandler extends IAuthenticationListener, ISessionListener, IAudioListener {
  public static enum DisconnectReason {
    SESSION_TIMEOUT, PING_TIMEOUT, SERVER_SHUTDOWN, MAX_AUDIO_REACHED, AUTHENTICATION_DATA, CRITICAL_ERROR
  }

  public static enum ConnectFailureReason {
    FORBIDDEN, PARAMETERERROR
  }
  
  public static enum EventCode
  {
    CLIENT_PING_TIMEOUT, CLIENT_IDLE_TIMEOUT, NSS_DISCONNECT, NSS_ERROR, INTERNAL_ERROR, APPSERVER_DISCONNECT, APPSERVER_ERROR, CLIENT_STARTED_NEW_TRANSACTION, RESOURCE_UNLOADED
  }
  
  /**
   * Client connects to the GW. 
   * 
   * @param session
   */
  void connect(Session session);
  
  /**
   * Connection was successful. 
   * 
   * @param session
   */
  void connected(Session session);

  /**
   * Connection failed. 
   * 
   * @param session
   * @param reasonCode
   * @param reasonText
   */
  void connectFailed(Session session, ConnectFailureReason reasonCode, String reasonText);

  /**
   * Client confirms its identity. 
   * 
   * @param session
   * @param encryptedSessionId
   */
  void confirm(Session session, byte[] encryptedSessionId);
  
  /**
   * Informs client of imminent disconnection. 
   * 
   * @param session
   * @param reason
   */
  void sendDisconnect(Session session, DisconnectReason reason);

  /**
   * Closes the socket connection with the client. 
   * 
   * @param session
   */
  void close(Session session);
  
  /**
   * Sends a ping request to the client. 
   * 
   * @param session
   */
  void sendPingRequest(Session session);

  /**
   * Client sends a ping response. 
   * 
   * @param session
   * @param requestId
   * @param latencyEstimate
   */
  void pingResponse(Session session, long requestId, long latencyEstimate);
  
  /**
   * Send an event to the client. 
   * 
   * @param session
   * @param transactionId
   * @param eventCode
   * @param payload
   * @param closeConnection
   */
  void sendEvent(Session session, short transactionId, EventCode eventCode, byte[] payload, boolean closeConnection);

  /**
   * Send data to the client. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param payload
   */
  void sendData(Session session, short transactionId, long requestId, byte[] payload);
  
  /**
   * Client sends a log request.
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param payload
   */
  void logData(Session session, short transactionId, long requestId, byte[] payload);
  
  /**
   * Send a LogResponse to the client. 
   * 
   * @param session
   * @param transactionId
   * @param requestId
   * @param statusCode
   */
  void logResponse(Session session, short transactionId, long requestId, short statusCode);

  /**
   * Notify the client that the transaction is complete
   */
  void sendTransactionComplete(Session session, short transactionId);
  
}
