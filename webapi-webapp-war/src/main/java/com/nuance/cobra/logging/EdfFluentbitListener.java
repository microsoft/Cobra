package com.nuance.cobra.logging;

import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.logging.fluentbit.FluentbitLogPoster;
import org.apache.commons.configuration2.CombinedConfiguration;

public class EdfFluentbitListener
        implements EdfLogListener
{
    @Override
    public void forwardLog(EdfMessageQueueable log, CombinedConfiguration commandConfigs)
    {
        FluentbitLogPoster.log(log, commandConfigs);
    }

    @Override
    public void destroy()
    {
        //No destroy action required for this listener
    }
}
