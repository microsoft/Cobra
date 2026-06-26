package com.nuance.cobra.client;


import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.client.azure.AzureASRClient;
import com.nuance.cobra.client.azure.AzureTokenProvider;
import com.nuance.cobra.client.generic.SocketConnectHealthCheckClient;
import com.nuance.cobra.client.krypton.KryptonUtils;
import com.nuance.cobra.client.mix.AuthorizationCredentials;
import com.nuance.cobra.client.mix.MixClientCredentialsManager;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.forgetme.ForgetMeGrpcClient;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.mix.grpc.InterceptedManagedChannel;
import com.nuance.cobra.client.mix.grpc.ManagedChannelManager;
import com.nuance.cobra.client.mix.krypton.KryptonGrpcClient;
import com.nuance.cobra.client.mix.krypton.KryptonGrpcRequestParams;
import com.nuance.cobra.client.mix.nlu.NluAasGrpcClient;
import com.nuance.cobra.client.mix.nlu.NluAasGrpcParams;
import com.nuance.cobra.client.mix.nvc.NvcClient;
import com.nuance.cobra.client.mix.nvc.NvcRequestParams;
import com.nuance.cobra.client.nim.MrecNimClient;
import com.nuance.cobra.client.nim.SessionNiMClient;
import com.nuance.cobra.client.nim.VocalizerNimClient;
import com.nuance.cobra.client.nlucachedb.NluCacheDbClient;
import com.nuance.cobra.client.response.InterpretationResponse;
import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.client.response.SpeechSynthesisResponse;
import com.nuance.cobra.client.stub.SessionStubClient;
import com.nuance.cobra.client.stub.SpeechRecoStubClient;
import com.nuance.cobra.client.stub.SpeechSynthesisStubClient;
import com.nuance.cobra.session.*;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.HealthCheckException;
import com.nuance.cobra.utils.UrlUtils;
import com.nuance.cobra.utils.audio.NoiseLevel;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.ResponseHandlerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import com.nuance.rpc.asr.v1.RecognizerGrpc;
import com.nuance.rpc.nlu.v1.RuntimeGrpc;
import com.nuance.rpc.tts.v1.SynthesizerGrpc;
import io.grpc.stub.AbstractAsyncStub;
import io.grpc.stub.AbstractStub;
import io.prometheus.client.Counter;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.*;

/**
 * This factory is the one that creates the appropriate client
 * based on the command's need. It will return the client.
 */

@java.lang.SuppressWarnings("java:S3740")
public class ClientFactory
{
    private static final Logger logger = LogManager.getLogger(ClientFactory.class);

    /**
     * Counter for number of TTS requests and voices used
     */
    private static final Counter webapiTtsEngineCounter = Counter.build()
                                                               .name("webapi_tts_requests_total")
                                                               .help("Number of TTS requests and voices used")
                                                               .labelNames(MetricsConstants.ENGINE_LABEL, MetricsConstants.VOICE_LABEL, MetricsConstants.TENANT_LABEL)
                                                               .register();
    /**
     * Counter for number of speech recognition engines and languages used
     */
    private static final Counter webapiSpeechRecoEngineCounter = Counter.build()
                                                                      .name("webapi_asr_requests_total")
                                                                      .help("Number of speech recognition requests and languages used")
                                                                      .labelNames(MetricsConstants.ENGINE_LABEL, MetricsConstants.DATAPACK_LABEL, MetricsConstants.TENANT_LABEL)
                                                                      .register();
    /**
     * Counter for number of NLU request sent to the NLU backend.
     */
    private static final Counter webapiNluEngineCounter = Counter.build()
            .name("webapi_nlu_requests_total")
            .help("Number of requests sent to NLU engine.")
            .labelNames(MetricsConstants.ENGINE_LABEL, MetricsConstants.LANGUAGE_LABEL)
            .register();
    public static final String SESSION_WILL_USE_A_STUB_BACKEND = "Session {} will use a stub backend.";


