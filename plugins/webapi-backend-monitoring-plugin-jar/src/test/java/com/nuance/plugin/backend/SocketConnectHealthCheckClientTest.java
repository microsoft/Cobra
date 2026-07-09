package com.nuance.plugin.backend;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.fail;

public class SocketConnectHealthCheckClientTest {
    HttpServer server;
    int serverPort;

    @Before
    public void setUp()
            throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0); //use port 0 to get a free ephemeral port.
        server.setExecutor(null); // creates a default executor
        server.start();

        serverPort = server.getAddress().getPort();
    }

    @Test
    public void checkHealthGood() {
        try {
            checkHealth("127.0.0.1", serverPort);
        } catch (Exception e) {
            fail();
        }
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void checkHealthUnknownHost() {
        try {
            checkHealth("www.nuance5145335.com", 443);
            fail();
        } catch (Exception e) {
            //success
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
            //success
        }
    }

    public void checkHealth(String host, int port) throws HealthCheckException
    {
        SocketConnectHealthCheckClient client = new SocketConnectHealthCheckClient(host, port);
        client.checkHealth();
    }
}