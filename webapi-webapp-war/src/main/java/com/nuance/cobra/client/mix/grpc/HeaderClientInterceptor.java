package com.nuance.cobra.client.mix.grpc;

import io.grpc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class HeaderClientInterceptor  implements ClientInterceptor {
    private static final Logger logger = LogManager.getLogger();

    private Map<String, String> customHeaders;

    public  HeaderClientInterceptor(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public interface InterceptedHeaderListener {
        void newHeader(Map<String, Object> header);
    }

    InterceptedHeaderListener listener = null;

    public void setListener(InterceptedHeaderListener listener) {
        this.listener = listener;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {

            @Override
            public void start(ClientCall.Listener<RespT> responseListener, Metadata headers) {
                insertCustomHeaders(headers);
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        logger.debug("header received from server: {}", headers);
                        Map<String, Object> headersMap = new HashMap<>();

                        for (String key: headers.keys()) {
                            headersMap.put(key, headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))); //google this is soo dumb!!!
                        }

                        notiffyListeners(headersMap);
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }

    protected void insertCustomHeaders(Metadata headers) {
        for(Map.Entry<String, String> pair : customHeaders.entrySet()) {
            headers.put(Metadata.Key.of(pair.getKey(), Metadata.ASCII_STRING_MARSHALLER), pair.getValue());
        }
    }

    private void notiffyListeners(Map<String, Object> headers) {
        if (listener != null) {
            listener.newHeader(headers);
        }
    }
}
