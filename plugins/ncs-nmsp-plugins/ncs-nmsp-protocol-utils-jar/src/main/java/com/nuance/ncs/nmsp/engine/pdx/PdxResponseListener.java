package com.nuance.ncs.nmsp.engine.pdx;

/** Callback interface replacing ManagerListener + NMASResourceListener + PDXCommandListener */
public interface PdxResponseListener {
    /** COP_CONNECTED: sessionId received */
    void onConnected(String sessionId);
    /** COP connection failed */
    void onConnectionFailed(short reasonCode);
    /** COP disconnected */
    void onDisconnected(short reasonCode);
    /** BCP_DATA with QueryResult */
    void onQueryResult(PdxQueryResult result);
    /** BCP_DATA with QueryError */
    void onQueryError(PdxQueryError error);
    /** BCP_DATA with QueryRetry */
    void onQueryRetry(PdxQueryRetry retry);
    /** VAP_PLAY received */
    void onAudioReceived(byte[] audioData);
    /** Any Netty channel error */
    void onError(Throwable cause);
}
