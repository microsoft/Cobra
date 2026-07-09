package com.nuance.cobra.client;

import com.nuance.cobra.utils.response.ResponseHandler;
import org.apache.commons.configuration2.CombinedConfiguration;

/**
 * The abstract definition of a SessionClient
 *
 */
public abstract class SessionClient extends Client
{

    /**
     * Constructor for the SessionClient super class
     * @param sessionId
     */
    public SessionClient(String sessionId)
    {
        super(sessionId);
        
    }

    /**
     * Abstract definition of the startSession method, allowing starting a new session with the webAPI platform
     * 
     * @param responseHandler
     *      The response handler used to send the response back to the network interface
     * @param requestConfig
     *      The config object containing the parameters to perform the request
     */
    public abstract void startSession(ResponseHandler responseHandler, CombinedConfiguration requestConfig);
    
    /**
     * Abstract definition of the endSession method, allowing ending a session with the webAPI platform
     * 
     * @param responseHandler
     *      The response handler used to send the response back to the network interface
     * @param requestConfig
     *      The config object containing the parameters to perform the request
     */
    public abstract void endSession(ResponseHandler responseHandler, CombinedConfiguration requestConfig);
    
    
}
