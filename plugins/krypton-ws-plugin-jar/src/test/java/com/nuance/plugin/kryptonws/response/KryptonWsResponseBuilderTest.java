package com.nuance.plugin.kryptonws.response;

import com.nuance.plugin.kryptonws.methods.KryptonWsCreateSessionMethod;
import com.nuance.plugin.kryptonws.methods.KryptonWsMethod;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.Session;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class KryptonWsResponseBuilderTest
{

    Session mockSession = mock(Session.class);
    KryptonWsResponseSender mockResponseSender = mock(KryptonWsResponseSender.class);
    KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));
    KryptonWsWebapiResponseListener kryptonWsWebapiResponseListener = new KryptonWsWebapiResponseListener();

    JSONObject methodJson = new JSONObject().put("method", KryptonWsCreateSessionMethod.NAME);
    KryptonWsMethod kryptonWsMethod = new KryptonWsCreateSessionMethod(methodJson, kryptonWsSession);

    @Before
    public void setUp() {
        kryptonWsMethod.setMethodName(KryptonWsCreateSessionMethod.NAME);
        kryptonWsSession.setWebapiResponseListener(kryptonWsWebapiResponseListener)
                        .setKryptonWsWsSession(mockSession)
                        .setKryptonWsResponseSender(mockResponseSender);
    }

    @Test
    public void buildTest()
    {
        kryptonWsMethod.setKryptonWsRequestId("someRequestId");
        kryptonWsSession.setKryptonWsSessionId("someSessionId");
        KryptonWsResponseBuilder builder = new KryptonWsResponseBuilder(kryptonWsMethod)
                .setStatus(KryptonWsMessage.STATUS.COMPLETE)
                .setCompletionCause(KryptonWsMessage.COMPLETION_CAUSE.ERROR)
                .setVersion("2.33");

        builder.addError(null);
        builder.addError("some error message", "some extra message");

        KryptonWsResponse res = builder.build();
        JSONObject jsonObject = new JSONObject(res.toJson());
        System.out.println(jsonObject.toString(4));
        assertEquals(KryptonWsCreateSessionMethod.NAME, jsonObject.get("responseTo"));
        assertEquals("Response", jsonObject.get("method"));
        assertEquals("complete", jsonObject.get("status"));
        assertEquals("2.33", jsonObject.get("version"));
        assertEquals("ERROR", jsonObject.get("completionCause"));
        assertEquals("someRequestId", jsonObject.get("requestId"));
        assertEquals("someSessionId", jsonObject.get("sessionId"));
    }

    @Test
    public void buildError()
    {
        Map<String, String> error;

        error = KryptonWsResponseBuilder.buildError("some error", null);
        assertEquals("some error", error.get("message"));

        error = KryptonWsResponseBuilder.buildError("some error2", "some extra info2");
        assertEquals("some extra info2", error.get("extra"));
    }
}