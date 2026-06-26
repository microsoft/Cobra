package com.nuance.cobra.api.response;

import org.junit.Assert;
import org.junit.Test;

public class ResponseCodeTest {

    @Test
    public void generalResponseCodeTest(){
        ResponseCode responseCode = ResponseCode.createResponseErrorForNimService(-8092);

        Assert.assertEquals(1908, responseCode.getCode());

        responseCode = ResponseCode.createResponseErrorForNimService(-8999);

        Assert.assertEquals(1001, responseCode.getCode());

        responseCode = ResponseCode.createResponseErrorForNimService(901);

        Assert.assertEquals(10901, responseCode.getCode());

        Assert.assertEquals("Service error.", responseCode.getMessage());

        Assert.assertEquals("Service error.", responseCode.getInternalMessage());

        String responseString = "code: 10901 message: Service error. message description: Service error.";
        Assert.assertEquals( responseString, responseCode.formattedLogMessage());

        String responseErrorString = "{'type':'result','result':{'final':true,'status':'error','errors':[{'message':'SpeechSynthesis service failed.','code': 1503}]}}";
        Assert.assertEquals(responseErrorString.replace("'","\""), ResponseCode.createResponseErrorString(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR));

        Assert.assertEquals(3, responseCode.getSeverity());

        Assert.assertEquals(false, responseCode.equals(ResponseCode.createResponseError(ExternalResponseErrorCode.NIM_VOCALIZER_NMAS_ERROR)));
    }

    @Test
    public void responseCodeTest(){
        ResponseCode responseCode = ResponseCode.createResponseError(InternalResponseErrorCode.GLOBAL_REGISTRY_URL_MALFORMED);

        Assert.assertEquals(1908, responseCode.getCode());
    }

    @Test
    public void genericResponseCodeTest(){
        ResponseCode responseCode = ResponseCode.createGenericResponse(InternalGenericResponseCode.LOG_POSTING_RESUMED);

        Assert.assertEquals(3000, responseCode.getCode());
    }
}
