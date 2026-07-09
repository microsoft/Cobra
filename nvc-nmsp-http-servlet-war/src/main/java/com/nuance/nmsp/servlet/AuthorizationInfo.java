package com.nuance.nmsp.servlet;

import com.nuance.nmsp.servlet.util.HexToArray;

public class AuthorizationInfo
{

    private final String appId;
    private final byte[] appKey;

    public AuthorizationInfo(String appId, String appKeyString)
    {
        this.appId = appId;
        this.appKey = HexToArray.hexStringToByteArray(appKeyString);
    }

    public String getAppId()
    {
        return appId;
    }

    public byte[] getAppKey()
    {
        return appKey;
    }

}