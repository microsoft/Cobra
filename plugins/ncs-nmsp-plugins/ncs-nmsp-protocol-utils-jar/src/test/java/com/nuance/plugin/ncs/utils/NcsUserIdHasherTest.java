package com.nuance.plugin.ncs.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
//@PowerMockIgnore({"javax.net.ssl.*","javax.security.*","org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
@PowerMockIgnore({"javax.crypto.*"})
@PrepareForTest({LogManager.class, Configurator.class})
public class NcsUserIdHasherTest
{
    private Logger mockLogger;

    @Before
    public void setUp()
    {
        mockLogger = mock(Logger.class);
        PowerMockito.mockStatic(LogManager.class);
        PowerMockito.mockStatic(Configurator.class);

        BDDMockito.given(LogManager.getLogger(NcsUserIdHasher.class)).willReturn(mockLogger);
        Mockito.reset(mockLogger);
    }

    @Test
    public void main()
            throws IOException
    {
        NcsUserIdHasher.main(new String[0]);
        verify(mockLogger).error(anyString());
        Mockito.reset(mockLogger);

        NcsUserIdHasher.main(new String[]{"exampleuser", "single"});
        verify(mockLogger).info("\"{}\" NCS {} hashes to: {}", "exampleuser", "single", "1e76828a-f303-3b5e-972d-996c9649c0e6");
        Mockito.reset(mockLogger);

        NcsUserIdHasher.main(new String[]{"exampleuser", "double"});
        verify(mockLogger).info("\"{}\" NCS {} hashes to: {}", "exampleuser", "double", "3460a476-ce80-3925-aac5-8e1b4ef14efc");
        Mockito.reset(mockLogger);
    }
}