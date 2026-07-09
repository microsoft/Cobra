package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SnmpErrorListenerTest
{
    private final SnmpResponseTrapSender mockSnmpResponseTrapSender = mock(SnmpResponseTrapSender.class);

    @Test
    public void notifyTest() {
        SnmpErrorListener listener = new SnmpErrorListener(mockSnmpResponseTrapSender);

        // Setup
        ResponseCode responseCode1 = ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_NMAS_ERROR);

        ResponseResult responseResult = new ResponseResult(responseCode1);
        listener.notify(responseResult);

        List<Map<Integer, String>> result = new ArrayList<>();
        HashMap<Integer, String> trapMessage = new HashMap<>();

        trapMessage.put(0, String.valueOf(responseCode1.getCode()));
        trapMessage.put(1, String.valueOf(responseCode1.getSeverity()));
        trapMessage.put(2, responseCode1.getMessage());
		trapMessage.put(3, responseCode1.getInternalMessage());

        result.add(trapMessage);

        try {
            Thread.yield();
            Thread.sleep(1000); //snmp trap sender is async
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(mockSnmpResponseTrapSender).sendTrap(result);
    }

    @Test
    public void testThreadPool() {
        int trapsCount = 100;
        SnmpErrorListener listener = new SnmpErrorListener(mockSnmpResponseTrapSender);

        // Setup
        ResponseCode responseCode1 = ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_NMAS_ERROR);

        ResponseResult responseResult = new ResponseResult(responseCode1);

        for (int i = 0; i < trapsCount; ++i) {
            listener.notify(responseResult);
        }

        List<Map<Integer, String>> result = new ArrayList<>();
        HashMap<Integer, String> trapMessage = new HashMap<>();

        trapMessage.put(0, String.valueOf(responseCode1.getCode()));
        trapMessage.put(1, String.valueOf(responseCode1.getSeverity()));
        trapMessage.put(2, responseCode1.getMessage());
        trapMessage.put(3, responseCode1.getInternalMessage());
        result.add(trapMessage);

        try {
            Thread.yield();
            Thread.sleep(5000); //snmp trap sender is async
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(mockSnmpResponseTrapSender, VerificationModeFactory.times(trapsCount)).sendTrap(result);
    }

}