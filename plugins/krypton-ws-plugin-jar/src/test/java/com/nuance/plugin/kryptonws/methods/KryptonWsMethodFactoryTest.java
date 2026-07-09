package com.nuance.plugin.kryptonws.methods;

import com.nuance.cobra.utils.http.FakeHttpSession;
import com.nuance.cobra.utils.http.WebSocketHttpSession;
import com.nuance.plugin.kryptonws.session.KryptonWsSession;
import com.nuance.plugin.kryptonws.webapi.KryptonWsWebapiResponseListener;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.Session;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class KryptonWsMethodFactoryTest
{
    Session session = new WebSocketHttpSession(new FakeHttpSession());
    KryptonWsSession kryptonWsSession = new KryptonWsSession(mock(ExecutorService.class));

    @Before
    public void setUp()
    {
        kryptonWsSession.setWebapiResponseListener(new KryptonWsWebapiResponseListener())
                        .setKryptonWsWsSession(session);
    }

    @Test
    public void createSession() throws InvocationTargetException
    {
        final KryptonWsMethod kryptonWsMethod = KryptonWsMethodFactory.createMethod(KryptonWsCreateSessionMethodTest.createSessionJsonString,
                kryptonWsSession);
        if(!(kryptonWsMethod instanceof KryptonWsCreateSessionMethod))
        {
            fail();
        }
    }

    @Test
    public void invalidMethod() throws InvocationTargetException
    {
        String invalidMethodJson = "{\n" +
                                   "   \"method\" : \"HacksRUs\"\n" +
                                   "}";
        KryptonWsMethod kryptonWsMethod = KryptonWsMethodFactory.createMethod(invalidMethodJson, kryptonWsSession);
        assertNull(kryptonWsMethod);
    }

}