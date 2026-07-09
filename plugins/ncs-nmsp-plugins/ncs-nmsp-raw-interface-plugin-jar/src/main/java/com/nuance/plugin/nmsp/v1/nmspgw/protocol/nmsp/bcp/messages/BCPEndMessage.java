/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.Utils;

/**
 * @author epigeon
 * 
 */
public class BCPEndMessage extends BCPMessage
{
  public static final String TRANSACTION_TAG    = "transaction";
  public static final String STATUS_TAG         = "status";
  public static final String REPORT_LATENCY_TAG = "reportLatency";

    private long disconnectionTimeout;
    private Utils.Status status = Utils.Status.DONT_REPORT;
    private boolean reportLatency = false;

  /**
   * @param version
   * @param sessionId
   * @param transactionId
   */
  public BCPEndMessage(short version, byte[] sessionId, short transactionId)
  {
    super(version, sessionId, transactionId, BCPMessage.BCP_END);
  }

  /**
   * @param disconnectionTimeout
   *          the disconnectionTimeout to set
   */
  public void setDisconnectionTimeout(long disconnectionTimeout)
  {
    this.disconnectionTimeout = disconnectionTimeout;
  }

  /**
   * @return the disconnectionTimeout
   */
  public long getDisconnectionTimeout()
  {
    return disconnectionTimeout;
  }


    public Utils.Status getStatus() {
        return status;
    }

    public void setStatus(Utils.Status status) {
        this.status = status;
    }

  public boolean isReportLatency() {
    return reportLatency;
  }

  public void setReportLatency(boolean reportLatency) {
    this.reportLatency = reportLatency;
  }

  @Override
  public String toString() {
    return super.toString() + " timeout: " + disconnectionTimeout + " status: " + status + " reportLatency: " + reportLatency;
  }
}
