package com.nuance.cobra.client.generic;

import com.nuance.cobra.client.HealthCheckClient;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.HealthCheckException;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.io.IOException;
import java.net.*;

public class SocketConnectHealthCheckClient implements HealthCheckClient
{
    private final SocketAddress socketAddress;
    private final int timeout;

    public SocketConnectHealthCheckClient(String host, int port)
    {
        socketAddress = new InetSocketAddress(host, port);

        final CombinedConfiguration serverConfig = WebApiConfigManager.getInstance();
        timeout = serverConfig.getInt(WebApiConfig.Property.SOCKET_CONNECT_HEALTH_CHECK_TIMEOUT_MILLISECONDS.key);
    }

    @Override
    public void checkHealth() throws HealthCheckException
    {
        try (final Socket socket = new Socket()) {
            socket.connect(socketAddress, timeout);
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
