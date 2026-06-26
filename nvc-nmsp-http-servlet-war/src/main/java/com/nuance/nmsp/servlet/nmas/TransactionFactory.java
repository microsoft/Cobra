package com.nuance.nmsp.servlet.nmas;

import com.nuance.nmsp.servlet.AuthorizationInfo;
import com.nuance.nmsp.servlet.Transaction;

public class TransactionFactory
{
    public Transaction newTransaction(AuthorizationInfo authorization, String uniqueId)
    {
        return new Transaction(authorization, uniqueId);
    }
}
