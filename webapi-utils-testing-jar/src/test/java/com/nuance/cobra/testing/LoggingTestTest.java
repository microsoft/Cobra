package com.nuance.cobra.testing;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests to verify the LoggingTest base class (the ...TestTest name is not a typo!)
 */
public class LoggingTestTest
{
    Logger logger = LogManager.getLogger(LoggingTestTest.class);

    final static String COM_NUANCE_LOGGER_NAME = "com.nuance";
    final static String RANDOM_PACKAGE_NAME = "XLxL3JvlbLciO4d50yeCowfa";
    final static String TEST_LOGGER_NAME = COM_NUANCE_LOGGER_NAME + "." + RANDOM_PACKAGE_NAME;
    final static String TEST_L1_LOGGER_NAME = TEST_LOGGER_NAME + ".package";
    final static String TEST_L2_LOGGER_NAME = TEST_L1_LOGGER_NAME + ".subpackage";
    final static String TEST_LEAF_LOGGER_NAME = TEST_L2_LOGGER_NAME + ".leaf";

    final static String[] RANDOM_STRINGS = {
            "kpSsiFJzHhWy8jG5B8Aq0K3M", "Mxu1qWvQHy3rL12dgyPsxMAk", "8RHpHZgK7bD9pTbhEodmhNq5",
            "XnrVhNRYGv8xyrQV0lk8SNO3", "XnrVhNRYGv8xyrQV0lk8SNO3"
    };

    /**
     * Checks the introduction of a new log configuration between the root and the leaf logger, and the
     * restoration of initial state, in the case when the leaf was originally using (inheriting) the root logger
     * config
     */
    @Test
    public void testRestoreNewLoggerConfig()
    {
        //  logging test instance used for the tests
        LoggingTest loggingTest = new LoggingTest()
        {
        };

        Logger testLogger = LogManager.getLogger(TEST_LOGGER_NAME);
        Logger testL2Logger = LogManager.getLogger(TEST_L2_LOGGER_NAME);
        Logger testLeafLogger = LogManager.getLogger(TEST_LEAF_LOGGER_NAME);

        verifyDefaultLogConfig();

        try
        {
            loggingTest.startCapturingLogOutput(Level.DEBUG, TEST_L2_LOGGER_NAME);

            // Check that an intermediate logger config has been introduced between the leaf and root loggers
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();

            LoggerConfig leafLoggerConfig = config.getLoggerConfig(TEST_LEAF_LOGGER_NAME);
            assertFalse(LogManager.ROOT_LOGGER_NAME.equals(leafLoggerConfig.getName()));
        }
        finally
        {
            // Ensure state is restored even if test fails. For test classes deriving from LoggingTest
            // this would be done automatically by the JUnit framework.
            loggingTest.restoreState();
        }

        // Verify that the configuration has been restored to its original state
        verifyDefaultLogConfig();
    }

    /**
     * Ensures current logging configuration will not interfere with the tests, or that the config has
     * been restored to its default state
     */
    private void verifyDefaultLogConfig()
    {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        LoggerConfig rootLoggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        assertTrue(LogManager.ROOT_LOGGER_NAME.equals(rootLoggerConfig.getName()));

        // checks that in the current configuration there is no intermediate node (so the config is the root
        // node's one.
        LoggerConfig leafLoggerConfig = config.getLoggerConfig(TEST_LEAF_LOGGER_NAME);
        assertTrue(LogManager.ROOT_LOGGER_NAME.equals(leafLoggerConfig.getName()));
    }

    /**
     * Checks the modification and restoration of the configuration of the specific node, in the case when the
     * specified logger originally had its own configuration.
     */
    @Test
    public void testRestoreExistingLoggerConfig()
    {
        // logging test instance used for the tests
        LoggingTest loggingTest = new LoggingTest()
        {
        };

        Logger testLogger = LogManager.getLogger(TEST_LOGGER_NAME);
        Logger testL2Logger = LogManager.getLogger(TEST_L2_LOGGER_NAME);
        Logger testLeafLogger = LogManager.getLogger(TEST_LEAF_LOGGER_NAME);

        verifyDefaultLogConfig();

        LoggerContext loggingContext = (LoggerContext) LogManager.getContext(false);
        Configuration loggingConfig = loggingContext.getConfiguration();

        try
        {
            LoggerConfig loggerConfig = new LoggerConfig(TEST_L2_LOGGER_NAME, Level.WARN, true);
            loggingConfig.addLogger(TEST_L2_LOGGER_NAME, loggerConfig);
            loggingContext.updateLoggers();

            assertTrue(Level.WARN.equals(LogManager.getLogger(TEST_L2_LOGGER_NAME)
                                                   .getLevel()));

            loggingTest.startCapturingLogOutput(Level.DEBUG, TEST_L2_LOGGER_NAME);
            assertTrue(Level.DEBUG.equals(LogManager.getLogger(TEST_L2_LOGGER_NAME)
                                                    .getLevel()));

            loggingTest.restoreState();
            assertTrue(Level.WARN.equals(LogManager.getLogger(TEST_L2_LOGGER_NAME)
                                                   .getLevel()));
        }
        finally
        {
            loggingConfig.removeLogger(TEST_L2_LOGGER_NAME);
            loggingContext.updateLoggers();
        }
    }

    /**
     * Checks restoring the state when there has been no prior change.
     */
    @Test
    public void testRestoreNoChange()
    {
        //  logging test instance used for the tests
        LoggingTest loggingTest = new LoggingTest()
        {
        };

        loggingTest.restoreState();

        // Confirms that the logger configuration state is in its expected state
        verifyDefaultLogConfig();
    }

