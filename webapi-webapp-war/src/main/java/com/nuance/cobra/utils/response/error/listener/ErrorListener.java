package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.IResponse;

public interface ErrorListener
{
    void notify(IResponse error);
}
