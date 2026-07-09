package com.nuance.cobra.logging;

import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.util.LinkedList;

public class EdfLogForwarder
{
    private LinkedList<EdfLogListener> logListeners = new LinkedList<>();

    public void addListener(EdfLogListener listener)
    {
        logListeners.add(listener);
    }

    public void removeListener(EdfLogListener listener)
    {
        logListeners.remove(listener);
    }

    public void forwardLog(EdfMessageQueueable log, CombinedConfiguration commandConfig)
    {
        for (EdfLogListener listener : logListeners)
        {
            listener.forwardLog(log, commandConfig);
        }
    }

    public void destroy()
    {
        for (EdfLogListener listener : logListeners)
        {
            listener.destroy();
        }
    }
}
