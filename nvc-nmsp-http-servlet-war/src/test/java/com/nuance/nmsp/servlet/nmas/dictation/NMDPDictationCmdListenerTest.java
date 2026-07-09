package com.nuance.nmsp.servlet.nmas.dictation;

import com.nuance.ncs.nmsp.engine.pdx.PdxQueryResult;
import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspSequence;
import com.nuance.ncs.nmsp.pdx.NmspSequenceImpl;
import com.nuance.nmsp.servlet.Transaction;
import junit.framework.TestCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NMDPDictationCmdListenerTest extends TestCase {

    public void testOnQueryResult() {
        Transaction mockTransaction = mock(Transaction.class);
        NMDPDictationCmdListener testDictationListener =
                new NMDPDictationCmdListener(mockTransaction, true, 2, "\\");

        NmspDictionary emptyData = new NmspDictionaryImpl();
        PdxQueryResult emptyResult = mock(PdxQueryResult.class);
        when(emptyResult.getData()).thenReturn(emptyData);
        when(emptyResult.isFinal()).thenReturn(true);
        testDictationListener.onQueryResult(emptyResult);

        NmspSequence transcriptions = new NmspSequenceImpl();
        transcriptions.addUTF8String("testString");
        NmspDictionary data = new NmspDictionaryImpl();
        data.addSequence("transcriptions", transcriptions);
        PdxQueryResult result = mock(PdxQueryResult.class);
        when(result.getData()).thenReturn(data);
        when(result.isFinal()).thenReturn(true);
        testDictationListener.onQueryResult(result);

        verify(mockTransaction, atLeastOnce()).setResults(any());
        verify(mockTransaction, atLeastOnce()).setRequestCompleted(true);
    }
}
