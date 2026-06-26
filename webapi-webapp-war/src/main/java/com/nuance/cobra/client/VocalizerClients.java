package com.nuance.cobra.client;

import com.nuance.cobra.utils.EnumUtils;

@java.lang.SuppressWarnings("squid:S00115")
public enum VocalizerClients
{
    nim,
    nvc;

    //Just for consistency
    public String getValue()
    {
        return name();
    }

    public static String[] getValues()
    {
        return EnumUtils.getNames(VocalizerClients.class);
    }
}