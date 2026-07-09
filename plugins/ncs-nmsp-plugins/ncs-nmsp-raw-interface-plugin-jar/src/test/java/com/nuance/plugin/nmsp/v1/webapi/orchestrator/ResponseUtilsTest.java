package com.nuance.plugin.nmsp.v1.webapi.orchestrator;

import com.nuance.plugin.nmsp.v1.nmspgw.server.ResponseCodes;
import com.nuance.plugin.nmsp.v1.session.Session;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponseUtilsTest
{

    @Test
    public void buildRetry()
    {
        Map<String, Object> queryInfo = Map.of("command", "NMDP_ASR_CMD");
        String name = "NoMatch";
        String description = "No match.";
        ResponseCodes.CompletionCause code = ResponseCodes.CompletionCause.NOMATCH;

        WebAPIOrchestrator webAPIOrchestrator = mock(WebAPIOrchestrator.class);
        Session nmspSession = mock(Session.class);
        when(nmspSession.getSessionIdString()).thenReturn("abc123");
        when(nmspSession.getWebAPIOrchestrator()).thenReturn(webAPIOrchestrator);
        when(webAPIOrchestrator.getTransactionID()).thenReturn((short)2);

        byte[] rc = ResponseUtils.buildRetry(queryInfo, nmspSession, name, description, code);
        assertTrue(rc.length > 0);

        queryInfo = Map.of("command", "DRAGON_NLU_ASR_CMD", "dictation_language", "fra-FRA");
        rc = ResponseUtils.buildRetry(queryInfo, nmspSession, name, description, code);
        assertTrue(rc.length > 0);

        queryInfo = Map.of("command", "DRAGON_NLU_ASR_CMD", "dictation_language", "por-BRA");
        rc = ResponseUtils.buildRetry(queryInfo, nmspSession, name, description, code);
        assertTrue(rc.length > 0);

        queryInfo = Map.of("command", "DRAGON_NLU_ASR_CMD", "dictation_language", "ita-ITA");
        rc = ResponseUtils.buildRetry(queryInfo, nmspSession, name, description, code);
        assertTrue(rc.length > 0);

        queryInfo = Map.of("command", "DRAGON_NLU_ASR_CMD", "dictation_language", "deu-DEU");
        rc = ResponseUtils.buildRetry(queryInfo, nmspSession, name, description, code);
        assertTrue(rc.length > 0);

        queryInfo = Map.of("command", "DRAGON_NLU_ASR_CMD", "dictation_language", "eng-USA");
        rc = ResponseUtils.buildRetry(queryInfo, nmspSession, name, description, code);
        assertTrue(rc.length > 0);
    }
}