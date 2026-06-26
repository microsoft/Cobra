package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnmpErrorListener
        implements ThrottledErrorListener
{
    private static ExecutorService executor = Executors.newCachedThreadPool(); //this thread pool will resize according to load.

    private SnmpResponseTrapSender localTrapSender;
    public SnmpErrorListener(SnmpResponseTrapSender trapSender)
    {
        localTrapSender = trapSender;
    }

    @Override
    public void notify(IResponse errors)
    {
        executor.submit(
            () -> {
                List<Map<Integer, String>> trapList = new ArrayList<>();
                for (ResponseCode error : errors.getErrors()) {
                    HashMap<Integer, String> trapMessage = new HashMap<>();
                    trapMessage.put(SnmpResponseTrapSender.OID_WEBAPI_CODE, Integer.toString(error.getCode()));
                    trapMessage.put(SnmpResponseTrapSender.OID_WEBAPI_SEVERITY, Integer.toString(error.getSeverity()));
                    trapMessage.put(SnmpResponseTrapSender.OID_WEBAPI_MESSAGE, error.getMessage());
					trapMessage.put(SnmpResponseTrapSender.OID_WEBAPI_INTERNAL_MESSAGE, error.getInternalMessage());


                    trapList.add(trapMessage);
                }
                localTrapSender.sendTrap(trapList);
            }
        );
    }
}
