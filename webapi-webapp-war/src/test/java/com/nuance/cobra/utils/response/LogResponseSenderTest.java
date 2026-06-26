package com.nuance.cobra.utils.response;

import org.junit.Test;

public class LogResponseSenderTest
{
    @Test
    public void testSendResponse()
            throws Exception
    {
        LogResponseSender tester = new LogResponseSender();
        tester.sendResponse("test string");
    }

    @Test
    public void testSendBinary()
            throws Exception
    {
        LogResponseSender tester = new LogResponseSender();
        tester.sendBinary(new byte[] { 97, 98, 99 });
    }

    @Test
    public void testCloseConnection()
            throws Exception
    {
        LogResponseSender tester = new LogResponseSender();
        tester.closeConnection(null);
    }

}