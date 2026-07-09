package com.nuance.cobra.client.mix;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.LoggingUtils;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public abstract class ResponseObserver<T> implements StreamObserver<T> {
    protected static final Logger logger =  LogManager.getLogger();

    protected abstract boolean isDone();
    protected abstract void errorHandler(Throwable e);
    protected abstract void completeTransaction(Throwable e, ExternalResponseErrorCode responseCode);
    protected abstract String getSessionId();

    @Override
    public void onError(Throwable e)
    {
        Session session = SessionManager.getInstance()
                                        .getSession(getSessionId(), true);
        LoggingUtils.setThreadContext(session);

        String appId = "";
        String mixAppId = "";
        try
        {
            appId = session.getSessionConfig().getString(WebApiConfig.Property.APPLICATION_KEY.key, "");
            mixAppId = session.getSessionConfig().getString(WebApiConfig.Property.MIX_CLIENT_ID.key, appId);
        } catch (Exception e2)
        {
            //Couldn't get appID's for logging
        }

        if (isDone()) {
            return;
        }

        if (e instanceof StatusRuntimeException)
        {
            var statusRuntimeException = (StatusRuntimeException) e;
            if (statusRuntimeException.getStatus().getCode() == io.grpc.Status.Code.UNAVAILABLE) {
                Set<String> grpcHeaders = statusRuntimeException.getTrailers().keys();

                if (grpcHeaders.contains("x-envoy-ratelimited")) {
                    Metadata.Key<Boolean> testKey = Metadata.Key.of("x-envoy-ratelimited", new Metadata.AsciiMarshaller<Boolean>() {
                        @Override
                        public String toAsciiString(Boolean aBoolean) {
                            return aBoolean.toString();
                        }

                        @Override
                        public Boolean parseAsciiString(String s) {
                            return Boolean.parseBoolean(s);
                        }
                    });
                    if (Boolean.TRUE.equals(statusRuntimeException.getTrailers().get(testKey))) {
                        logger.error("Backend rate limiting reached for {}", mixAppId, e);
                        completeTransaction(e, ExternalResponseErrorCode.BACKEND_ENGINE_RATE_LIMIT_EXCEEDED);
                        return;
                    }
                }
            }
        }

        errorHandler(e);
    }
}
