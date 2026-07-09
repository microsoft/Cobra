package com.nuance.cobra.utils.response;

import com.nuance.cobra.api.response.ResponseResult;

import java.util.Map;

/**
 * An interface for classes that need to take action before or after a response is sent (e.g. command chain).
 */
public interface CommandChainer {

    /**
     * Returns the index of the current command in the chain
     * @return the index of the current command in the chain
     */
    int getCurrentCommandIndex();

    /**
     * Returns the number of commands in the chain
     * @return the number of commands in the chain
     */
    int getCommandCount();

    /**
     * Returns the chain id
     * @return The chain id
     */
    String getChainId();

    /**
     * Returns the chain metadata
     * @return The chain metadata
     */
    Map<String, Object> getChainMetadata();

    /**
     * Called right before a response is sent.
     */
    void onResponseWillBeSent(ResponseResult response);

    /**
     * Called right after a response is sent.
     */
    void onResponseWasSent(ResponseResult response, String formattedResponse);
}
