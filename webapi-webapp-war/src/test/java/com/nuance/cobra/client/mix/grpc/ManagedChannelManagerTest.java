package com.nuance.cobra.client.mix.grpc;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ManagedChannelManagerTest
{
    CombinedConfiguration configs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    Configuration config = CobraConfigurationUtils.getHighestConfig(configs);

    @Test
    public void managedChannelShutdown()
    {
        ManagedChannel managedChannel = mock(ManagedChannel.class);
        ManagedChannelManager.managedChannelShutdown(configs, managedChannel);
        verifyNoInteractions(managedChannel);

        config.setProperty(WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.key, 0);
        ManagedChannelManager.managedChannelShutdown(configs, managedChannel);
        verify(managedChannel).shutdown();
    }

    @Test
    public void managedChannelShutdownNow()
    {
        ManagedChannel managedChannel = mock(ManagedChannel.class);
        ManagedChannelManager.managedChannelShutdownNow(configs, managedChannel);
        verifyNoInteractions(managedChannel);

        config.setProperty(WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.key, 0);
        ManagedChannelManager.managedChannelShutdownNow(configs, managedChannel);
        verify(managedChannel).shutdownNow();
    }

    @Test
    public void scheduleShutdownOfManagedChannel()
    {
        ManagedChannel managedChannel = mock(ManagedChannel.class);
        InterceptedManagedChannel interceptedManagedChannel = mock(InterceptedManagedChannel.class);
        when(interceptedManagedChannel.getChannel()).thenReturn(managedChannel);

        ManagedChannelManager.ChannelBuilder.scheduleShutdownOfManagedChannel(interceptedManagedChannel, 0);
        verifyNoInteractions(interceptedManagedChannel);

        ManagedChannelManager.ChannelBuilder.scheduledShutdownOfManagedChannel(interceptedManagedChannel);
        verify(managedChannel).shutdown();
    }

    @Test
    public void testSetServiceConfig() {
        ManagedChannelBuilder mockChannel = mock(ManagedChannelBuilder.class);
        when(mockChannel.defaultServiceConfig(any())).thenReturn(mockChannel);
        assertTrue(ManagedChannelManager.ChannelBuilder.setServiceConfig(mockChannel, "grpc_service_config.json"));
    }

    @Test
    public void testSetServiceConfigNotFound() {
        ManagedChannelBuilder mockChannel = mock(ManagedChannelBuilder.class);
        when(mockChannel.defaultServiceConfig(any())).thenReturn(mockChannel);
        assertFalse(ManagedChannelManager.ChannelBuilder.setServiceConfig(mockChannel, "/not_found.json"));
    }
}