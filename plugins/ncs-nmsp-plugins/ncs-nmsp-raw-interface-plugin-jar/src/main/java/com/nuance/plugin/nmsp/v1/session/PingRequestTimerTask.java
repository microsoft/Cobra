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

import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.GWThreadStarter;
import com.nuance.plugin.nmsp.v1.nmspgw.server.threading.IGWThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executor;

/**
 * @author epigeon
 *
 */
public class PingRequestTimerTask implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    private final Session session;
    private final Executor executor;

    /**
     *
     */
    public PingRequestTimerTask(Session session, Executor executor) {
        this.session = session;
        this.executor = executor;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        // Switch to the session's thread
        try {
            if (session == null || executor == null) {
                return; // Safety
            }
            GWThreadStarter thread = new GWThreadStarter(session, new PingRequestTimeoutThread());
            executor.execute(thread);
        } catch (Exception e) {
            logger.error("Failed to start a thread.", e);
        }
    }

    private class PingRequestTimeoutThread implements IGWThread {

        @Override
        public void start() {
            if (session == null) {
                return; // Safety
            }
            session.getClient().sendPingRequest(session);
        }

    }
}
