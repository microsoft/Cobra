package com.nuance.nmsp.servlet;

import com.nuance.ncs.nmsp.engine.pdx.PdxClient;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;

public class TransactionTest extends TestCase {

    public void testGeters() {
        AuthorizationInfo testInfo = new AuthorizationInfo("testID", "testAppkey");
        Transaction testTransaction = new Transaction(testInfo, "uniqueID");

        assertEquals(testInfo, testTransaction.getAuthorizationInfo());
        assertEquals("uniqueID", testTransaction.getUniqueId());

        assertTrue(testTransaction.isRequestCompleted());
        testTransaction.setRequestCompleted(false);
        assertFalse(testTransaction.isRequestCompleted());

        PdxClient mockClient = mock(PdxClient.class);
        testTransaction.setClient(mockClient);
        assertEquals(mockClient, testTransaction.getClient());

        testTransaction.setSessionId("testId");
        assertEquals("testId", testTransaction.getSessionId());

        testTransaction.setError("testError");
        assertEquals("testError", testTransaction.getError());

        testTransaction.setErrorID(1);
        assertEquals(1, testTransaction.getErrorID());

        testTransaction.setResults("testResults");
        assertEquals("testResults", testTransaction.getResults());

        testTransaction.cleanup();
    }

    public void testWaitForResponse() {
        AuthorizationInfo testInfo = new AuthorizationInfo("testID", "testAppkey");
        Transaction testTransaction = new Transaction(testInfo, "uniqueID");

        Thread testWaitThread = new Thread() {
            public void run() {
                testTransaction.waitForResponse();
            }
        };

        testWaitThread.start();

        testTransaction.setRequestCompleted(true);

        try {
            testWaitThread.join(100);
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testWaitForResponseInterruptedRestoresFlag() throws Exception {
        AuthorizationInfo testInfo = new AuthorizationInfo("testID", "testAppkey");
        final Transaction testTransaction = new Transaction(testInfo, "uniqueID");
        final boolean[] interruptedSeen = {false};

        Thread waitThread = new Thread() {
            public void run() {
                testTransaction.waitForResponse();
                interruptedSeen[0] = Thread.currentThread().isInterrupted();
            }
        };

        waitThread.start();
        // Spin briefly until the thread is parked inside condition.await()
        long deadline = System.currentTimeMillis() + 1000;
        while (waitThread.getState() != Thread.State.WAITING && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }
        waitThread.interrupt();
        waitThread.join(1000);
        assertFalse(waitThread.isAlive());
        assertTrue("interrupted flag should be restored after catch", interruptedSeen[0]);
    }

    public void testSignalReleasesAwaitingThread() throws Exception {
        AuthorizationInfo testInfo = new AuthorizationInfo("testID", "testAppkey");
        final Transaction testTransaction = new Transaction(testInfo, "uniqueID");

        Thread waitThread = new Thread() {
            public void run() {
                testTransaction.condition.waitForNMSPGateway();
            }
        };

        waitThread.start();
        long deadline = System.currentTimeMillis() + 1000;
        while (waitThread.getState() != Thread.State.WAITING && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }
        // Call signal() directly so we exercise the public method (not the setRequestCompleted path)
        testTransaction.condition.signal();
        waitThread.join(1000);
        assertFalse(waitThread.isAlive());
    }
}
