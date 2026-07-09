package com.nuance.cobra.utils.config;

import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ConfigHelpers
{
    private static final Logger logger = LogManager.getLogger(ConfigHelpers.class);

    public static Set<String> extractRootKeys(HierarchicalConfiguration config) {
        Iterator<String> voices = config.getKeys();
        Set<String> uniqueKeys = new HashSet<>();

        while (voices.hasNext()) {
            uniqueKeys.add(voices.next().split("\\.")[0]);
        }

        return uniqueKeys;
    }

    public static void clearCombinedConfiguration(CombinedConfiguration cc, String sessionId)
    {
        if(cc == null) return;

        if(cc instanceof WebapiGlobalCombinedConfiguration) { return; }

        String sessionIdToLog = Optional.ofNullable(sessionId).orElse("");

        var lingerMilliseconds = cc.getInt("combinedConfigLingerTimeMilliseconds", 0);
        if(lingerMilliseconds < 1)
        {
            actuallyClearCombinedConfiguration(cc, sessionIdToLog);
            return;
        }

        Runnable task = new ThreadContextRunnable()
        {
            @Override
            public void runWithContext()
            {
                actuallyClearCombinedConfiguration(cc, sessionIdToLog);
            }
        };
        logger.trace("Scheduling clearing of CombinedConfiguration after {} milliseconds for session {}", lingerMilliseconds, sessionIdToLog);
        ScheduledTaskUtils.scheduleTask(task, lingerMilliseconds, TimeUnit.MILLISECONDS);
    }

    private static void actuallyClearCombinedConfiguration(CombinedConfiguration cc, String sessionIdToLog)
    {
        logger.trace("Clearing CombinedConfiguration for session {} ({})", sessionIdToLog, cc);
        cc.clear();
        cc.clearEventListeners();
        cc.clearErrorListeners();
    }
}
