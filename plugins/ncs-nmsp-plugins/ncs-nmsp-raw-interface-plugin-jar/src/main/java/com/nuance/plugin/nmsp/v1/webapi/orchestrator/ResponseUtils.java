package com.nuance.plugin.nmsp.v1.webapi.orchestrator;

import com.nuance.ncs.nmsp.lang.NCSQueryRetryAndErrorMessages;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXDictionary;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXQueryError;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.PDXQueryRetry;
import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

public class ResponseUtils {
    private static final Logger logger = LogManager.getLogger();

    private ResponseUtils() {
        //hide constructor
    }

    public static byte[] buildRetry(Map<String, Object> queryInfo, Session nmspSession, String name, String description, ResponseCodes.CompletionCause code)
    {
        String commandName = (String) queryInfo.get("command");
        if(commandName.equalsIgnoreCase("DRAGON_NLU_ASR_CMD"))
        {
            return buildRetryDragonNluAsrCmd(queryInfo, nmspSession, code);
        }

        PDXDictionary responseData = new PDXDictionary();
        responseData.addUTF8String("prompt", NCSQueryRetryAndErrorMessages.getRecNoMatchPrompt(queryInfo));
        responseData.addUTF8String("name", name);
        responseData.addInteger("cause", code.getCause());
        responseData.addUTF8String("NMAS_PRFX_TRANSACTION_ID", String.valueOf(nmspSession.getWebAPIOrchestrator().getTransactionID()));
        responseData.addUTF8String("NMAS_PRFX_SESSION_ID", nmspSession.getSessionIdString());
        responseData.addUTF8String("description", description);
        responseData.addUTF8String("_resultType", "NmspQuery");
        responseData.addUTF8String("_statuus", "retry"); //NOT a typo, used by prod customers.
        responseData.addUTF8String("_status", "retry");
        responseData.addInteger("code", code.getCause());
        PDXQueryRetry response = new PDXQueryRetry(responseData.toByteArray());

        return response.toByteArray();
    }

    public static byte[] buildError(Session nmspSession, String description, ResponseCodes.CompletionCause code)
    {
        short transactionId = -1;
        if(nmspSession.getWebAPIOrchestrator() != null)
        {
            transactionId = nmspSession.getWebAPIOrchestrator().getTransactionID();
        }

        PDXDictionary responseData = new PDXDictionary();
        responseData.addUTF8String("description", Objects.requireNonNullElse(description, "Internal server error."));
        responseData.addUTF8String("_resultType", "NmspQuery");
        responseData.addUTF8String("_statuus", "error"); //NOT a typo, used by prod customers.
        responseData.addUTF8String("_status", "error");
        responseData.addInteger("error", code.getCause());
        responseData.addUTF8String("NMAS_PRFX_TRANSACTION_ID", String.valueOf(transactionId));
        responseData.addUTF8String("NMAS_PRFX_SESSION_ID", nmspSession.getSessionIdString());
        PDXQueryError response = new PDXQueryError(responseData.toByteArray());

        return response.toByteArray();
    }

    public static class ReverseEndianDataOutputStream extends DataOutputStream {
        /**
         * Creates a new data output stream to write data to the specified
         * underlying output stream. The counter <code>written</code> is
         * set to zero.
         *
         * @param out the underlying output stream, to be saved for later
         *            use.
         * @see FilterOutputStream#out
         */
        public ReverseEndianDataOutputStream(OutputStream out) {
            super(out);
        }

        public void reverseWriteShort(Short data) throws IOException {
            this.writeShort(Short.reverseBytes(data));
        }

        public void reverseWriteInt(Integer data) throws IOException {
            this.writeInt(Integer.reverseBytes(data));
        }
    }

    private static byte[] buildRetryDragonNluAsrCmd(Map<String, Object> queryInfo, Session nmspSession, ResponseCodes.CompletionCause code)
    {
        PDXDictionary responseData = new PDXDictionary();
        responseData.addInteger("cause", code.getCause());
        responseData.addUTF8String("prompt", NCSQueryRetryAndErrorMessages.getRecNoMatchPrompt(queryInfo));
        responseData.addUTF8String("name", "AUDIO_INFO");
        responseData.addUTF8String("NMAS_PRFX_TRANSACTION_ID", String.valueOf(nmspSession.getWebAPIOrchestrator().getTransactionID()));
        responseData.addUTF8String("NMAS_PRFX_SESSION_ID", nmspSession.getSessionIdString());
        PDXQueryRetry response = new PDXQueryRetry(responseData.toByteArray());

        return response.toByteArray();
    }

}
