package com.nuance.cobra.utils;


import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

public class JvmUtilsTest {
    private Logger mockLogger = Mockito.mock(Logger.class);

    @Test
    public void outputJvmMemoryTest(){
        assertNotNull(mockLogger);
        JvmUtils.outputJvmMemory(mockLogger);
    }

}
