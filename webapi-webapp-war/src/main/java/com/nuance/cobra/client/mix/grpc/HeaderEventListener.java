package com.nuance.cobra.client.mix.grpc;

import java.util.Map;

public class HeaderEventListener implements HeaderClientInterceptor.InterceptedHeaderListener {
    public interface HeaderListener {
        void newHeader(Map<String, Object> header);
    }

    HeaderListener listener = null;

    public void setListener (HeaderListener listener) {
        this.listener = listener;
    }

    public HeaderEventListener() {
    }

    @Override
    public void newHeader(Map<String, Object> header) {
        if (listener != null) {
            listener.newHeader(header);
        }
    }
}
