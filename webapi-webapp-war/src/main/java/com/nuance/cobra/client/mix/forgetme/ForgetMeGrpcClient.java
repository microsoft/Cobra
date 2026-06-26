package com.nuance.cobra.client.mix.forgetme;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.UserResetClient;
import com.nuance.cobra.client.UserResetClients;
import com.nuance.cobra.client.mix.MixUtils;
import com.nuance.cobra.client.mix.grpc.HeaderEventListener;
import com.nuance.cobra.client.mix.grpc.ManagedChannelManager;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.rpc.asr.forgetme.v1.DeleteSpeakerProfilesRequest;
import com.nuance.rpc.asr.forgetme.v1.DeleteSpeakerProfilesResponse;
import com.nuance.rpc.fabric.forget.v1.ForgetRequest;
import com.nuance.rpc.fabric.forget.v1.ForgetResponse;
import com.nuance.rpc.fabric.forget.v1.ForgetType;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Mix forget me gRPC client
 */
public class ForgetMeGrpcClient
        extends UserResetClient {

    private static final Logger logger = LogManager.getLogger();

    private static final String ENGINE_TYPE = UserResetClients.MIX.getValue();

    protected final CombinedConfiguration cobraConfig;
    private final com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.ForgetMeStub asrForgetMeStub;
    private final com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.ForgetMeStub logForgetMeStub;
    private final StreamObserver<DeleteSpeakerProfilesResponse> deleteSpeakerProfilesResponseStreamObserver;
    private final StreamObserver<ForgetResponse> logForgetResponseStreamObserver;

    private final ResponseHandler responseHandler;
    private final ResponseErrorHandler errorHandler;

    protected boolean isUserResetDone;

    public ForgetMeGrpcClient(String sessionId,
                              CombinedConfiguration cobraConfig,
                              com.nuance.rpc.asr.forgetme.v1.ForgetMeGrpc.ForgetMeStub asrForgetMeStub,
                              com.nuance.rpc.fabric.forget.v1.ForgetMeGrpc.ForgetMeStub logForgetMeStub,
                              ResponseHandler responseHandler,
                              ResponseErrorHandler errorHandler, HeaderEventListener interceptor) {

        super(sessionId);

        this.cobraConfig = cobraConfig;
        this.asrForgetMeStub = asrForgetMeStub;
        this.logForgetMeStub = logForgetMeStub;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        interceptor.setListener(header -> {
            var session = SessionManager.getInstance().getSession(getSessionId(), false);
            if (session == null) return;
            if (session.getCommand() == null) return;

            session.getCommand().getStatistics().addEngineResponse("grpcHeaders", header);

            MixUtils.checkIfRateLimited(header, logger);
        });
        deleteSpeakerProfilesResponseStreamObserver = new AsrForgetMeGrpcResponseObserver(this);
        logForgetResponseStreamObserver = new LogForgetMeGrpcResponseObserver(this);
    }

    public CombinedConfiguration getCobraConfig() {
        return cobraConfig;
    }

    @Override
    public void startUserReset()
    {
        startAsrForgetRequest();
    }

    public void startAsrForgetRequest()
    {
        Runnable asrForgetRequestRunnable = () ->
        {
            DeleteSpeakerProfilesRequest asrForgetRequest = buildAsrForgetRequest();
            logger.debug("Sending Mix ASR-ForgetMe as a service gRPC request:\n{}", asrForgetRequest);
            asrForgetMeStub.deleteSpeakerProfiles(asrForgetRequest, deleteSpeakerProfilesResponseStreamObserver);
        };
        startForgetRequest(asrForgetRequestRunnable);
    }

    public void startLogForgetRequest()
    {
        Runnable logForgetRequestRunnable = () ->
        {
            ForgetRequest logForgetRequest = buildLogForgetRequest();
            logger.debug("Sending Mix Log-ForgetMe as a service gRPC request:\n{}", logForgetRequest);
            logForgetMeStub.forget(logForgetRequest, logForgetResponseStreamObserver);
        };
        startForgetRequest(logForgetRequestRunnable);
    }

    private void startForgetRequest(Runnable forgetRequest)
    {
        try
        {
            forgetRequest.run();
        } catch (Exception e)
        {
            MixUtils.handleMixThrowable(logger, this.getClass().getSimpleName(), new Throwable().getStackTrace()[0].getMethodName(), e);
            MixUtils.invalidateTokenIfUnauthorized(logger, e, getCobraConfig());

            sendErrorResponse(ExternalResponseErrorCode.USER_RESET_COMMAND_EXECUTE_ERROR);
        }
    }

    private DeleteSpeakerProfilesRequest buildAsrForgetRequest()
    {
        Map<String, String> clientData = MixUtils.getClientDataMap(sessionId, cobraConfig);

        setStatistics(ENGINE_TYPE);

        return DeleteSpeakerProfilesRequest.newBuilder()
                .setUserId(cobraConfig.getString(WebApiConfig.Property.CLIENT_USER_ID.key))
                .putAllClientData(clientData)
                .build();
    }

    private ForgetRequest buildLogForgetRequest()
    {
        Map<String, String> clientData = MixUtils.getClientDataMap(sessionId, cobraConfig);

        setStatistics(ENGINE_TYPE);

        return ForgetRequest.newBuilder()
                            .setForgetType(ForgetType.USER)
                            .setForgetId(cobraConfig.getString(WebApiConfig.Property.CLIENT_USER_ID.key))
                            .putAllClientData(clientData).build();
    }

    @Override
    public void stopUserReset() {
        // Nothing to do
    }

    @Override
    public void cancelUserReset(boolean isFinal) {
        // Nothing to do
    }

    protected synchronized void sendResponse(ResponseResult response) {
        if (!isUserResetDone) {
            isUserResetDone = true;
            ManagedChannelManager.managedChannelShutdown(getCobraConfig(), (ManagedChannel) asrForgetMeStub.getChannel());

            String responseJson = null;
            try {
                responseJson = response.toPrettyJson();
            } catch (CobraException e) {
                logger.error("Error while converting userReset response to JSON", e);
            }
            logger.debug("Sending back Cobra userReset response to client:\n{}", responseJson);

            responseHandler.onResponse(response);
        }
    }

    protected synchronized void sendErrorResponse(ExternalResponseErrorCode errorCode, String ... args) {
        if (!isUserResetDone) {
            isUserResetDone = true;
            ManagedChannelManager.managedChannelShutdown(getCobraConfig(), (ManagedChannel) asrForgetMeStub.getChannel());

            var errorResponse = new ResponseResult(true, ResponseCode.createResponseError(errorCode, args));

            String errorResponseJson = null;
            try {
                errorResponseJson = errorResponse.toPrettyJson();
            } catch (CobraException e) {
                logger.error("Error while converting userReset error response to JSON", e);
            }
            logger.debug("Sending back Cobra userReset error response to client:\n{}", errorResponseJson);

            errorHandler.handleError(errorResponse);
        }
    }
}
