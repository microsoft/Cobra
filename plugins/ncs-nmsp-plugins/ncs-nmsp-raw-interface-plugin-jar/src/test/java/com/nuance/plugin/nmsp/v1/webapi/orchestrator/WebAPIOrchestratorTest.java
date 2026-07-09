package com.nuance.plugin.nmsp.v1.webapi.orchestrator;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxCommand;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.executor.*;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import junit.framework.TestCase;
import io.netty.channel.Channel;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebAPIOrchestratorTest extends TestCase {

    @Test
    public void test_buildCommand_SpeechRecognitionExecutor() throws IOException {
        IWebApiFeatures mockWebAPIFeatures = mock(IWebApiFeatures.class);
        when(mockWebAPIFeatures.getWebApiEndpoint(any())).thenReturn(null);
        WebAPIOrchestrator webAPIOrchestrator = new WebAPIOrchestrator(mockWebAPIFeatures);
        Session mockSession = new Session(mock(Channel.class), webAPIOrchestrator);
        HashMap<String, Object> queryInfo = new HashMap<>();
        queryInfo.put("RECEIVER_PDX_COMMANDS", new LinkedList<PdxCommand>());
        queryInfo.put("command", "NVC_ASR_CMD");
        queryInfo.put("REQUEST_INFO", "");
        webAPIOrchestrator.queryInfo = queryInfo;
        Executor executor = webAPIOrchestrator.buildCommand(mockSession);
        assertEquals(SpeechRecognitionExecutor.class, executor.getClass());
    }

    @Test
    public void test_buildCommand_SpeechSynthesisExecutor() throws IOException {
        IWebApiFeatures mockWebAPIFeatures = mock(IWebApiFeatures.class);
        when(mockWebAPIFeatures.getWebApiEndpoint(any())).thenReturn(null);
        WebAPIOrchestrator webAPIOrchestrator = new WebAPIOrchestrator(mockWebAPIFeatures);
        Session mockSession = new Session(mock(Channel.class), webAPIOrchestrator);
        HashMap<String, Object> queryInfo = new HashMap<>();
        queryInfo.put("RECEIVER_PDX_COMMANDS", new LinkedList<PdxCommand>());
        queryInfo.put("command", "NVC_TTS_CMD");
        queryInfo.put("TEXT_TO_READ", "");
        webAPIOrchestrator.queryInfo = queryInfo;
        Executor executor = webAPIOrchestrator.buildCommand(mockSession);
        assertEquals(SpeechSynthesisExecutor.class, executor.getClass());
    }

    @Test
    public void test_buildCommand_InterpretationWithSpeechRecognitionExecutor() throws IOException {
        IWebApiFeatures mockWebAPIFeatures = mock(IWebApiFeatures.class);
        when(mockWebAPIFeatures.getWebApiEndpoint(any())).thenReturn(null);
        WebAPIOrchestrator webAPIOrchestrator = new WebAPIOrchestrator(mockWebAPIFeatures);
        Session mockSession = new Session(mock(Channel.class), webAPIOrchestrator);
        HashMap<String, Object> queryInfo = new HashMap<>();
        queryInfo.put("RECEIVER_PDX_COMMANDS", new LinkedList<PdxCommand>());
        queryInfo.put("command", "NDSP_ASR_APP_CMD");
        queryInfo.put("REQUEST_INFO", "");
        webAPIOrchestrator.queryInfo = queryInfo;
        Executor executor = webAPIOrchestrator.buildCommand(mockSession);
        assertEquals(InterpretationWithSpeechRecognitionExecutor.class, executor.getClass());
    }

    @Test
    public void test_buildCommand_InterpretationExecutor() throws IOException {
        IWebApiFeatures mockWebAPIFeatures = mock(IWebApiFeatures.class);
        when(mockWebAPIFeatures.getWebApiEndpoint(any())).thenReturn(null);
        WebAPIOrchestrator webAPIOrchestrator = new WebAPIOrchestrator(mockWebAPIFeatures);
        Session mockSession = new Session(mock(Channel.class), webAPIOrchestrator);
        HashMap<String, Object> queryInfo = new HashMap<>();
        queryInfo.put("RECEIVER_PDX_COMMANDS", new LinkedList<PdxCommand>());
        queryInfo.put("command", "NDSP_APP_CMD");
        queryInfo.put("REQUEST_INFO", "");
        webAPIOrchestrator.queryInfo = queryInfo;
        Executor executor = webAPIOrchestrator.buildCommand(mockSession);
        assertEquals(InterpretationExecutor.class, executor.getClass());
    }

    @Test
    public void test_buildCommand_invalidCommand() throws IOException {
        IWebApiFeatures mockWebAPIFeatures = mock(IWebApiFeatures.class);
        when(mockWebAPIFeatures.getWebApiEndpoint(any())).thenReturn(null);
        WebAPIOrchestrator webAPIOrchestrator = new WebAPIOrchestrator(mockWebAPIFeatures);
        Session mockSession = new Session(mock(Channel.class), webAPIOrchestrator);
        HashMap<String, Object> queryInfo = new HashMap<>();
        queryInfo.put("RECEIVER_PDX_COMMANDS", new LinkedList<PdxCommand>());
        queryInfo.put("command", "invalid");
        queryInfo.put("REQUEST_INFO", "");
        webAPIOrchestrator.queryInfo = queryInfo;

        WebAPIOrchestrator webAPIOrchestratorSpy = spy(webAPIOrchestrator);
        doNothing().when(webAPIOrchestratorSpy).sendError(any(), any());
        Executor executor = webAPIOrchestratorSpy.buildCommand(mockSession);
        assertNull(executor);
    }
}
