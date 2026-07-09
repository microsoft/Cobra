package com.nuance.plugin.driver_v2.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Timer {
    private static final Logger logger = LogManager.getLogger();
    private static final String TAG = "TAG-Timer";

    Thread t;
    long timeout;
    long t_start;
    long t_end;
    Timer.Listener listener;

    public interface Listener {
        void onStarted(long start);
        void onCancelled(long duration);
        void onExpired(long duration);
    }

    public Timer() {
    }

    private void run() {
        t = new Thread() {
            synchronized public void run() {
                try {
                    t_start = System.currentTimeMillis();
                    logger.info( "Timer started");
                    listener.onStarted(t_start);

                    logger.info( "Timer thread running...");
                    this.wait(timeout);

                    logger.info( "Timer thread done waiting...");
                    t_end = System.currentTimeMillis();
                    listener.onExpired((t_end - t_start));

                } catch (InterruptedException e) {
                    logger.info( "Timer thread interrupted...");
                    t_end = System.currentTimeMillis();
                    listener.onCancelled((t_end - t_start));
                }
            }
        };
        t.start();
    }

    public void start(long ms, Timer.Listener listener) {
        this.listener = listener;
        this.timeout = ms;
        this.run();
    }
    public void cancel() {
        logger.info( "Timer cancelled");
        if( t != null && (t.getState() == Thread.State.WAITING || t.getState() == Thread.State.TIMED_WAITING) )
            t.interrupt();
    }
    public void reset() {
        logger.info( "Timer reset");
        this.cancel();
        this.start(this.timeout, this.listener);
    }
    public void reset(long ms) {
        logger.info( "Timer reset");
        this.cancel();
        this.start(ms, this.listener);
    }

}
