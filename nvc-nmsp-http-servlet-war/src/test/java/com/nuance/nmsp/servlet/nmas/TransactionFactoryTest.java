package com.nuance.nmsp.servlet.nmas;

import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.Transaction;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransactionFactoryTest
{
    @Test
    public void newTransaction()
    {
        TransactionFactory tf = new TransactionFactory();
        Transaction t = tf.newTransaction(new AuthorizationInfo("abc123", "xyz123"), "zyx987");
        assertNotNull(t);
    }
}