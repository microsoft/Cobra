package com.nuance.cobra.logging.eventhub;

import com.nuance.cobra.logging.EdfLogListener;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import org.apache.commons.configuration2.CombinedConfiguration;

public class EventHubListener
        implements EdfLogListener
{
    @Override
    public void forwardLog(EdfMessageQueueable log, CombinedConfiguration commandConfigs)
    {
        EventHubLogPoster.log(log, commandConfigs);
    }

    @Override
    public void destroy()
    {
        //No destroy action required for this listener
    }
}