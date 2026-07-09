package com.nuance.cobra.utils;

import com.nuance.cobra.api.response.ResponseEvent;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.response.StartSessionResponse;
import com.nuance.cobra.test.CobraTest;
import com.nuance.cobra.test.TestUtils;
import com.nuance.cobra.utils.response.ResponseFormatter;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.response.ResponseReceiver;
import com.nuance.cobra.utils.webapi.ResponseSender;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class ResponseHandlerTest extends CobraTest
{
    private final ResponseSender mockResponseSender = mock(ResponseSender.class);
    private final ResponseReceiver mockResponseReceiver = mock(ResponseReceiver.class);
    private final ResponseFormatter mockResponseFormatter = mock(ResponseFormatter.class);

    @Before
    public void setUp()
            throws Exception
    {

    }

    @After
    public void tearDown()
            throws Exception
    {

    }

    @Test
    public void test_constructor() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);

        final Object sender = TestUtils.getFieldValue(ResponseHandler.class, handler, "sender");
        assertNull(sender);

        final Object formatter = TestUtils.getFieldValue(ResponseHandler.class, handler, "formatter");
        assertNull(formatter);

        final Object receiver = TestUtils.getFieldValue(ResponseHandler.class, handler, "receiver");
        assertNull(receiver);
    }

    @Test
    public void test_setSender_null() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);

        final Object value = TestUtils.getFieldValue(ResponseHandler.class, handler, "sender");
        assertNull(value);
    }

    @Test
    public void test_setSender_notNull() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);

        final Object value = TestUtils.getFieldValue(ResponseHandler.class, handler, "sender");
        assertTrue(value == mockResponseSender);
    }

    @Test
    public void test_getSender_null() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);
        TestUtils.inject(handler, ResponseHandler.class, "sender", null);

        assertTrue(null == handler.getSender());
    }

    @Test
    public void test_getSender_notNull() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);
        TestUtils.inject(handler, ResponseHandler.class, "sender", mockResponseSender);

        assertTrue(mockResponseSender == handler.getSender());
    }



    @Test
    public void test_setFormatter_null() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);

        final Object value = TestUtils.getFieldValue(ResponseHandler.class, handler, "formatter");
        assertNull(value);
    }

    @Test
    public void test_setFormatter_notNull() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, null);

        final Object value = TestUtils.getFieldValue(ResponseHandler.class, handler, "formatter");
        assertTrue(value == mockResponseFormatter);
    }

    @Test
    public void test_getFormatter_null() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);
        TestUtils.inject(handler, ResponseHandler.class, "formatter", null);

        assertTrue(null == handler.getFormatter());
    }

    @Test
    public void test_getFormatter_notNull() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);
        TestUtils.inject(handler, ResponseHandler.class, "formatter", mockResponseFormatter);

        assertTrue(mockResponseFormatter == handler.getFormatter());
    }



    @Test
    public void test_setReceiver_null() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, null);

        final Object value = TestUtils.getFieldValue(ResponseHandler.class, handler, "receiver");
        assertNull(value);
    }

    @Test
    public void test_setReceiver_notNull() throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(mockResponseReceiver, null, null);

        final Object value = TestUtils.getFieldValue(ResponseHandler.class, handler, "receiver");
        assertTrue(value == mockResponseReceiver);
    }


    @Test(expected=NullPointerException.class)
    public void test_onResponse_nullFormatter() throws Exception
    {
        String resultString = "{ \"QueryResult\": { \"jsessionid\": \"A8E023BFEE99F4CD1AC29285C83B7527\", \"result_type\": \"NINA_START_SESSION_CMD\", \"adk_result\": { \"Response\": { \"request\": { \"servletStartTime\":1490991231437, \"serverVersion\":\"3.0.0\", \"jsessionid\":\"A8E023BFEE99F4CD1AC29285C83B7527\", \"application\":\"NinaCloud\", \"method\":\"StartSession.Execute\", \"requestID\":\"1\", \"servletEndTime\":1490991231440, \"servletExecutionTimeMs\":3, \"ANI\":\"9BBBA2A736754A9CB89B374C2056F039\", \"version\":\"2.0.0\" }, \"grammarsFound\":{ }, \"adk\": { \"agent\": { \"focus\":{ } }, \"agency\":{ }, \"beliefState\": { \"hints\": { \"hints\":{ } } } }, \"provisioning\": { \"nlu.recognition.technology\":\"mrec\", \"mmf.recognition.technology\":\"nr\" }, \"applicationfound\":false } } } }";
        JSONObject jsonObject = new JSONObject(resultString);
        
        final StartSessionResponse response = StartSessionResponse.fromNiMResult(jsonObject);

        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);
        handler.onResponse(response);
    }

    @Test(expected=NullPointerException.class)
    public void test_onResponse_nullResponse_throwsNPE() throws Exception
    {
        final StartSessionResponse response = null;
        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, mockResponseSender);

        when(mockResponseFormatter.formatResponse(null)).thenThrow(new NullPointerException(""));

        handler.onResponse(response);
    }

    @Test
    public void test_onResponse_sender_nullResponse_OK() throws Exception
    {
        final StartSessionResponse response = null;
        final String formattedResponse = "formattedResonse";
        when(mockResponseFormatter.formatResponse(null)).thenReturn(formattedResponse);

        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, mockResponseSender);

        handler.onResponse(response);

        verify(mockResponseSender).sendResponse(formattedResponse);
    }

    @Test
    public void test_onResponse_sender() throws Exception
    {
        
        String resultString = "{ \"QueryResult\": { \"jsessionid\": \"A8E023BFEE99F4CD1AC29285C83B7527\", \"result_type\": \"NINA_START_SESSION_CMD\", \"adk_result\": { \"Response\": { \"request\": { \"servletStartTime\":1490991231437, \"serverVersion\":\"3.0.0\", \"jsessionid\":\"A8E023BFEE99F4CD1AC29285C83B7527\", \"application\":\"NinaCloud\", \"method\":\"StartSession.Execute\", \"requestID\":\"1\", \"servletEndTime\":1490991231440, \"servletExecutionTimeMs\":3, \"ANI\":\"9BBBA2A736754A9CB89B374C2056F039\", \"version\":\"2.0.0\" }, \"grammarsFound\":{ }, \"adk\": { \"agent\": { \"focus\":{ } }, \"agency\":{ }, \"beliefState\": { \"hints\": { \"hints\":{ } } } }, \"provisioning\": { \"nlu.recognition.technology\":\"mrec\", \"mmf.recognition.technology\":\"nr\" }, \"applicationfound\":false } } } }";
        JSONObject jsonObject = new JSONObject(resultString);
        
        final StartSessionResponse response = StartSessionResponse.fromNiMResult(jsonObject);
        final String formattedResponse = "formattedResonse";
        when(mockResponseFormatter.formatResponse(any())).thenReturn(formattedResponse);

        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, mockResponseSender);

        handler.onResponse(response);

        verify(mockResponseSender).sendResponse(formattedResponse);
    }

    @Test
    public void test_onResponse_receiver_nullResponse_OK() throws Exception
    {
        final StartSessionResponse response = null;
        final String formattedResponse = "formattedResonse";
        when(mockResponseFormatter.formatResponse(null)).thenReturn(formattedResponse);
        when(mockResponseReceiver.onResponse(any())).thenReturn(true);

        final ResponseHandler handler = new ResponseHandler(mockResponseReceiver, mockResponseFormatter, mockResponseSender);

        handler.onResponse(response);

        verify(mockResponseReceiver).onResponse(response);
        verify(mockResponseSender).sendResponse(formattedResponse);
    }

    @Test
    public void test_onResponse_receiver() throws Exception
    {
        
        String resultString = "{ \"QueryResult\": { \"jsessionid\": \"A8E023BFEE99F4CD1AC29285C83B7527\", \"result_type\": \"NINA_START_SESSION_CMD\", \"adk_result\": { \"Response\": { \"request\": { \"servletStartTime\":1490991231437, \"serverVersion\":\"3.0.0\", \"jsessionid\":\"A8E023BFEE99F4CD1AC29285C83B7527\", \"application\":\"NinaCloud\", \"method\":\"StartSession.Execute\", \"requestID\":\"1\", \"servletEndTime\":1490991231440, \"servletExecutionTimeMs\":3, \"ANI\":\"9BBBA2A736754A9CB89B374C2056F039\", \"version\":\"2.0.0\" }, \"grammarsFound\":{ }, \"adk\": { \"agent\": { \"focus\":{ } }, \"agency\":{ }, \"beliefState\": { \"hints\": { \"hints\":{ } } } }, \"provisioning\": { \"nlu.recognition.technology\":\"mrec\", \"mmf.recognition.technology\":\"nr\" }, \"applicationfound\":false } } } }";
        JSONObject jsonObject = new JSONObject(resultString);
        
        final StartSessionResponse response = StartSessionResponse.fromNiMResult(jsonObject);
        final String formattedResponse = "formattedResonse";
        when(mockResponseFormatter.formatResponse(any())).thenReturn(formattedResponse);
        when(mockResponseReceiver.onResponse(any(ResponseResult.class))).thenReturn(true);

        final ResponseHandler handler = new ResponseHandler(mockResponseReceiver, mockResponseFormatter, mockResponseSender);

        handler.onResponse(response);

        verify(mockResponseReceiver).onResponse(response);
        verify(mockResponseSender).sendResponse(formattedResponse);
    }



    @Test(expected=NullPointerException.class)
    public void test_sendResponse_formatterNull() throws Exception
    {
        
        String resultString = "{ \"QueryResult\": { \"jsessionid\": \"A8E023BFEE99F4CD1AC29285C83B7527\", \"result_type\": \"NINA_START_SESSION_CMD\", \"adk_result\": { \"Response\": { \"request\": { \"servletStartTime\":1490991231437, \"serverVersion\":\"3.0.0\", \"jsessionid\":\"A8E023BFEE99F4CD1AC29285C83B7527\", \"application\":\"NinaCloud\", \"method\":\"StartSession.Execute\", \"requestID\":\"1\", \"servletEndTime\":1490991231440, \"servletExecutionTimeMs\":3, \"ANI\":\"9BBBA2A736754A9CB89B374C2056F039\", \"version\":\"2.0.0\" }, \"grammarsFound\":{ }, \"adk\": { \"agent\": { \"focus\":{ } }, \"agency\":{ }, \"beliefState\": { \"hints\": { \"hints\":{ } } } }, \"provisioning\": { \"nlu.recognition.technology\":\"mrec\", \"mmf.recognition.technology\":\"nr\" }, \"applicationfound\":false } } } }";
        JSONObject jsonObject = new JSONObject(resultString);
        
        final StartSessionResponse response = StartSessionResponse.fromNiMResult(jsonObject);

        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);

        handler.onResponse(response);
    }

    @Test(expected=NullPointerException.class)
    public void test_sendResponse_senderNull() throws Exception
    {
        final String response = "response";

        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, null);

        handler.sendResponse(response);

    }

    @Test
    public void test_sendResponse_nullOK() throws Exception
    {
        final StartSessionResponse response = null;
        final String formattedResponse = "formattedResonse";
        when(mockResponseFormatter.formatResponse(null)).thenReturn(formattedResponse);

        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, mockResponseSender);

        handler.onResponse(response);

        verify(mockResponseSender).sendResponse(formattedResponse);
    }

    @Test
    public void test_sendResponse() throws Exception
    {
        
        String resultString = "{ \"QueryResult\": { \"jsessionid\": \"A8E023BFEE99F4CD1AC29285C83B7527\", \"result_type\": \"NINA_START_SESSION_CMD\", \"adk_result\": { \"Response\": { \"request\": { \"servletStartTime\":1490991231437, \"serverVersion\":\"3.0.0\", \"jsessionid\":\"A8E023BFEE99F4CD1AC29285C83B7527\", \"application\":\"NinaCloud\", \"method\":\"StartSession.Execute\", \"requestID\":\"1\", \"servletEndTime\":1490991231440, \"servletExecutionTimeMs\":3, \"ANI\":\"9BBBA2A736754A9CB89B374C2056F039\", \"version\":\"2.0.0\" }, \"grammarsFound\":{ }, \"adk\": { \"agent\": { \"focus\":{ } }, \"agency\":{ }, \"beliefState\": { \"hints\": { \"hints\":{ } } } }, \"provisioning\": { \"nlu.recognition.technology\":\"mrec\", \"mmf.recognition.technology\":\"nr\" }, \"applicationfound\":false } } } }";
        JSONObject jsonObject = new JSONObject(resultString);
        final StartSessionResponse response = StartSessionResponse.fromNiMResult(jsonObject);
        final String formattedResponse = "formattedResonse";
        when(mockResponseFormatter.formatResponse(any())).thenReturn(formattedResponse);

        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, mockResponseSender);

        handler.onResponse(response);

        verify(mockResponseSender).sendResponse(formattedResponse);
    }


    @Test(expected=NullPointerException.class)
    public void test_onAudio_nullSender() throws Exception
    {
        final byte[] audio = new byte[] { 0, 0, 0, 0 };

        final ResponseHandler handler = new ResponseHandler(null, null, null);

        handler.onAudio(audio, true);
    }

    @Test(expected=NullPointerException.class)
    public void test_onAudio_nullAudio_senderThrowsNPE() throws Exception
    {
        final byte[] audio = null;
        doThrow(new NullPointerException()).when(mockResponseSender).sendBinary(null);

        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);

        handler.onAudio(audio, true);
    }

    @Test
    public void test_onAudio_nullAudio_senderOK() throws Exception
    {
        final byte[] audio = null;

        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);

        handler.onAudio(audio, true);

        verify(mockResponseSender).sendBinary(audio);
    }

    @Test(expected=NullPointerException.class)
    public void test_onAudio_emptyAudio_senderThrowsNPE() throws Exception
    {
        final byte[] audio = new byte[0];
        doThrow(new NullPointerException()).when(mockResponseSender).sendBinary(aryEq(audio));

        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);

        handler.onAudio(audio, true);
    }

    @Test
    public void test_onAudio_emptyAudio_senderOK() throws Exception
    {
        final byte[] audio = new byte[0];

        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);

        handler.onAudio(audio, true);

        verify(mockResponseSender).sendBinary(audio);
    }

    @Test
    public void testSendRespomse()
            throws Exception
    {
        final ResponseHandler handler = new ResponseHandler(null, null, mockResponseSender);

        handler.sendResponse("test");

        verify(mockResponseSender).sendResponse("test");
    }

    @Test
    public void testSendEvent()
            throws Exception
    {
        when(mockResponseFormatter.formatResponse(any())).thenReturn("test");
        final ResponseHandler handler = new ResponseHandler(null, mockResponseFormatter, mockResponseSender);

        ResponseEvent testEvent = new ResponseEvent("test");

        handler.sendEvent(testEvent);

        verify(mockResponseSender).sendResponse("test");
    }

}
