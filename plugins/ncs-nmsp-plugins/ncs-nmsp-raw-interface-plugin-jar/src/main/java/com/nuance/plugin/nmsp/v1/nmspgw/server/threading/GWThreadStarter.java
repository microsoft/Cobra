/**
 *
 */
package com.nuance.plugin.nmsp.v1.nmspgw.server.threading;

import com.nuance.plugin.nmsp.v1.session.NMSPSessionController;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author epigeon
 */
public class GWThreadStarter implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final Session session;
    private final IGWThread thread;

    public GWThreadStarter(Session session, IGWThread thread) {
        this.session = session;
        this.thread = thread;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            thread.start();
        } catch (Exception e) {

            logger.error("Caught exception: ", e);
            for (StackTraceElement element : e.getStackTrace()) {
                logger.error(element.toString());
            }
            NMSPSessionController.getInstance().handleInternalError(session);
        }
    }

}
