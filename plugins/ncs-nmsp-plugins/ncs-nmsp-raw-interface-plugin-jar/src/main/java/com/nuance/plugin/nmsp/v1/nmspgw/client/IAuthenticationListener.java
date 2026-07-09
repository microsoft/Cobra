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
 * @author epigeon
 * 
 */
public interface IAuthenticationListener
{

  /**
   * Invoked when the Authentication rejects a connection. Causes the session to be terminated
   */
  void authenticationRejected(Session session, String reason);

  /**
   * Invoked when the Authentication decides to challenge a connection. Causes COPConnectedMessage to be sent out so the client can encrypt the session id and send it on the COPConfirmMessage to be
   * compared with encryptedSessionId.
   *
   * @param session
   */
  void authenticationChallenged(Session session);
  
  
  /**
   * Invoked when the Authentication accepts a connection without any challenge.
   * 
   * @param session
   */
  void authenticationAccepted(Session session);
}
