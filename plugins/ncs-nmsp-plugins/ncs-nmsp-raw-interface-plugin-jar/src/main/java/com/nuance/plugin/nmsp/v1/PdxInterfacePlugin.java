package com.nuance.plugin.nmsp.v1;

import com.nuance.plugin.v1.IWebApiSystemPlugin;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;

public class PdxInterfacePlugin implements IWebApiSystemPlugin {
    public static final String THREAD_CONTEXT_PREFIX = "RAW_NMSP";
    
    MainThread mainThread;

    @Override
    public boolean onServerStart(IWebApiFeatures features) {
        mainThread = new MainThread(features);
        mainThread.start();
        return true;
    }

    @Override
    public void onServerStop() {
        mainThread.terminate();
    }
}
