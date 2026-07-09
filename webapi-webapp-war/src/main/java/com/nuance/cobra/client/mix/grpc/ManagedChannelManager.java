package com.nuance.cobra.client.mix.grpc;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.encryption.Utils;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Charsets.UTF_8;

public class ManagedChannelManager
{
    private static final Logger logger = LogManager.getLogger(ManagedChannelManager.class);

    private static final ConcurrentHashMap<String, List<InterceptedManagedChannel>> interceptedManagedChannelCache = new ConcurrentHashMap<>();
    public static final String DEFAULT_GRPC_RETRY_POLICY_RESOURCE_NAME = "grpc_service_config.json";

    public static class ChannelBuilder {
        private boolean isPlainText;
        private final String host;
        private final int port;
        private int channels = (int) WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.defaultValue;
        private int shutdownSeconds = (int) WebApiConfig.Property.GRPC_CHANNEL_LIFETIME_SECONDS.defaultValue;
        private boolean keepAliveWithoutCalls = (boolean) WebApiConfig.Property.GRPC_CHANNEL_KEEP_ALIVE_WITHOUT_CALLS.defaultValue;
        private int keepAliveTimeSeconds = (int) WebApiConfig.Property.GRPC_CHANNEL_KEEP_ALIVE_TIME_SECONDS.defaultValue;
        private Instant ceaseNewUsageInstant = null;
        private Map<String, String> customHeaders;
        private String authorityOverride = null;

        public ChannelBuilder(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public ChannelBuilder setCustomHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public ChannelBuilder setChannels(int channels) {
            this.channels = channels;
            return this;
        }

        public ChannelBuilder setShutdownSeconds(int shutdownSeconds) {
            this.shutdownSeconds = shutdownSeconds;
            return this;
        }

        public ChannelBuilder setKeepAliveWithoutCalls(boolean keepAliveWithoutCalls)
        {
            this.keepAliveWithoutCalls = keepAliveWithoutCalls;
            return this;
        }

        public ChannelBuilder setKeepAliveTimeSeconds(int keepAliveTimeSeconds)
        {
            this.keepAliveTimeSeconds = keepAliveTimeSeconds;
            return this;
        }

        public ChannelBuilder setCeaseNewUsageInstant(Instant ceaseNewUsageInstant)
        {
            this.ceaseNewUsageInstant = ceaseNewUsageInstant;
            return this;
        }

        public ChannelBuilder setPlainText(boolean isPlainText) {
            this.isPlainText = isPlainText;
            return this;
        }

        public ChannelBuilder setAuthorityOverride(String authorityOverride)
        {
            this.authorityOverride = authorityOverride;
            return this;
        }

        private InterceptedManagedChannel buildChannel() {
            ManagedChannelBuilder<?> managedChannelBuilder = ManagedChannelBuilder.forAddress(host, port);
            if(isPlainText)
            {
                managedChannelBuilder.usePlaintext();
            }

            var interceptor = new HeaderClientInterceptor(customHeaders);

            managedChannelBuilder.intercept(interceptor);

            managedChannelBuilder.keepAliveWithoutCalls(keepAliveWithoutCalls);

            if(keepAliveTimeSeconds != -1)
                managedChannelBuilder.keepAliveTime(keepAliveTimeSeconds, TimeUnit.SECONDS);

            if(authorityOverride != null) managedChannelBuilder.overrideAuthority(authorityOverride);

            setServiceConfig(managedChannelBuilder, DEFAULT_GRPC_RETRY_POLICY_RESOURCE_NAME); // set retry policy on grpc channel

            return new InterceptedManagedChannel(managedChannelBuilder.build(), interceptor, ceaseNewUsageInstant);
        }

        static boolean setServiceConfig(ManagedChannelBuilder<?> channelBuilder, String serviceConfigFilePath) {
            try {
                Map<String, ?> policy = readServiceConfig(serviceConfigFilePath);
                channelBuilder.defaultServiceConfig(policy).enableRetry();
                return true;
            } catch (Exception e) {
                logger.error("Unable to set gRPC Service Config.", e);
            }

            return false;
        }

        static Map<String, Object> readServiceConfig(String serviceConfigFilePath) throws FileNotFoundException {
            String path = WebApiConfigManager.getConfigPath() + serviceConfigFilePath;
            File configFile = new File(path);
            InputStream stream = new FileInputStream(configFile);

            return new Gson().fromJson(
                            new JsonReader(
                                    new InputStreamReader(
                                            Objects.requireNonNull(stream),
                                            UTF_8)),
                            Map.class);
        }

        private InterceptedManagedChannel build() {
            if(channels < 1)
            {
                return buildChannel();
            } else {
                return getCachedChannel();
            }
        }

        private String getHostPort()
        {
            return String.format("%s:%d", host, port);
        }

        private InterceptedManagedChannel getCachedChannel()
        {
            if(interceptedManagedChannelCache.containsKey(getHostPort()))
            {
                return getExistingChannel();
            }

            return getNewChannel();
        }

        private InterceptedManagedChannel getExistingChannel()
        {
            List<InterceptedManagedChannel> managedChannelList = interceptedManagedChannelCache.get(getHostPort());
            InterceptedManagedChannel managedChannel = managedChannelList.get(Utils.getRandomIndex(managedChannelList.size()));
            managedChannel = checkBackoffOrShutdownOrTerminatedChannel(managedChannel, managedChannelList);
            return managedChannel;
        }

        private InterceptedManagedChannel checkBackoffOrShutdownOrTerminatedChannel(InterceptedManagedChannel managedChannel, List<InterceptedManagedChannel> managedChannelList)
        {
            if(Instant.now().isBefore(managedChannel.getCeaseNewUsageInstant()) &&
               !managedChannel.getChannel().isShutdown() &&
               !managedChannel.getChannel().isTerminated() )
            {
                managedChannel.getInterceptor().setCustomHeaders(customHeaders);
                return managedChannel;
            }

            logger.debug("Recreating ManagedChannel for {}", getHostPort());
            managedChannelList.remove(managedChannel);
            managedChannel = buildChannel();
            managedChannelList.add(managedChannel);
            scheduleShutdownOfManagedChannel(managedChannel, shutdownSeconds);

            return managedChannel;
        }

        private InterceptedManagedChannel getNewChannel()
        {
            List<InterceptedManagedChannel> managedChannelList = new ArrayList<>();
            for (var i = 0; i < channels; i++)
            {
                logger.debug("Creating ManagedChannel for {}", getHostPort());
                InterceptedManagedChannel managedChannel = buildChannel();
                managedChannelList.add(managedChannel);

                scheduleShutdownOfManagedChannel(managedChannel, shutdownSeconds);
            }
            interceptedManagedChannelCache.put(getHostPort(), managedChannelList);
            return managedChannelList.get(Utils.getRandomIndex(managedChannelList.size()));
        }

        static void scheduleShutdownOfManagedChannel(InterceptedManagedChannel managedChannel, int shutdownSeconds)
        {
            if (shutdownSeconds <= 0)
            {
                return;
            }

            ThreadContextRunnable task = new ThreadContextRunnable() {
                @Override
                public void runWithContext() {
                    scheduledShutdownOfManagedChannel(managedChannel);
                }
            };

            ScheduledTaskUtils.scheduleTask(task, shutdownSeconds, TimeUnit.SECONDS);
        }

        static void scheduledShutdownOfManagedChannel(InterceptedManagedChannel managedChannel)
        {
            logger.debug("Shutting down ManagedChannel!");
            managedChannel.getChannel().shutdown();
        }
    }

