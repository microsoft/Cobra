package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.servlet.response.ResponseManager;

import javax.servlet.http.HttpServletResponse;

public class TransactionFactory
{
    TransactionFactory() { throw new IllegalStateException("Utility class!"); }

    public static TransactionEngine newTransactionEngine()
    {
        return new TransactionEngine();
    }

    public static ResponseManager newResponseManager(HttpServletResponse response, String context)
    {
        return new ResponseManager(response, context);
    }
}
