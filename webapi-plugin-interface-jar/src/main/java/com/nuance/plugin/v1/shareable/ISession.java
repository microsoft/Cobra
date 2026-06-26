package com.nuance.plugin.v1.shareable;

import org.apache.commons.configuration2.CombinedConfiguration;

public interface ISession {
    /**
     * Get the session ID.
     * @return
     *      Active command
     */
    String getSessionId();

    /**
     * Get the active command in the session.
     * @return
     *      Active command
     */
    ICommand getCommand();

    /**
     * Return the current session config map.
     * @return  Session config map.
     */
    CombinedConfiguration getSessionConfig();

    /**
     * Get a composite configuration that is request properties first, followed by session config.
     * @return The request+session configs.
     */
    CombinedConfiguration getConfigForRequest();
}
