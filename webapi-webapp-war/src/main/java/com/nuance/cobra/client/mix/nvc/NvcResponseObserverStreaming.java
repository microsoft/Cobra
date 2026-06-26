package com.nuance.cobra.client.mix.nvc;

import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.rpc.tts.v1.SynthesizerProto;

class NvcResponseObserverStreaming extends NvcResponseObserver<SynthesizerProto.SynthesisResponse> {
    public NvcResponseObserverStreaming(NvcClient nvcClient) {
        super(nvcClient);
    }

    @Override
    public void onNext(SynthesizerProto.SynthesisResponse response) {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(getSessionId(), true));

        switch (response.getResponseCase()) {

            case STATUS:
                onStatus(response.getStatus());
                break;

            case EVENTS:
                onEvent(response.getEvents()); // there can be multiple events
                break;

            case AUDIO:
                onAudio(response.getAudio());
                break;
        }
    }
}
