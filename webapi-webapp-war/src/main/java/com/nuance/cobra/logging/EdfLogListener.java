package com.nuance.cobra.logging;

import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import org.apache.commons.configuration2.CombinedConfiguration;

/**
 * This interface is a listener for EDF logs.
 * This will be implemented by any/all logging mechanism chosen for forwarding EDF logs.
 */
public interface EdfLogListener
{
    public void forwardLog(EdfMessageQueueable edfLog, CombinedConfiguration commandConfig);

    public void destroy();
}
