package com.nuance.cobra.client.generic;

import com.nuance.cobra.utils.HealthCheckException;
import org.junit.Before;
import org.junit.Test;
import com.nuance.cobra.test.CobraTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import static org.junit.Assert.*;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class SocketConnectHealthCheckClientTest extends CobraTest
{
    @Before
    public void setUp()
            throws Exception
    {
        super.setUp();
    }

    @Test
    public void checkHealthGood()
    {
        try
        {
            checkHealth("www.example.com", 443);
        } catch (Exception e)
        {
            fail();
        }
    }

    @Test
    public void checkHealthUnknownHost()
    {
        try
        {
            checkHealth("www.nuance5145335.com", 443);
            fail();
        } catch (Exception e)
        {
        }
    }

    @Test
    public void checkHealthTimeout()
    {
        try
        {
            checkHealth("www.example.com", 81);
            fail();
        } catch (Exception e)
        {
        }
    }

    public void checkHealth(String host, int port) throws HealthCheckException
    {
        SocketConnectHealthCheckClient client = new SocketConnectHealthCheckClient(host, port);
        client.checkHealth();
    }
}
