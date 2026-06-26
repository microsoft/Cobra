package com.nuance.cobra.client.mix.grpc;

import io.grpc.ManagedChannel;

import java.time.Instant;

/*
We need this class because the google's managed channel no longer gives access to the interceptor once it is created.
 */
public class InterceptedManagedChannel {
    private final ManagedChannel channel;
    private final HeaderClientInterceptor interceptor;
    private final Instant ceaseNewUsageInstant;

    public InterceptedManagedChannel(ManagedChannel channel, HeaderClientInterceptor interceptor, Instant ceaseNewUsageInstant) {
        this.channel = channel;
        this.interceptor = interceptor;
        this.ceaseNewUsageInstant = ceaseNewUsageInstant;
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public HeaderClientInterceptor getInterceptor() {
        return interceptor;
    }

    public Instant getCeaseNewUsageInstant() { return ceaseNewUsageInstant; }
}