    /**
     * Various log capture + clearing captured output.
     */
    @Test
    public void testCaptureLogging()
    {
        verifyDefaultLogConfig();

        //  logging test instance used for the tests
        LoggingTest loggingTest = new LoggingTest()
        {
        };

        int strIndex = 0;
        final String SHOULD_NOT_BE_CAPTURED = RANDOM_STRINGS[strIndex++];

        Logger testLogger = LogManager.getLogger(TEST_LOGGER_NAME);
        Logger testL2Logger = LogManager.getLogger(TEST_L2_LOGGER_NAME);
        Logger testLeafLogger = LogManager.getLogger(TEST_LEAF_LOGGER_NAME);

        // will be logged, but not captured
        testLogger.info("should not be captured: {}", SHOULD_NOT_BE_CAPTURED);
        testL2Logger.info("should not be captured: {}", SHOULD_NOT_BE_CAPTURED);
        testLeafLogger.info("should not be captured: {}", SHOULD_NOT_BE_CAPTURED);

        // checks nothing has been captured
        assertTrue(loggingTest.getCapturedLogOutput()
                              .isEmpty());

        try
        {
            // start capturing level 2, any child logger at debug should be capture, anything at trace or in
            // parent loggers should not.
            loggingTest.startCapturingLogOutput(Level.DEBUG, TEST_L2_LOGGER_NAME);

            // parent
            testLogger.debug("should not be captured: {}", SHOULD_NOT_BE_CAPTURED);
            // trace (level too low)
            testLeafLogger.trace("should not be captured: {}", SHOULD_NOT_BE_CAPTURED);

            // checks still nothing was captured
            assertTrue(loggingTest.getCapturedLogOutput()
                                  .isEmpty());

            final String SHOULD_BE_CAPTURED_1 = RANDOM_STRINGS[strIndex++];
            final String SHOULD_BE_CAPTURED_2 = RANDOM_STRINGS[strIndex++];
            final String SHOULD_BE_CAPTURED_3 = RANDOM_STRINGS[strIndex++];

            testL2Logger.debug("should be captured: {}", SHOULD_BE_CAPTURED_1);
            testLeafLogger.debug("should be captured: {}", SHOULD_BE_CAPTURED_2);
            testLeafLogger.info("should be captured: {}", SHOULD_BE_CAPTURED_3);

            System.out.println("Output = " + loggingTest.getCapturedLogOutput());

            // checks output has been captured as expected
            assertTrue(loggingTest.getCapturedLogOutput()
                                  .contains(SHOULD_BE_CAPTURED_1));
            assertTrue(loggingTest.getCapturedLogOutput()
                                  .contains(SHOULD_BE_CAPTURED_2));
            assertTrue(loggingTest.getCapturedLogOutput()
                                  .contains(SHOULD_BE_CAPTURED_3));

            // validates clear captured output functionality
            loggingTest.clearCapturedOutput();
            assertTrue(loggingTest.getCapturedLogOutput()
                                  .isEmpty());
        }
        finally
        {
            // Ensure state is restored even if test fails. For test classes deriving from LoggingTest
            // this would be done automatically by the JUnit framework.
            loggingTest.restoreState();
        }

        // Confirms that the logger configuration state has been restored to its original state
        verifyDefaultLogConfig();
    }

    /**
     * Checks the function used to configure the root logger, with default ALL value. This function would normally
     * be invoked before tests in a test class derived from LoggingTest are executed.
     */
    @Test
    public void testConfigureRootLoggerLevelDefault()
    {
        Logger rootLogger = LogManager.getRootLogger();
        assertEquals(Level.INFO, rootLogger.getLevel());

        try
        {
            LoggingTest.configureRootLoggerLevel();
            assertEquals(Level.ALL, rootLogger.getLevel());
        }
        finally
        {
            LoggingTest.restoreRootLoggerLevel();
            assertEquals(Level.INFO, rootLogger.getLevel());
        }
    }

    /**
     * Checks the function used to configure the root logger, with the system property configuring it set to a
     * non-default value.
     */
    @Test
    public void testConfigureRootLoggerLevelWithProperty()
    {
        Logger rootLogger = LogManager.getRootLogger();
        assertEquals(Level.INFO, rootLogger.getLevel());

        try
        {
            System.setProperty(LoggingTest.LOG_LEVEL_PROPERTY, "DeBuG");
            LoggingTest.configureRootLoggerLevel();
            assertEquals(Level.DEBUG, rootLogger.getLevel());
        }
        finally
        {
            LoggingTest.restoreRootLoggerLevel();
            assertEquals(Level.INFO, rootLogger.getLevel());
        }
    }

    /**
     * Checks the function used to configure the root logger with the system property configuring it set to a
     * specific value.
     */
    @Test
    public void testConfigureRootLoggerLevelWithPropertyInvalid()
    {
        Logger rootLogger = LogManager.getRootLogger();
        assertEquals(Level.INFO, rootLogger.getLevel());

        try
        {
            System.setProperty(LoggingTest.LOG_LEVEL_PROPERTY, "invalid");
            LoggingTest.configureRootLoggerLevel();

            // Level should be set to the default ince property value was invalid.
            assertEquals(LoggingTest.DEFAULT_TEST_LOG_LEVEL, rootLogger.getLevel());
        }
        finally
        {
            LoggingTest.restoreRootLoggerLevel();
            assertEquals(Level.INFO, rootLogger.getLevel());
        }
    }
}
