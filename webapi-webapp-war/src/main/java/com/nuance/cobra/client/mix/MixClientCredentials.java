package com.nuance.cobra.client.mix;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Base64;
import java.util.List;

public class MixClientCredentials implements Serializable
{
    private String mixClientAppId;
    private List<String> mixClientSecrets;
    private String mixClientScope;
    private String mixClientForm;
    private String mixAuthUri;

    public MixClientCredentials(String mixClientAppId, List<String> mixClientSecrets, String mixClientScope, String mixClientForm, String mixAuthUri)
    {
        this.mixClientAppId = mixClientAppId;
        this.mixClientSecrets = mixClientSecrets;
        this.mixClientScope = mixClientScope;
        this.mixClientForm = mixClientForm;
        this.mixAuthUri = mixAuthUri;
    }

    public String getMixClientScope()
    {
        return mixClientScope;
    }

    public String getMixClientForm()
    {
        return mixClientForm;
    }

    public String getMixClientAppId()
    {
        return mixClientAppId;
    }

    public List<String> getMixClientSecrets()
    {
        return mixClientSecrets;
    }

    public String getMixAuthUri() { return mixAuthUri; }

    public String serialize()
    {
        byte[] bytes = SerializationUtils.serialize(this);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static MixClientCredentials deserialize(String data)
    {
        byte[] bytes = Base64.getDecoder().decode(data);
        return SerializationUtils.deserialize(bytes);
    }
}
