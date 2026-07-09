package com.nuance.plugin.backend;

import java.io.IOException;
import java.net.*;

public class SocketConnectHealthCheckClient
{
    private final SocketAddress socketAddress;
    private final int timeoutMs = 1000;

    public SocketConnectHealthCheckClient(String host, int port)
    {
        socketAddress = new InetSocketAddress(host, port);
    }

    public void checkHealth() throws HealthCheckException
    {
        try (final Socket socket = new Socket())
        {
            socket.connect(socketAddress, timeoutMs);
        }
        catch (IOException e)
        {
            final String suffix;
            if (e instanceof SocketTimeoutException)
            {
                suffix = " -- Connection Timeout";
            }
            else if (e instanceof UnknownHostException)
            {
                suffix = " -- Unknown Host";
            }
            else
            {
                suffix = " -- General Failure";
            }

            throw new HealthCheckException("Health check failed" + suffix, e);
        }
    }

}
