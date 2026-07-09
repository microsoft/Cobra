package com.nuance.cobra.command;

import com.nuance.cobra.utils.response.ResponseFormatter;

/**
 * The BaseResponseFormatter simply returns the same string
 */
public class BaseResponseFormatter implements ResponseFormatter
{
    /**
     * Returns the same string
     * @param response
     *         The unformatted response
     *
     * @return The formatted response
     */
    public String formatResponse(Object response)
    {
        return (String) response;
    }
}
