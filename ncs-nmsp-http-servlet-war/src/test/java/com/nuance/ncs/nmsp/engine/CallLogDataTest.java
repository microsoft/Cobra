package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.servlet.NcsServletHandlerTest;
import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CallLogDataTest
{
    TransactionEngine transactionEngine;
    ResponseManager responseManager;

    @Before
    public void setUp()
    {
        transactionEngine = mock(TransactionEngine.class);
        responseManager = mock(ResponseManager.class);
    }

    @Test(expected = Test.None.class)
    public void addCallLog()
    {
        CallLogData callLogData = new CallLogData(transactionEngine, responseManager, null);
        assertFalse(callLogData.addCallLog("{]"));
        assertTrue(callLogData.addCallLog("{\"key\":\"value\"}"));
        assertTrue(callLogData.addCallLog(NcsServletHandlerTest.asrRequestDataJsonString));
    }
}
