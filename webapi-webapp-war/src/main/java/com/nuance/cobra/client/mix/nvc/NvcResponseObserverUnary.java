package com.nuance.cobra.client.mix.nvc;

import com.nuance.cobra.session.SessionManager;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.rpc.tts.v1.SynthesizerProto;

class NvcResponseObserverUnary extends NvcResponseObserver<SynthesizerProto.UnarySynthesisResponse> {

    public NvcResponseObserverUnary(NvcClient nvcClient) {
        super(nvcClient);
    }

    @Override
    public void onNext(SynthesizerProto.UnarySynthesisResponse response) {
        LoggingUtils.setThreadContext(SessionManager.getInstance().getSession(getSessionId(), true));
        onStatus(response.getStatus());

        onEvent(response.getEvents()); // there can be multiple events

        onAudio(response.getAudio());
    }
}
