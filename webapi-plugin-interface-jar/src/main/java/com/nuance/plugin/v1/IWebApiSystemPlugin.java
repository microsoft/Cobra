package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IWebApiFeatures;

/**
 * System plugins get initialized when the container starts. Do note that at this level you will not have a crash safety net.
 */
public interface IWebApiSystemPlugin extends IWebApiPlugin {
    /**
     * Called when WebApi servlet is initialised.
     */
    default boolean onServerStart(IWebApiFeatures features) {
        return false;
    }

    /**
     * Called when WebApi servlet is terminated.
     */
    default void onServerStop() {
    }
}
