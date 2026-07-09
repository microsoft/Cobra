package com.nuance.cobra.client.mix.nvc;

import com.google.common.collect.ImmutableMap;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.SpeechSynthesisClient;
import com.nuance.cobra.client.VocalizerClients;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.mix.grpc.ManagedChannelManager;
import com.nuance.cobra.client.response.SpeechSynthesisResponse;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.ExceptionWithExternalResponseErrorCode;
import com.nuance.cobra.utils.audio.AudioCodec;
import com.nuance.cobra.utils.audio.encoders.SpeexEncoder;
import com.nuance.cobra.utils.request.VOCSVoiceConfigUtil;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.tts.v1.SynthesizerGrpc;
import com.nuance.rpc.tts.v1.SynthesizerProto;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public class NvcClient extends SpeechSynthesisClient {

    private static final Logger logger = LogManager.getLogger();

    public static final String AUTH_MIX_BEARER_TOKEN = "mixBearerToken";
    public static final String AUTH_NO_AUTH = "no_auth";
    public static final String AUTH_PASSTHROUGH = "passthrough";

    protected final NvcRequestParams params;
    private final SynthesizerGrpc.SynthesizerStub stub;

    private final ResponseHandler responseHandler;
    private final ResponseErrorHandler errorHandler;

    protected boolean isSpeechSynthesisDone = false;

    private SpeexEncoder speexEncoder;

    public NvcClient(String sessionId,
                     NvcRequestParams params,
                     SynthesizerGrpc.SynthesizerStub stub, // injected, to enable unit testing
                     ResponseHandler responseHandler,
                     ResponseErrorHandler errorHandler, HeaderEventListener interceptor) {

        super(sessionId);

        this.params = params;
        this.stub = stub;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        interceptor.setListener(new HeaderEventListener.HeaderListener() {
            @Override
            public void newHeader(Map<String, Object> header) {
                Session session = SessionManager.getInstance().getSession(getSessionId(), false);
                if (session == null) return;
                if (session.getCommand() == null) return;

                session.getCommand().getStatistics().addEngineResponse("grpcHeaders", header);

                MixUtils.checkIfRateLimited(header, logger);
            }
        });

        AudioCodec codec = params.getCodec();
        if (codec != null &&
            codec.getEncoding() == AudioCodec.Encoding.SPEEX) {
            if (codec.getSampleRate() == 8000) {
                speexEncoder = new SpeexEncoder(SpeexEncoder.NARROWBAND, 6, 8000);
            } else if (codec.getSampleRate() == 16000) {
                speexEncoder = new SpeexEncoder(SpeexEncoder.WIDEBAND, 8, 16000);
            }
        }
    }

    @Override
    public void startSpeechSynthesis() {
        try {
            SynthesizerProto.SynthesisRequest request = buildRequest();
            logger.debug("Sending NVC request:\n{}", request);
            if (params.isAudioChunkDisabled()) {
                stub.unarySynthesize(request, new NvcResponseObserverUnary(this));
            } else {
                stub.synthesize(request, new NvcResponseObserverStreaming(this));
            }

        } catch (Exception e) {
            MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
            MixUtils.invalidateTokenIfUnauthorized(logger, e, params.getConfig());

            if (e instanceof ExceptionWithExternalResponseErrorCode) {
                sendError(((ExceptionWithExternalResponseErrorCode) e).getErrorCode());
            } else {
                sendError(ExternalResponseErrorCode.SPEECH_SYNTHESIS_COMMAND_EXECUTE_ERROR);
            }
        }
    }

    private SynthesizerProto.SynthesisRequest buildRequest()
            throws InvalidKeySpecException, ExceptionWithExternalResponseErrorCode
    {
        params.assertValidity();

        SynthesizerProto.SynthesisRequest.Builder builder =
                SynthesizerProto.SynthesisRequest.newBuilder()
                        .setVoice(SynthesizerProto.Voice.newBuilder()
                                .setName(params.getVoiceName())
                                .setModel(params.getVoiceModel())
                                .setLanguage(params.getVoiceLanguage()))
                        .setAudioParams(params.getAudioParameters())
                        .setEventParams(SynthesizerProto.EventParameters.newBuilder()
                                .setSuppressInput(params.shouldMaskUserUtterance()))
                        .setInput(buildRequestInput());

        if (params.isStatisticsEnabled()) {
            builder.setEventParams(SynthesizerProto.EventParameters.newBuilder()
                    .setSendLogEvents(true));
        }

        ImmutableMap<String, String>
                clientData = MixUtils.getClientDataMap(this.sessionId, params.getConfig(), Map.of("language", params.getVoiceLanguage()));
        if(logger.isTraceEnabled()) logger.trace("Adding clientData: {}", StringUtils.join(clientData));

        builder.putAllClientData(clientData);
        addUserId(builder);

        setEngineRequestStatistics(VocalizerClients.nvc.getValue(), params.getVoiceLanguage(), params.getVoiceName(),
                params.getVoiceModel(), VOCSVoiceConfigUtil.getVoiceQuality(params.getConfig()), VOCSVoiceConfigUtil.getResources(params.getConfig()));

        return builder.build();
    }

    private void addUserId(SynthesizerProto.SynthesisRequest.Builder builder)
    {
        if (!MixUtils.shouldAddUserId(params.getConfig(), WebApiConfig.Property.NVC_GRPC_USE_USERID, logger))
        {
            return;
        }

        String userId = params.getConfig().getString(WebApiConfig.Property.CLIENT_USER_ID.key);
        logger.trace("Adding user_id: {}", userId);
        builder.setUserId(userId);
    }

    private SynthesizerProto.Input.Builder buildRequestInput() throws ExceptionWithExternalResponseErrorCode {
        SynthesizerProto.Input.Builder input = SynthesizerProto.Input.newBuilder();

        String inputType = params.getInputType();

        if ("text".equals(inputType)) {
            input.setText(SynthesizerProto.Text.newBuilder()
                    .setText(params.getInputBody()));

        } else if ("ssml".equals(inputType)) {
            input.setSsml(SynthesizerProto.SSML.newBuilder()
                    .setText(params.getInputBody()));

        } else if (inputType == null) {
            throw new IllegalArgumentException("TTS input type is null.");
        } else {
            throw new IllegalArgumentException(String.format("TTS input type is of unsupported type: %s", inputType));
        }

        for (SynthesizerProto.SynthesisResource.Builder resource : params.getSynthesisResources()) {
            input.addResources(resource);
        }

        return input;
    }

    @Override
    public void stopSpeechSynthesis() {
        sendResponse(ResponseResult.ResponseStatus.SUCCESS);
    }

    @Override
    public void cancelSpeechSynthesis(boolean isFinal) {
        sendResponse(ResponseResult.ResponseStatus.CANCELLED);
    }

    protected void sendAudio(byte[] audioBytes) {
        if (!isSpeechSynthesisDone) {
            AudioCodec codec = params.getCodec();
            if (codec.getEncoding() == AudioCodec.Encoding.SPEEX) {
                audioBytes = speexEncoder.encode(audioBytes);
            }
            responseHandler.onAudio(audioBytes, false); // isLastBuffer argument is always ignored
        }
    }

    protected void sendResponse(ResponseResult.ResponseStatus status) {
        if (!isSpeechSynthesisDone) {
            isSpeechSynthesisDone = true;
            ManagedChannelManager.managedChannelShutdown(params.getConfig(), (ManagedChannel)stub.getChannel());

            SpeechSynthesisResponse response = new SpeechSynthesisResponse(status, true);
            responseHandler.onResponse(response);
        }
    }

    protected void sendError(ExternalResponseErrorCode errorCode) {
        if (!isSpeechSynthesisDone) {
            isSpeechSynthesisDone = true;
            ManagedChannelManager.managedChannelShutdown(params.getConfig(), (ManagedChannel)stub.getChannel());

            SpeechSynthesisResponse response = new SpeechSynthesisResponse(true, ResponseCode.createResponseError(errorCode));
            errorHandler.handleError(response);
        }
    }

    @Override
    public boolean isSpeechSynthesisDone() {
        return isSpeechSynthesisDone;
    }
}
