package com.nuance.plugin.v1.shareable;

import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.io.IOException;

public interface IWebApiFeatures {

    /**
     * Get the latest configs
     * @return
     */
    CombinedConfiguration getConfigs() throws IOException;
    
    CombinedConfiguration getConfigs(String tenantName) throws IOException;

    void close();

    /**
     * Get the SNMP utility.
     * @return
     */
    ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs);

    /**
     * Get the EDF utility.
     * @return
     */
    IEdfLogPoster getEdfPoster(CombinedConfiguration configs);

    /**
     * Get the webAPI endpoint. This is the entry point ot the webAPI functionality.
     * @return
     */
    IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) throws IOException;
}
