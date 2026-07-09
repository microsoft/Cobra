package com.nuance.plugin.nmsp.v1.webapi.orchestrator;

import javax.websocket.CloseReason;

public interface IWebapiOrchestratorEvents {
    void onResponse(String response);

    void onBinary(byte[] data);

    void onClose(CloseReason closeReason);
}