    /**
     * Creates a session Client
     *
     * @param sessionId
     *         The session identifier that is bound to the client
     *
     * @return The client to be used
     *
     * @see SessionClient
     */
    public SessionClient createSessionClient(CombinedConfiguration params, String sessionId)
    {
        boolean nimNcsSessionRequired = params.getBoolean(WebApiConfig.Property.NIM_NCS_SESSION_REQUIRED.key, false);

        //Use a stub backend?
        if (Session.isSessionUsingStubBackend(sessionId) || !nimNcsSessionRequired)
        {
            logger.debug(SESSION_WILL_USE_A_STUB_BACKEND, sessionId);
            return new SessionStubClient(sessionId);
        }

        return new SessionNiMClient(sessionId);
    }

    /**
     * Creates a Configuration Client
     *
     * @param responseHandler
     *         The callback object to return the responses
     *
     * @return The client to be used
     *
     * @see SessionClient
     */
    public SpeechSynthesisClient createSpeechSynthesisClient(CombinedConfiguration params, String sessionId,
            ResponseHandler responseHandler)
    {
        String tenant = null;

        if (sessionId!=null)
        {
            //Use a stub backend?
            if (Session.isSessionUsingStubBackend(sessionId))
            {
                logger.debug(SESSION_WILL_USE_A_STUB_BACKEND, sessionId);
                return new SpeechSynthesisStubClient(sessionId, responseHandler);
            }

            // retrieve tenant for counters
            final SessionManager sessionManager = SessionManager.getInstance();
            Session session = sessionManager.getSession(sessionId, false);
            if (session != null) {
                tenant = session.getTenantKey();
            }
        }

        VocalizerClients client;
        try
        {
            client = VocalizerClients.valueOf(params.getString(WebApiConfig.Property.VOCALIZER.key)
                                                    .toLowerCase());
        } catch (Exception e)
        {
            client = VocalizerClients.nim;
        }
        String voice = params.getString(WebApiConfig.Property.SPEECH_SYNTHESIS_VOICE.key);

        switch (client)
        {
            case nvc:
                incrementWebapiTTSEngineCount("NVC", voice, tenant);

                NvcRequestParams nvcParams = new NvcRequestParams(params);
                String nvcVersion = nvcParams.getVoiceNvcVersion();

                ResponseErrorHandler errorHandler = new ResponseErrorHandler();
                errorHandler.addListener(new ResponseHandlerErrorListener(responseHandler));
                errorHandler.addListener(new LoggerErrorListener(LogManager.getLogger(NvcClient.class)));
                errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
                errorHandler.addListener(new HttpErrorListener());

                HeaderEventListener interceptor = new HeaderEventListener();
                SynthesizerGrpc.SynthesizerStub grpcSynthStub = buildNvcStub(nvcParams, params, errorHandler, interceptor, getCustomGrpcHeaders(sessionId, nvcVersion));
                if(grpcSynthStub == null) return null;

                return new NvcClient(sessionId, nvcParams, grpcSynthStub, responseHandler, errorHandler, interceptor);

            case nim:
            default:
                incrementWebapiTTSEngineCount("NiM", voice, tenant);
                return new VocalizerNimClient(sessionId, params, responseHandler);
        }

    }

