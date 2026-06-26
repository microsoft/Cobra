package com.nuance.nmsp.servlet.nmas.dictation;


import com.nuance.ncs.nmsp.engine.pdx.PdxQueryResult;
import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspSequence;
import com.nuance.nmsp.servlet.Transaction;
import com.nuance.nmsp.servlet.nmas.NMASListener;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

public class NMDPDictationCmdListener
        extends NMASListener
{
    private static final Logger log = ESAPI.getLogger(NMDPDictationCmdListener.class);

    private final String nlEscapeSequence;

    private final boolean escapeNewLines;
    private final int nBestNumber;

    public NMDPDictationCmdListener(Transaction transaction, boolean escapeNewLines, int nBestNumber,
            String nlEscapeSequence)
    {
        super(transaction);
        this.escapeNewLines = escapeNewLines;
        this.nlEscapeSequence = nlEscapeSequence;

        if (nBestNumber > 0 && nBestNumber <= 10)
        {
            this.nBestNumber = nBestNumber;
        }
        else
        {
            this.nBestNumber = 10;
        }
    }

    @Override
    public void onQueryResult(PdxQueryResult result)
    {
        NmspDictionary data = result.getData();
        log.info(Logger.EVENT_UNSPECIFIED, "Result final=" + result.isFinal());

        if (data.containsKey("transcriptions"))
        {
            NmspSequence transcriptionsSeq = data.getSequence("transcriptions");
            StringBuilder strBuffer = new StringBuilder();

            for (int index = 0; index < transcriptionsSeq.size() && index < nBestNumber; index++)
            {
                String transcript = transcriptionsSeq.getUTF8String(index);

                if (escapeNewLines) {
                    transcript = transcript.replace("\n", nlEscapeSequence);
                }
                strBuffer.append(transcript).append("\n");
            }
            transaction.setResults(strBuffer.toString());
        }
        else if (data.containsKey("nlsml_results"))
        {
            transaction.setResults(data.getUTF8String("nlsml_results"));
        }

        transaction.setRequestCompleted(true);
    }
}
