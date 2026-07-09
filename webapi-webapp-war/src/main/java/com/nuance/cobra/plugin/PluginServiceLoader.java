package com.nuance.cobra.plugin;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Wrapper around the service loader, that allows successful mocking of the service loader calls with PowerMock
 * for unit test purposes. This is necessary because there are limitations (not fully understood) when mocking
 * the standard service loader. Only the few methods needed are wrapped.
 *
 * @param <T> Interface class being loaded.
 */
public class PluginServiceLoader<T>
        implements Iterable<T>
{
    private ServiceLoader<T> serviceLoader;

    private PluginServiceLoader(ServiceLoader<T> serviceLoader)
    {
        this.serviceLoader = serviceLoader;
    }

    /**
     * Wraps service loading.
     */
    public static <T> PluginServiceLoader<T> load(Class<T> serviceClass, ClassLoader cl)
    {
        return new PluginServiceLoader(ServiceLoader.load(serviceClass, cl));
    }

    /**
     * Wraps service iteration.
     */
    public Iterator<T> iterator()
    {
        return serviceLoader.iterator();
    }
}
