package com.nuance.cobra.testing;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Base class for tests that need to set the level of specific loggers and verify the logging output.
 * <p>
 * To capture output, it adds and in-memory appender to these loggers. After a test is run, the logging
 * configuration is reset not to impact subsequent tests.
 */

public abstract class LoggingTest
{
    public static final String LOG_LEVEL_PROPERTY = "com.nuance.cobra.test.loglevel";
    public static final Level DEFAULT_TEST_LOG_LEVEL  = Level.ALL;


    private static final Logger logger = LogManager.getLogger(LoggingTest.class);

    static private ByteArrayOutputStream loggingStream = new ByteArrayOutputStream();
    static private OutputStreamAppender inMemoryAppender;

    final static String TEST_APPENDER_NAME = "TestInMemoryAppender";

    final static LoggerContext loggingContext = (LoggerContext) LogManager.getContext(false);
    final static Configuration loggingConfig = loggingContext.getConfiguration();

    private ArrayList<SavedLoggerConfigInfo> savedLoggerConfigs = new ArrayList<>();

    private static Level savedRootLoggerLevel;

    /**
     * Used to store preexisting configuration so it can be restored after the test.
     */
    static private class SavedLoggerConfigInfo
    {
        private String loggerName;
        private Level savedLevel;

        /**
         * Use this constructor if the config already exists, so the level is saved for later restoration.
         *
         * @param loggerName the logger name
         */
        protected SavedLoggerConfigInfo(String loggerName, Level savedLevel)
        {
            this.loggerName = loggerName;
            this.savedLevel = savedLevel;
        }

        /**
         * Use this constructor if the config is newly created, and thus there is no level to save for later restoration
         *
         * @param loggerName the logger config
         */
        protected SavedLoggerConfigInfo(String loggerName)
        {
            this.loggerName = loggerName;
        }

        String getLoggerName()
        {
            return loggerName;
        }

        Level getSavedLevel()
        {
            return savedLevel;
        }

        boolean wasCreated()
        {
            return savedLevel == null;
        }
    }

    static
    {
        loggingStream = new ByteArrayOutputStream();

        inMemoryAppender = OutputStreamAppender.createAppender(null, null, loggingStream, TEST_APPENDER_NAME, false, true);

        inMemoryAppender.start();
    }


    /**
     * Configured the root logger log level, before tests are run in this class. By default, the ALL level is used to
     * maximize coverage of conditionally executed code in log statements. THe current value is saved so it can be
     * restored thereafter.
     */
    @BeforeClass
    static public void configureRootLoggerLevel()
    {
        // log at ALL level, unless the system property is set
        Level level = DEFAULT_TEST_LOG_LEVEL;
        String configuredLevelStr = System.getProperty(LOG_LEVEL_PROPERTY);
        if (configuredLevelStr != null)
        {
            boolean isLevelValid = Arrays.stream(Level.values())
                                         .map((value) -> value.toString())
                                         .anyMatch(configuredLevelStr::equalsIgnoreCase);

            if (isLevelValid)
            {
                level = Level.toLevel(configuredLevelStr);
            }
            else
            {
                logger.error("Unrecognized '{}' log level configured for root logger, defaulting to '{}'",
                        configuredLevelStr, level);
            }
        }

        LoggerConfig loggerConfig = loggingConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        savedRootLoggerLevel = loggerConfig.getLevel();
        loggerConfig.setLevel(level);
        loggingContext.updateLoggers();
    }

    /**
     * Restores the log level to its original value.
     */
    @AfterClass
    static public void restoreRootLoggerLevel()
    {
        LoggerConfig loggerConfig = loggingConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(savedRootLoggerLevel);
        loggingContext.updateLoggers();
    }

    /**
     * Sets up the in-memory appenders for the specified loggers, so that the output at the specified level (& lower)
     * can be captured and asserted on.
     *
     * @param level       Log level
     * @param loggerNames Loggers for which set level and to capture the output
     */
    protected void startCapturingLogOutput(Level level, String... loggerNames)
    {
        this.clearCapturedOutput();

        for (String loggerName : loggerNames)
        {
            LoggerConfig loggerConfig = loggingConfig.getLoggerConfig(loggerName);
            if (loggerName.equals(loggerConfig.getName()))
            {
                // This logger already has its own configuration node. Save the current level, update the level
                // save the configuration, and add the in-memory appender.

                savedLoggerConfigs.add(new SavedLoggerConfigInfo(loggerName, loggerConfig.getLevel()));
                loggerConfig.setLevel(level);
                loggerConfig.addAppender(inMemoryAppender, Level.ALL, null);
            }
            else
            {
                // This logger uses the config of a parent node. Create a new LoggerConfig, with the specified
                // level and in-memory appender, and adds it to the configuration.

                savedLoggerConfigs.add(new SavedLoggerConfigInfo(loggerName));
                loggerConfig = new LoggerConfig(loggerName, level, true);
                loggerConfig.addAppender(inMemoryAppender, level, null);
                loggingConfig.addLogger(loggerName, loggerConfig);
            }
        }

        // apply the config changes to loggers
        loggingContext.updateLoggers();
    }


    /**
     * Clears captured output.
     */
    public void clearCapturedOutput()
    {
        loggingStream.reset();
    }

    /**
     * Returns the log output captured.
     *
     * @return captured output as a string.
     */
    public String getCapturedLogOutput()
    {
        return loggingStream.toString();
    }

    @After
    public void restoreState()
    {
        if (savedLoggerConfigs.isEmpty())
        {
            // Avoids applying changes to loggers when no changes have been done to logger configuration.
            return;
        }

        for (SavedLoggerConfigInfo configInfo : savedLoggerConfigs)
        {
            if (configInfo.wasCreated())
            {
                // this logger config was specifically created for this test, remove it
                loggingConfig.removeLogger(configInfo.getLoggerName());
            }
            else
            {
                // this logger already existed, restore its level and remove the in-memory appender
                LoggerConfig loggerConfig = loggingConfig.getLoggerConfig(configInfo.getLoggerName());

                if (!loggerConfig.getName()
                                 .equals(configInfo.getLoggerName()))
                {
                    // this would only happen if the test case, or production code modified the logging config.
                    // This is not expected, and in this situation this class should not be used.
                    throw new IllegalStateException(
                            "Unexpected change to logging configuration, cannot restore state.");
                }

                loggerConfig.setLevel(configInfo.getSavedLevel());
                loggerConfig.removeAppender(TEST_APPENDER_NAME);
            }
        }

        savedLoggerConfigs.clear();

        // apply the config changes to loggers
        loggingContext.updateLoggers();

        clearCapturedOutput();
    }
}
