package com.nuance.cobra.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;

@SuppressWarnings("java:S2160")
public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V>
{
    private final int maxEntries;
    private final int maxStringLength;

    public MaxSizeHashMap(int maxEntries, int maxStringLength)
    {
        if(maxEntries < 0)
        {
            maxEntries = Integer.MAX_VALUE;
        }
        this.maxEntries = maxEntries;

        if(maxStringLength < 0)
        {
            maxStringLength = Integer.MAX_VALUE;
        }
        this.maxStringLength = maxStringLength;
    }

    public void putWithLimit(K key, V value)
    {
        if(this.size() >= maxEntries &&
           !this.containsKey(key))
        {
            return;
        }

        if(key instanceof String)
        {
            String k = (String) key;
            key = (K) StringUtils.left(k, maxStringLength);
        }

        if(value instanceof String)
        {
            String v = (String) value;
            value = (V) StringUtils.left(v, maxStringLength);
        }

        if (maxStringLength > 0) this.put(key, value);
    }
}