    private ManagedChannelManager()
    {
        throw new IllegalStateException("Utility class");
    }

    public static ChannelBuilder getBuilder(String host, int port) {
        return new ChannelBuilder(host, port);
    }

    public static void managedChannelShutdown(CombinedConfiguration config, ManagedChannel managedChannel)
    {
        if(isOneGrpcChannelPerRequest(config))
        {
            managedChannel.shutdown();
        }
    }

    public static void managedChannelShutdownNow(CombinedConfiguration config, ManagedChannel managedChannel)
    {
        if(isOneGrpcChannelPerRequest(config))
        {
            managedChannel.shutdownNow();
        }
    }

    private static boolean isOneGrpcChannelPerRequest(CombinedConfiguration cc)
    {
        var channels = cc.getInt(WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.key, (Integer) WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.defaultValue);
        return channels < 1;
    }

    public static InterceptedManagedChannel getManagedChannel(String host, int port, boolean isPlainText, CombinedConfiguration cc, Map<String, String> customHeaders)
    {
        return getManagedChannel(host, port, isPlainText, cc, customHeaders, null);
    }

    public static synchronized InterceptedManagedChannel getManagedChannel(String host, int port, boolean isPlainText, CombinedConfiguration cc, Map<String, String> customHeaders, String authorityOverride)
    {
        var channels = cc.getInt(WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.key, (Integer) WebApiConfig.Property.GRPC_CHANNELS_PER_DESTINATION.defaultValue);
        var shutdownSeconds = cc.getInt(WebApiConfig.Property.GRPC_CHANNEL_LIFETIME_SECONDS.key);
        var shutdownBackoffSeconds = cc.getLong(WebApiConfig.Property.GRPC_CHANNEL_LIFETIME_BACKOFF_SECONDS.key);
        var keepAliveTimeSeconds = cc.getInt(WebApiConfig.Property.GRPC_CHANNEL_KEEP_ALIVE_TIME_SECONDS.key);
        var keepAliveWithoutCalls = cc.getBoolean(WebApiConfig.Property.GRPC_CHANNEL_KEEP_ALIVE_WITHOUT_CALLS.key);

        ChannelBuilder builder = getBuilder(host, port);
        builder.setPlainText(isPlainText)
                .setChannels(channels)
                .setShutdownSeconds(shutdownSeconds)
                .setKeepAliveWithoutCalls(keepAliveWithoutCalls)
                .setKeepAliveTimeSeconds(keepAliveTimeSeconds)
                .setCeaseNewUsageInstant(Instant.now().plus(shutdownSeconds - shutdownBackoffSeconds, ChronoUnit.SECONDS))
                .setCustomHeaders(customHeaders)
                .setAuthorityOverride(authorityOverride);

        return builder.build();
    }

    // Only used by unitTest. Maybe we should refactor this.
    public static ConcurrentMap<String, List<InterceptedManagedChannel>> getManagedChannelCache()
    {
        return interceptedManagedChannelCache;
    }
}
