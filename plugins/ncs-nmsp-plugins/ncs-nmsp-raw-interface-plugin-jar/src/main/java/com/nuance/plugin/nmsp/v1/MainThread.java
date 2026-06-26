package com.nuance.plugin.nmsp.v1;

import com.nuance.plugin.nmsp.v1.nmspgw.client.NMSPClientManager;
import com.nuance.plugin.nmsp.v1.nmspgw.server.NVCGWServer;
import com.nuance.plugin.nmsp.v1.nmspgw.server.utils.Quiesce;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;

public class MainThread extends Thread {
    private final IWebApiFeatures features;
    private NVCGWServer nvcgwServer;
    private boolean running = true;
    private final Object eventLock = new Object();

    MainThread(IWebApiFeatures features) {
        this.features = features;
    }

    @Override
    public void run() {
        Quiesce quiesce = new Quiesce(60, 1);
        nvcgwServer = new NVCGWServer(quiesce, features);
        NMSPClientManager.init();
        nvcgwServer.onApplicationEvent("start");

        while (running) {
            //wait forever
            synchronized (eventLock) {
                try {
                    eventLock.wait();
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            }
        }

    }

    void terminate() {
        running = false;
        nvcgwServer.onApplicationEvent("stop");
        synchronized (eventLock) {
            eventLock.notifyAll();
        }
    }
}
