package com.nuance.cobra.utils;

import org.apache.logging.log4j.Logger;

public class JvmUtils
{
    public static void outputJvmMemory(Logger logger) {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        logger.warn("JVM Free memory (MB): {}", (freeMemory / 1048576));
        logger.warn("JVM Allocated memory (MB): {}", (allocatedMemory / 1048576));
        logger.warn("JVM Max memory (MB): {}", (maxMemory / 1048576));
        //logger.warn("Total free memory (MB): {}",((freeMemory + (maxMemory - allocatedMemory)) / 1048576));
    }
}