    // TODO find out whether the gRPC channel should be shut down gracefully when Cobra is shut down
    private SynthesizerGrpc.SynthesizerStub buildNvcStub(NvcRequestParams params, CombinedConfiguration cobraConfiguration, ResponseErrorHandler errorHandler, HeaderEventListener interceptor, Map<String, String> customHeaders)
    {
        URL url = UrlUtils.getServerUrl(params.getUrl());
        InterceptedManagedChannel channel = ManagedChannelManager.getManagedChannel(UrlUtils.getServerHost(url), UrlUtils.getServerPort(url), UrlUtils.isPlainTextProtocol(url), cobraConfiguration, customHeaders);

        channel.getInterceptor().setListener(interceptor);

        SynthesizerGrpc.SynthesizerStub synthesizerStub = SynthesizerGrpc.newStub(channel.getChannel());

        if (cobraConfiguration.getString(WebApiConfig.Property.NVC_GRPC_AUTH_METHOD.key).equals(NvcClient.AUTH_MIX_BEARER_TOKEN))
        {
            String mixToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(cobraConfiguration));
            if (mixToken == null || mixToken.isBlank())
            {
                errorHandler.handleError(new SpeechSynthesisResponse(true, ResponseCode.createResponseError(ExternalResponseErrorCode.MIX_AUTH_ERROR)));
                return null;
            }
            AuthorizationCredentials bearerToken = new AuthorizationCredentials(mixToken, AuthorizationCredentials.AUTH_TYPE_BEARER);
            synthesizerStub = synthesizerStub.withCallCredentials(bearerToken);
        } else if(cobraConfiguration.getString(WebApiConfig.Property.NVC_GRPC_AUTH_METHOD.key).equals(NvcClient.AUTH_PASSTHROUGH))
        {
            synthesizerStub = (SynthesizerGrpc.SynthesizerStub) addPassthroughAuthorization(synthesizerStub, cobraConfiguration);
        } else {
            logAuthMethod(WebApiConfig.Property.NVC_GRPC_AUTH_METHOD.key, cobraConfiguration.getString(WebApiConfig.Property.NVC_GRPC_AUTH_METHOD.key));
        }

