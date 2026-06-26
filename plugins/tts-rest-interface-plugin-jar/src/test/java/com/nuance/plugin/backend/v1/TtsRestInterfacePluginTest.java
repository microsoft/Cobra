package com.nuance.plugin.backend.v1;

import com.nuance.plugin.backend.v1.json.RequestJson;
import com.nuance.plugin.v1.WebApiCommonException;
import com.nuance.plugin.v1.shareable.*;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

import static com.nuance.cobra.utils.errors.RestPluginsError.UNKNOWN_ACTION;
import static com.nuance.cobra.utils.errors.RestPluginsError.UNSUPPORTED_URI;
import static com.nuance.plugin.backend.v1.TtsRestConfig.Property.TTS_REST_ENCAPSULATE_SIMPLE_TEXT;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TtsRestInterfacePluginTest {
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);
    private CombinedConfiguration testConf = mock(CombinedConfiguration.class);
    private HttpSession mockHttpSession = mock(HttpSession.class);

    @Before
    public void initTest() {
        when(request.getSession()).thenReturn(mockHttpSession);
        when(request.getQueryString()).thenReturn("text=test%20me%20something&voice=bla");
    }

    @Test
    public void getUrlPattern() {
        TtsRestInterfacePlugin ttsRestInterfacePlugin = new TtsRestInterfacePlugin();
        assertEquals("rest/text-to-speech/v1/.*", ttsRestInterfacePlugin.getUrlPattern());
    }

    @Test
    public void onHttpGetGenerateException() {
        when(request.getSession()).thenReturn(null); //trigger null pointer exception
        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/v1/text:synthesize?text=test%20me%20something&voice=bla");
        when(request.getHeader("Authorization")).thenReturn("xyz:xyz");

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });

        boolean caughtPluginException = false;
        try {
            httpInterfacePlugin.onHttpGet(request, response);
        } catch (WebApiCommonException e) {
            assertEquals(UNSUPPORTED_URI.getHttpStatusCode(), e.getHttpStatusCode());
            caughtPluginException = true;
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertTrue(caughtPluginException);
    }

    @Test
    public void onHttpGetSynthesize() throws IOException {
        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/text-to-speech/v1/synthesize?text=test%20me%20something&voice=bla");
        when(request.getHeader("Authorization")).thenReturn("xyz:xyz");

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });
        boolean caughtPluginException = false;
        try {
            httpInterfacePlugin.onHttpGet(request, response);
        } catch (WebApiCommonException e) {
            assertEquals(UNKNOWN_ACTION.getHttpStatusCode(), e.getHttpStatusCode());
            caughtPluginException = true;
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertTrue(caughtPluginException);
    }

    @Test
    public void onHttpGetVoices() {
        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/text-to-speech/v1/voices");

        HierarchicalConfiguration vocsConf = new BaseHierarchicalConfiguration();
        vocsConf.addProperty("Ava.name", "Ava");
        vocsConf.addProperty("Ava.pcm_16_16k.voice_model", "xpremium-high");
        when(testConf.configurationAt("vocsMapping")).thenReturn(vocsConf);

        IWebApiEndpoint apiEndpoint = mock(IWebApiEndpoint.class);
        ISession activeSession = mock(ISession.class);
        when(apiEndpoint.getActiveSession()).thenReturn(activeSession);
        when(activeSession.getCommand()).thenReturn(null);
        when(activeSession.getSessionConfig()).thenReturn(testConf);

        StringWriter result = new StringWriter();

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return apiEndpoint;
            }
        });
        try {
            assertTrue(httpInterfacePlugin.onHttpGet(request, response));
        } catch (IOException | WebApiCommonException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void onHttpGetDocs() throws IOException {
        PrintWriter writer = new PrintWriter(TtsRestInterfacePlugin.class.getResource("/").getPath() + "tts_api.yaml", "UTF-8");
        writer.println("testFile");
        writer.close();

        OutputStream testOut = new ByteArrayOutputStream();

        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/text-to-speech/v1/docs");

        StringWriter result = new StringWriter();

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });

        ServletOutputStream os = new ServletOutputStream() {
            private StringBuilder data = new StringBuilder();

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                data.append((char) b);
            }

            public String toString() {
                return data.toString();
            }
        };

        try {
            when(response.getWriter()).thenReturn(new PrintWriter(new BufferedWriter(result)));
            when(response.getOutputStream()).thenReturn(os);
            httpInterfacePlugin.onHttpGet(request, response);
        } catch (IOException | WebApiCommonException e) {
            fail(e.getMessage());
        }

        assertEquals("testFile", os.toString().trim());
    }

    @Test
    public void onHttpGetBadAction() throws IOException {
        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/v1/text:voices");

        HierarchicalConfiguration vocsConf = new BaseHierarchicalConfiguration();
        vocsConf.addProperty("Ava.name", "Ava");
        vocsConf.addProperty("Ava.pcm_16_16k.voice_model", "xpremium-high");
        when(testConf.configurationAt("vocsMapping")).thenReturn(vocsConf);

        StringWriter result = new StringWriter();

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });
        when(response.getWriter()).thenReturn(new PrintWriter(new BufferedWriter(result)));

        boolean exceptionCaught = false;
        try {
            httpInterfacePlugin.onHttpGet(request, response);
        } catch (WebApiCommonException e) {
            assertEquals(UNSUPPORTED_URI.getHttpStatusCode(), e.getHttpStatusCode());
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        exceptionCaught = false;
        //test default path
        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/text-to-speech/v1/noCommand");
        try {
            httpInterfacePlugin.onHttpGet(request, response);
        } catch (WebApiCommonException e) {
            assertEquals(UNKNOWN_ACTION.getHttpStatusCode(), e.getHttpStatusCode());
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
    }

    @Test
    public void badURL() throws IOException {
        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/v1/badbadbadbad:synthesize?text=test%20me%20something&voice=bla");
        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });
        boolean exceptionCaught = false;
        try {
            httpInterfacePlugin.onHttpGet(request, response);
        } catch (WebApiCommonException e) {
            assertEquals(UNSUPPORTED_URI.getHttpStatusCode(), e.getHttpStatusCode());
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
    }

    @Test
    public void onHttpPost() throws IOException, WebApiCommonException {
        String requestJson = "{\n" +
                "  \"generalConfig\": {\n" +
                "    \"sensitivity\": \"masked\",\n" +
                "    \"statistics\": false\n" +
                "  },\n" +
                "  \"input\": {\n" +
                "    \"text\": \"string\"\n" +
                "  },\n" +
                "  \"voice\": {\n" +
                "    \"name\": \"string\",\n" +
                "    \"gender\": \"Female\",\n" +
                "    \"locale\": \"string\"\n" +
                "  },\n" +
                "  \"audioConfig\": {\n" +
                "    \"audioEncoding\": \"audio/L16;rate=16000\"\n" +
                "  },\n" +
                "  \"clientMetaDataConfig\": {\n" +
                "    \"clientUserId\": \"string\",\n" +
                "    \"clientDeviceId\": \"string\",\n" +
                "    \"clientSessionId\": \"string\",\n" +
                "    \"clientOsName\": \"string\",\n" +
                "    \"clientGUID\": \"string\",\n" +
                "    \"clientOversion\": \"string\"\n" +
                "  }\n" +
                "}";

        when(request.getRequestURI()).thenReturn("/webapi-platform/http/companyName_appName/rest/text-to-speech/v1/synthesize/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestJson)));

        final byte[] myBytes = requestJson.getBytes("UTF-8");
        ServletInputStream servletInputStream = new ServletInputStream() {
            private int lastIndexRetrieved = -1;
            private ReadListener readListener = null;

            @Override
            public boolean isFinished() {
                return (lastIndexRetrieved == myBytes.length - 1);
            }

            @Override
            public boolean isReady() {
                // This implementation will never block
                // We also never need to call the readListener from this method, as this method will never return false
                return isFinished();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                this.readListener = readListener;
                if (!isFinished()) {
                    try {
                        readListener.onDataAvailable();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                } else {
                    try {
                        readListener.onAllDataRead();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                }
            }

            @Override
            public int read() throws IOException {
                int i;
                if (!isFinished()) {
                    i = myBytes[lastIndexRetrieved + 1];
                    lastIndexRetrieved++;
                    if (isFinished() && (readListener != null)) {
                        try {
                            readListener.onAllDataRead();
                        } catch (IOException ex) {
                            readListener.onError(ex);
                            throw ex;
                        }
                    }
                    return i;
                } else {
                    return -1;
                }
            }
        };
        when(request.getInputStream()).thenReturn(servletInputStream);

        IWebApiEndpoint apiEndpoint = mock(IWebApiEndpoint.class);
        ISession activeSession = mock(ISession.class);
        when(apiEndpoint.getActiveSession()).thenReturn(activeSession);
        when(activeSession.getCommand()).thenReturn(null);
        when(activeSession.getSessionConfig()).thenReturn(testConf);

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return apiEndpoint;
            }
        });

        httpInterfacePlugin.onHttpPost(request, response);

        when(request.getRequestURI()).thenReturn("");
        assertEquals("", request.getRequestURI());
        try {
            assertTrue(httpInterfacePlugin.onHttpPost(request, response));
        } catch (WebApiCommonException e) {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getHttpStatusCode());
            assertEquals(UNKNOWN_ACTION.getValue(), e.getErrorCodeValue());
        }

    }

    @Test
    public void testGetTTSText() {
        HierarchicalConfiguration vocsConf = new BaseHierarchicalConfiguration();
        vocsConf.addProperty("Ava.name", "Ava");
        vocsConf.addProperty("Ava.lang", "en-us");
        when(testConf.configurationAt("vocsMapping")).thenReturn(vocsConf);

        IWebApiEndpoint apiEndpoint = mock(IWebApiEndpoint.class);
        ISession activeSession = mock(ISession.class);
        when(apiEndpoint.getActiveSession()).thenReturn(activeSession);
        when(activeSession.getCommand()).thenReturn(null);
        when(activeSession.getSessionConfig()).thenReturn(testConf);

        StringWriter result = new StringWriter();

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        RequestJson request = new RequestJson();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return apiEndpoint;
            }
        });

        request.input.put("text", "basic text");
        assertEquals("<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"https://www.w3.org/2001/mstts\" xml:lang=\"null\">basic text</speak>", httpInterfacePlugin.getTTSText(true, request));

        request.input.put("text", "basic text");
        assertEquals("basic text", httpInterfacePlugin.getTTSText(false, request));

        request.input.put("text", "<speak>ssml text</speak>");
        assertEquals("<speak>ssml text</speak>", httpInterfacePlugin.getTTSText(true, request));
    }

    @Test
    public void testGetTTSTextException() {
        HierarchicalConfiguration vocsConf = new BaseHierarchicalConfiguration();
        vocsConf.addProperty("Ava.name", "Ava");
        vocsConf.addProperty("Ava.lang", "en-us");
        when(testConf.configurationAt("vocsMapping")).thenReturn(vocsConf);

        IWebApiEndpoint apiEndpoint = mock(IWebApiEndpoint.class);
        ISession activeSession = mock(ISession.class);
        when(apiEndpoint.getActiveSession()).thenReturn(activeSession);
        when(activeSession.getCommand()).thenReturn(null);
        when(activeSession.getSessionConfig()).thenReturn(testConf);

        StringWriter result = new StringWriter();

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        RequestJson request = new RequestJson();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                throw new IOException("error");
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return apiEndpoint;
            }
        });

        request.input.put("text", "basic text");
        assertEquals("basic text", httpInterfacePlugin.getTTSText(true, request));
    }

    @Test
    public void testPutCommandParameters() {
        HierarchicalConfiguration vocsConf = new BaseHierarchicalConfiguration();
        vocsConf.addProperty("Ava.name", "Ava");
        vocsConf.addProperty("Ava.lang", "en-us");
        when(testConf.configurationAt("vocsMapping")).thenReturn(vocsConf);
        when(testConf.getBoolean(TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getKey(), (Boolean)TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getDefaultValue())).thenReturn(true);

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });

        JSONObject command = new JSONObject();
        JSONObject parameters = new JSONObject();
        RequestJson queryParam = new RequestJson();
        queryParam.input.put("text", "ssml text");
        TtsRestInterfacePlugin.Actions action = TtsRestInterfacePlugin.Actions.synthesize;
        httpInterfacePlugin.putCommandParameters(command, parameters, queryParam, action);

        assertEquals(new JSONObject("{\"name\":\"speechSynthesis\",\"id\":\"tts\",\"parameters\":{\"data\":\"<speak version=\\\"1.0\\\" xmlns=\\\"http://www.w3.org/2001/10/synthesis\\\" xmlns:mstts=\\\"https://www.w3.org/2001/mstts\\\" xml:lang=\\\"null\\\">ssml text<\\/speak>\",\"type\":\"ssml\"}}").toString(), command.toString());
    }

    @Test
    public void testPutCommandParametersNTTS() {
        HierarchicalConfiguration voiceConf = new BaseHierarchicalConfiguration();
        BaseHierarchicalConfiguration vocsConf = mock(BaseHierarchicalConfiguration.class);
        voiceConf.addProperty("name", "Ava");
        voiceConf.addProperty("language", "en-us");
        voiceConf.addProperty("nvcVersion", "nvc3");
        when(vocsConf.configurationAt("Ava")).thenReturn(voiceConf);
        when(testConf.configurationAt("vocsMapping")).thenReturn(vocsConf);
        when(testConf.getBoolean(TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getKey(), (Boolean)TTS_REST_ENCAPSULATE_SIMPLE_TEXT.getDefaultValue())).thenReturn(true);

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return testConf;
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });

        JSONObject command = new JSONObject();
        JSONObject parameters = new JSONObject();
        RequestJson queryParam = new RequestJson();
        queryParam.input.put("text", "ssml text");
        queryParam.voice.put("voice", "Ava");
        TtsRestInterfacePlugin.Actions action = TtsRestInterfacePlugin.Actions.synthesize;
        httpInterfacePlugin.putCommandParameters(command, parameters, queryParam, action);

        assertEquals(new JSONObject("{\"name\":\"speechSynthesis\",\"id\":\"tts\",\"parameters\":{\"voice\":\"Ava\",\"data\":\"<lang xml:lang=\\\"en-us\\\">ssml text<\\/lang>\",\"type\":\"ssml\"}}").toString(), command.toString());
    }

    @Test
    public void testPutCommandParametersException() {
        HierarchicalConfiguration vocsConf = new BaseHierarchicalConfiguration();
        vocsConf.addProperty("Ava.name", "Ava");
        vocsConf.addProperty("Ava.lang", "en-us");
        when(testConf.configurationAt("vocsMapping")).thenReturn(vocsConf);

        TtsRestInterfacePlugin httpInterfacePlugin = new TtsRestInterfacePlugin();
        httpInterfacePlugin.onInitPlugin(new IWebApiFeatures() {
            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                throw new IOException("error");
            }

            @Override
            public CombinedConfiguration getConfigs(String tenantName) throws IOException {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return null;
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) {
                return null;
            }
        });

        JSONObject command = new JSONObject();
        JSONObject parameters = new JSONObject();
        RequestJson queryParam = new RequestJson();
        queryParam.input.put("text", "<speak>ssml text</speak>");
        TtsRestInterfacePlugin.Actions action = TtsRestInterfacePlugin.Actions.synthesize;
        httpInterfacePlugin.putCommandParameters(command, parameters, queryParam, action);

        assertEquals(new JSONObject("{\"name\":\"speechSynthesis\",\"id\":\"tts\",\"parameters\":{\"data\":\"<speak>ssml text<\\/speak>\",\"type\":\"text\"}}").toString(), command.toString());
    }
}