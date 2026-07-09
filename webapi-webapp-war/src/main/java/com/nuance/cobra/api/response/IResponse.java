package com.nuance.cobra.api.response;

import com.nuance.cobra.utils.CobraException;

import java.util.List;

public interface IResponse
{
    public String toJson()
            throws CobraException;

    public List<ResponseCode> getErrors();
}