        return synthesizerStub;
    }

    private void logAuthMethod(String authMethodKey, String authMethod) {
        logger.trace("{} = {}", authMethodKey, authMethod);
    }

    AbstractStub addPassthroughAuthorization(AbstractAsyncStub abstractStub, CombinedConfiguration cobraConfiguration)
    {
        AuthTypeAndData authTypeAndData = new AuthTypeAndData(cobraConfiguration);

        if (authTypeAndData.getAuthType() == AuthTypeAndData.ValidationTypes.hashcode)
        {
            String username = cobraConfiguration.getString(WebApiConfig.Property.APPLICATION_KEY.key);
            String basicAuthString = username + ":" + authTypeAndData.getAuthData();
            String basicAuthBase64 = Base64.getEncoder().encodeToString(basicAuthString.getBytes());
            AuthorizationCredentials basicAuth = new AuthorizationCredentials(basicAuthBase64, AuthorizationCredentials.AUTH_TYPE_BASIC);
            return abstractStub.withCallCredentials(basicAuth);
        } else if (authTypeAndData.getAuthType() == AuthTypeAndData.ValidationTypes.jws)
        {
            AuthorizationCredentials bearerToken = new AuthorizationCredentials(authTypeAndData.getAuthData(), AuthorizationCredentials.AUTH_TYPE_BEARER);
            return abstractStub.withCallCredentials(bearerToken);
        } else {
            logger.trace("passthrough with {}", authTypeAndData.getAuthType());
        }

        return abstractStub;
    }

    /**
     * Creates a Speech Recognition Client
     *
     * @param config
     *         The configuration properties for the command.
     * @param vocabularies
     *         The array of dynamic vocabulary to activate.
     * @param responseHandler
     *         The callback object to return the responses
     *
     * @return The client to be used
     *
     * @see SpeechRecoClient
     * @see ResponseHandler
     */
    public SpeechRecoClient createSpeechRecoClient(CombinedConfiguration config, Session session, List<DynamicVocabulary> vocabularies, ResponseHandler responseHandler)
            throws CobraException
    {
        String sessionId = session.getSessionId();
        //Use a stub backend?
        if (Session.isSessionUsingStubBackend(sessionId))
        {
            logger.debug(SESSION_WILL_USE_A_STUB_BACKEND, sessionId);
            return new SpeechRecoStubClient(sessionId, responseHandler);
        }

        String speechRecoEngine = config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key);
        SpeechRecognitionClients client;

        try
        {
            client = SpeechRecognitionClients.fromString(config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_ENGINE.key));
        } catch (Exception e)
        {
            logger.error("Couldn't instantiate a SpeechRecognitionClient for: {}", speechRecoEngine);
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.UNKNOWN_SPEECH_RECO_SERVICE, speechRecoEngine));
        }

        switch(client)
        {
            case SPEECH_SERVICE_ASR:
                incrementWebapiSpeechRecoEngineCount("MicrosoftAzureAI",
                        config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key), session.getTenantKey());
                String clientId = config.getString(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key, (String) WebApiConfig.Property.KEY_VAULT_CLIENT_ID.defaultValue);
                logger.info("The client id is {}", clientId);
                AzureTokenProvider tokenProvider = new AzureTokenProvider(clientId);
                return new AzureASRClient(sessionId, responseHandler, config, vocabularies, tokenProvider);

            case MREC_NIM:
                incrementWebapiSpeechRecoEngineCount("MrecNim",
                        config.getString(WebApiConfig.Property.SPEECH_RECOGNITION_LANGUAGE.key), session.getTenantKey());
                return new MrecNimClient(config, session, vocabularies, responseHandler);

            case KRYPTON_GRPC:
                KryptonGrpcRequestParams kryptonGrpcParams = new KryptonGrpcRequestParams(config, vocabularies, LogManager.getLogger(KryptonGrpcRequestParams.class));

                ResponseErrorHandler errorHandler = new ResponseErrorHandler();
                errorHandler.addListener(new ResponseHandlerErrorListener(responseHandler));
                errorHandler.addListener(new LoggerErrorListener(LogManager.getLogger(KryptonGrpcClient.class)));
                errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
                errorHandler.addListener(new HttpErrorListener());

                HeaderEventListener interceptor = new HeaderEventListener();
                RecognizerGrpc.RecognizerStub
                        grpcRecognizerStub = buildKryptonGrpcStub(kryptonGrpcParams, config, errorHandler, interceptor, getCustomGrpcHeaders(sessionId));
                if(grpcRecognizerStub == null) return null;

                KryptonGrpcClient kryptonGrpcClient = new KryptonGrpcClient(config, sessionId, kryptonGrpcParams, grpcRecognizerStub, responseHandler, errorHandler, interceptor);

                incrementWebapiSpeechRecoEngineCount("Krypton-gRPC", KryptonUtils.getLanguageForMetrics(config), session.getTenantKey());

                return kryptonGrpcClient;

            default:
                logger.error("Couldn't instantiate a SpeechRecognitionClient for: {}", speechRecoEngine);
                throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.UNKNOWN_SPEECH_RECO_SERVICE, speechRecoEngine));
        }
    }

    // TODO it is OK to create a new gRPC stub for each request, but the gRPC channel should be reused
    // TODO find out whether the gRPC channel should be shut down gracefully when Cobra is shut down
    private RecognizerGrpc.RecognizerStub buildKryptonGrpcStub(KryptonGrpcRequestParams params, CombinedConfiguration cobraConfiguration, ResponseErrorHandler errorHandler, HeaderEventListener interceptor, Map<String, String> customHeaders)
    {
        URL url = UrlUtils.getServerUrl(params.getUrl());
        InterceptedManagedChannel channel = ManagedChannelManager.getManagedChannel(UrlUtils.getServerHost(url), UrlUtils.getServerPort(url), UrlUtils.isPlainTextProtocol(url), cobraConfiguration, customHeaders);

        channel.getInterceptor().setListener(interceptor);

        RecognizerGrpc.RecognizerStub recognizerStub = RecognizerGrpc.newStub(channel.getChannel());

        if (cobraConfiguration.getString(WebApiConfig.Property.KRYPTON_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN))
        {
            String mixToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(cobraConfiguration));
            if (mixToken == null || mixToken.isBlank())
            {
                errorHandler.handleError(new SpeechRecoResponse(true, new NoiseLevel(0,0), ResponseCode.createResponseError(ExternalResponseErrorCode.MIX_AUTH_ERROR)));
                return null;
            }
            AuthorizationCredentials bearerToken = new AuthorizationCredentials(mixToken, AuthorizationCredentials.AUTH_TYPE_BEARER);
            recognizerStub = recognizerStub.withCallCredentials(bearerToken);
        } else if(cobraConfiguration.getString(WebApiConfig.Property.KRYPTON_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_PASSTHROUGH))
        {
            recognizerStub = (RecognizerGrpc.RecognizerStub) addPassthroughAuthorization(recognizerStub, cobraConfiguration);
        } else {
            logAuthMethod(WebApiConfig.Property.KRYPTON_GRPC_AUTH_METHOD.key, cobraConfiguration.getString(WebApiConfig.Property.KRYPTON_GRPC_AUTH_METHOD.key));
        }

        return recognizerStub;
    }

    private void incrementWebapiSpeechRecoEngineCount(String speechRecoEngine, String datapack, String tenant)
    {
        webapiSpeechRecoEngineCounter.labels(speechRecoEngine,
                datapack != null ? datapack : MetricsConstants.UNDEFINED_LABEL_VALUE,
                MetricsConstants.nonNullLabelValue(tenant)).inc();
    }

    private void incrementWebapiTTSEngineCount(String ttsEngine, String voice, String tenant)
    {
        webapiTtsEngineCounter.labels(ttsEngine,
                voice != null ? voice : MetricsConstants.UNDEFINED_LABEL_VALUE,
                MetricsConstants.nonNullLabelValue(tenant)).inc();
    }


    /**
     * Creates an interpretation Client
     *
     * @param params
     *         The configuration properties for the command.
     * @param sessionId
     *         The session identifier that is bound to the client
     * @param vocabularies
     *         The array of dynamic vocabulary to activate.
     * @param responseHandler
     *         The callback object to return the responses
     *
     *
     * @return The client to be used
     *
     * @see InterpretationClient
     */
    public InterpretationClient createInterpretationClient(CombinedConfiguration params, String sessionId, List<DynamicVocabulary> vocabularies, ResponseHandler responseHandler)
    {
        String interpretationBackend = params.getString(WebApiConfig.Property.INTERPRETATION_BACKEND.key);
        String interpretationLanguage = null;

        if (interpretationBackend.equalsIgnoreCase(InterpretationClients.NLU_GRPC.getValue()))
        {
            interpretationLanguage = params.getString(WebApiConfig.Property.INTERPRETATION_LANGUAGE.key,
                    (String) WebApiConfig.Property.INTERPRETATION_LANGUAGE.defaultValue);
        }

        incrementWebapiNLUEngineCount(interpretationBackend, interpretationLanguage);

        if (interpretationBackend.equalsIgnoreCase(InterpretationClients.NLU_GRPC.getValue()))
        {
            ResponseErrorHandler errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new ResponseHandlerErrorListener(responseHandler));
            errorHandler.addListener(new LoggerErrorListener(LogManager.getLogger(NluAasGrpcClient.class)));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());

            NluAasGrpcParams nluAasGrpcParams = new NluAasGrpcParams(sessionId, params, vocabularies);

            HeaderEventListener interceptor = new HeaderEventListener();
            RuntimeGrpc.RuntimeStub nluAasGrpcStub = buildNluAasGrpcStub(nluAasGrpcParams, errorHandler, params, interceptor, getCustomGrpcHeaders(sessionId));
            if (nluAasGrpcStub == null) {
                return null;
            }
            return new NluAasGrpcClient(nluAasGrpcParams, nluAasGrpcStub, responseHandler, errorHandler, interceptor);
        } else
        {
            return new NluCacheDbClient(params, sessionId, responseHandler);
        }
    }


    private void incrementWebapiNLUEngineCount(String nluEngine, @Nullable String language)
    {
        webapiNluEngineCounter.labels(nluEngine, MetricsConstants.nonNullLabelValue(language)).inc();
    }

    private RuntimeGrpc.RuntimeStub buildNluAasGrpcStub(NluAasGrpcParams params, ResponseErrorHandler errorHandler, CombinedConfiguration sessionConfigs, HeaderEventListener interceptor, Map<String, String> customHeaders)
    {
        URL url = UrlUtils.getServerUrl(params.getUrl());
        InterceptedManagedChannel channel = ManagedChannelManager.getManagedChannel(UrlUtils.getServerHost(url), UrlUtils.getServerPort(url), UrlUtils.isPlainTextProtocol(url), sessionConfigs, customHeaders);
        channel.getInterceptor().setListener(interceptor);

        RuntimeGrpc.RuntimeStub grpcStub = RuntimeGrpc.newStub(channel.getChannel());

        CombinedConfiguration cobraConfiguration = params.getCobraConfig();

        if (cobraConfiguration.getString(WebApiConfig.Property.NLUAAS_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN))
        {
            String mixToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(cobraConfiguration));
            if (mixToken == null || mixToken.isBlank())
            {
                errorHandler.handleError(new InterpretationResponse(true, ResponseCode.createResponseError(ExternalResponseErrorCode.MIX_AUTH_ERROR)));
                return null;
            }
            AuthorizationCredentials bearerToken = new AuthorizationCredentials(mixToken, AuthorizationCredentials.AUTH_TYPE_BEARER);
            grpcStub = grpcStub.withCallCredentials(bearerToken);
        } else if(cobraConfiguration.getString(WebApiConfig.Property.NLUAAS_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_PASSTHROUGH))
        {
            grpcStub = (RuntimeGrpc.RuntimeStub) addPassthroughAuthorization(grpcStub, cobraConfiguration);
        } else {
            logAuthMethod(WebApiConfig.Property.NLUAAS_GRPC_AUTH_METHOD.key, cobraConfiguration.getString(WebApiConfig.Property.NLUAAS_GRPC_AUTH_METHOD.key));
        }

        return grpcStub;
    }

    /**
     * Creates an userReset Client
     *
     * @param params
     *         The configuration properties for the command.
     * @param sessionId
     *         The session identifier that is bound to the client
     * @param responseHandler
     *         The callback object to return the responses
     *
     *
     * @return The client to be used
     *
     * @see UserResetClient
     */
    public UserResetClient createUserResetClient(CombinedConfiguration params, String sessionId, ResponseHandler responseHandler)
    {
        String userResetBackend = params.getString(WebApiConfig.Property.USER_RESET_BACKEND.key);

        if (userResetBackend == null) {
            logger.info("No userReset client set up");
        }
        else if (userResetBackend.equalsIgnoreCase(UserResetClients.MIX.getValue()))
        {
            ResponseErrorHandler errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new ResponseHandlerErrorListener(responseHandler));
            errorHandler.addListener(new LoggerErrorListener(LogManager.getLogger(ForgetMeGrpcClient.class)));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());

            HeaderEventListener interceptor = new HeaderEventListener();

            com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.ForgetMeStub asrForgetMeGrpcStub = buildAsrForgetMeGrpcStub(params, errorHandler, interceptor, getCustomGrpcHeaders(sessionId));
            com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.ForgetMeStub logForgetMeGrpcStub = buildLogForgetMeGrpcStub(params, errorHandler, interceptor, getCustomGrpcHeaders(sessionId));

            if (asrForgetMeGrpcStub == null || logForgetMeGrpcStub == null) {
                return null;
            }

            return new ForgetMeGrpcClient(sessionId, params, asrForgetMeGrpcStub, logForgetMeGrpcStub, responseHandler, errorHandler, interceptor);
        } else
        {
            logger.info("No userReset client matching {}", userResetBackend);
        }
        return null;
    }

    private com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.ForgetMeStub buildAsrForgetMeGrpcStub(CombinedConfiguration params, ResponseErrorHandler errorHandler, HeaderEventListener interceptor, Map<String, String> customHeaders) {
        URL url = UrlUtils.getServerUrl(params.getString(WebApiConfig.Property.ASR_FORGET_ME_GRPC_SERVER_URL.key));
        InterceptedManagedChannel channel = ManagedChannelManager.getManagedChannel(UrlUtils.getServerHost(url), UrlUtils.getServerPort(url), UrlUtils.isPlainTextProtocol(url), params, customHeaders);
        channel.getInterceptor().setListener(interceptor);

        com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.ForgetMeStub userResetGrpcStub = com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.newStub(channel.getChannel());

        if (params.getString(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN)) {
            String mixToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(params));
            if (mixToken == null || mixToken.isBlank())
            {
                errorHandler.handleError(new InterpretationResponse(true, ResponseCode.createResponseError(ExternalResponseErrorCode.MIX_AUTH_ERROR)));
                return null;
            }
            AuthorizationCredentials bearerToken = new AuthorizationCredentials(mixToken, AuthorizationCredentials.AUTH_TYPE_BEARER);
            userResetGrpcStub = userResetGrpcStub.withCallCredentials(bearerToken);
        }
        else if(params.getString(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_PASSTHROUGH)) {
            userResetGrpcStub = (com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.ForgetMeStub) addPassthroughAuthorization(userResetGrpcStub, params);
        }
        else {
            logAuthMethod(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key, params.getString(WebApiConfig.Property.ASR_FORGET_ME_GRPC_AUTH_METHOD.key));
        }
        return userResetGrpcStub;
    }

    private com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.ForgetMeStub buildLogForgetMeGrpcStub(CombinedConfiguration params, ResponseErrorHandler errorHandler, HeaderEventListener interceptor, Map<String, String> customHeaders) {
        URL url = UrlUtils.getServerUrl(params.getString(WebApiConfig.Property.LOG_FORGET_ME_GRPC_SERVER_URL.key));
        String authorityOverride = params.getString(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTHORITY_OVERRIDE.key);
        InterceptedManagedChannel channel = ManagedChannelManager.getManagedChannel(UrlUtils.getServerHost(url), UrlUtils.getServerPort(url), UrlUtils.isPlainTextProtocol(url), params, customHeaders, authorityOverride);
        channel.getInterceptor().setListener(interceptor);

        com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.ForgetMeStub userResetGrpcStub = com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.newStub(channel.getChannel());

        if (params.getString(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_MIX_BEARER_TOKEN)) {
            String mixToken = MixClientCredentialsManager.getBearerToken(MixUtils.mixClientCredentialsFromConfig(params));
            if (mixToken == null || mixToken.isBlank())
            {
                errorHandler.handleError(new InterpretationResponse(true, ResponseCode.createResponseError(ExternalResponseErrorCode.MIX_AUTH_ERROR)));
                return null;
            }
            AuthorizationCredentials bearerToken = new AuthorizationCredentials(mixToken, AuthorizationCredentials.AUTH_TYPE_BEARER);
            userResetGrpcStub = userResetGrpcStub.withCallCredentials(bearerToken);
        }
        else if(params.getString(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key).equals(KryptonGrpcClient.AUTH_PASSTHROUGH)) {
            userResetGrpcStub = (com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.ForgetMeStub) addPassthroughAuthorization(userResetGrpcStub, params);
        }
        else {
            logAuthMethod(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key, params.getString(WebApiConfig.Property.LOG_FORGET_ME_GRPC_AUTH_METHOD.key));
        }
        return userResetGrpcStub;
    }

    public DynamicVocabularyClient createDynamicVocabularyClient(CombinedConfiguration config, String sessionId, ResponseHandler responseHandler)
    {
        //For now, we only support Mrec client.
        return new DynamicVocabularyMrecClient(sessionId, config, responseHandler);
    }



    public HealthCheckClient createHealthCheckClient(String type, Map<String, Object> params)
            throws HealthCheckException
    {
        final String host;
        final int port;
        switch (type) {
            case "NinaMobile":
                //TODO: better validation of parameters
                host = (String)params.get("nmspGatewayHost");
                port = (int)params.get("nmspGatewayPort");

                //TODO: implement deep check of NiM
                return new SocketConnectHealthCheckClient(host, port);
            case "tcpSocket":
                host = (String)params.get("endpointHost");
                port = (int)params.get("endpointPort");
                return new SocketConnectHealthCheckClient(host, port);
            default:
                throw new HealthCheckException("Unknown service type -- " + type);
        }
    }

    private Map<String, String> getCustomGrpcHeaders(String sessionId) {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("x-client-request-id", sessionId);
        return customHeaders;
    }

    protected Map<String, String> getCustomGrpcHeaders(String sessionId, String nvcVersion) {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("x-client-request-id", sessionId);

        if (nvcVersion != null && nvcVersion.equals("nvc3")) {
            customHeaders.put("x-nuance-tts-neural", "true");
        }

        logger.info("Setting custom grpc headers {}", customHeaders);

        return customHeaders;
    }
}
