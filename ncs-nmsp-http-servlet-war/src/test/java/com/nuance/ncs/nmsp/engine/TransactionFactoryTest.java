package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TransactionFactoryTest {

    @Test
    public void newTransactionEngineReturnsInstance() {
        TransactionEngine engine = TransactionFactory.newTransactionEngine();
        assertNotNull(engine);
    }

    @Test
    public void newResponseManagerReturnsInstance() {
        ResponseManager rm = TransactionFactory.newResponseManager(new MockHttpServletResponse(), "ctx");
        assertNotNull(rm);
    }

    @Test
    public void packagePrivateConstructorThrows() throws Exception {
        Constructor<TransactionFactory> ctor = TransactionFactory.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        try {
            ctor.newInstance();
            fail("expected InvocationTargetException wrapping IllegalStateException");
        } catch (InvocationTargetException ite) {
            assertTrue(ite.getCause() instanceof IllegalStateException);
        }
    }
}
