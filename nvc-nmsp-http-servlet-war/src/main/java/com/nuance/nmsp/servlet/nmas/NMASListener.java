package com.nuance.nmsp.servlet.nmas;

import com.nuance.ncs.nmsp.engine.pdx.PdxQueryError;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryResult;
import com.nuance.ncs.nmsp.engine.pdx.PdxQueryRetry;
import com.nuance.ncs.nmsp.engine.pdx.PdxResponseListener;
import com.nuance.nmsp.servlet.Transaction;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.servlet.http.HttpServletResponse;

/**
 * Listens to events from the PDX engine and relays them to the in-flight Transaction.
 */
public class NMASListener
        implements PdxResponseListener
{

    private static final Logger log = ESAPI.getLogger(NMASListener.class);

    protected Transaction transaction;

    public NMASListener(Transaction transaction)
    {
        this.transaction = transaction;
    }

    @Override
    public void onConnected(String sessionId)
    {
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + " We're now connected to the GW! (" + sessionId + ")");
        transaction.setSessionId(sessionId);
    }

    @Override
    public void onConnectionFailed(short reasonCode)
    {
        log.error(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                        .getName() + " Connection to the GW failed!");
        transaction.setError("Connection to the NMSP GW failed. Returning 503.");
        transaction.setErrorID(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        transaction.setRequestCompleted(false);
    }

    @Override
    public void onDisconnected(short reasonCode)
    {
        log.info(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                       .getName() + " We're now disconnected from the GW! reason=" + reasonCode);
        if (reasonCode > 0)
        {
            String errorMsg = "Unknown error from the NMSP Gateway";
            switch (reasonCode)
            {
                case 1:
                    errorMsg = "CLIENT_SOCKET_SHUTDOWN_UNEXPECTEDLY";
                    transaction.setErrorID(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    break;
                case 2:
                    errorMsg = "CLIENT_PING_TIMEOUT";
                    transaction.setErrorID(HttpServletResponse.SC_REQUEST_TIMEOUT);
                    break;
                case 3:
                    errorMsg = "CLIENT_IDLE_TIMEOUT";
                    transaction.setErrorID(HttpServletResponse.SC_REQUEST_TIMEOUT);
                    break;
                case 4:
                    errorMsg = "NSS_TERMINATED_SESSION_TIMEOUT";
                    transaction.setErrorID(HttpServletResponse.SC_REQUEST_TIMEOUT);
                    break;
                case 5:
                    errorMsg = "NSS_TERMINATED_SESSION_ERROR";
                    transaction.setErrorID(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    break;
                case 6:
                    errorMsg = "GW_TERMINATED_SESSION_ERROR";
                    transaction.setErrorID(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    break;
                case 7:
                    errorMsg = "APP_SERVER_SESSION_TERMINATED_NORMALLY";
                    transaction.setErrorID(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    break;
                case 8:
                    errorMsg = "APP_SERVER_SESSION_TERMINATED_ERROR";
                    transaction.setErrorID(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    break;
                default:
                    transaction.setErrorID(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    break;
            }
            log.warning(Logger.EVENT_UNSPECIFIED, String.format("reasonCode = %d. errorMsg = %s. Returned errorId = %d", reasonCode, errorMsg, transaction.getErrorID()));
            transaction.setError(errorMsg);
            transaction.setRequestCompleted(false);
        }
    }

    @Override
    public void onQueryResult(PdxQueryResult result)
    {
        log.info(Logger.EVENT_UNSPECIFIED, "Default onQueryResult: subclasses should override.");
    }

    @Override
    public void onQueryError(PdxQueryError error)
    {
        String errorMsg = "Received QueryError: " + error.getError() + " " + error.getDescription();
        log.error(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                  .getName() + " " + errorMsg);
        int errorId = error.getError();
        switch (errorId)
        {
            case 550:
                transaction.setErrorID(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                break;
            case 507:
            case 508:
            case 509:
            case 510:
            case 511:
            case 515:
                transaction.setErrorID(HttpServletResponse.SC_NOT_ACCEPTABLE);
                break;
            case 503:
            case 512:
                transaction.setErrorID(HttpServletResponse.SC_UNAUTHORIZED);
                break;
            case 504:
            case 505:
            case 506:
            default:
                transaction.setErrorID(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                break;
        }
        log.error(Logger.EVENT_UNSPECIFIED, String.format("errorId = %d. errorMsg = %s. Returned errorId = %d", errorId, errorMsg, transaction.getErrorID()));
        transaction.setError(errorMsg);
        transaction.setRequestCompleted(false);
    }

    @Override
    public void onQueryRetry(PdxQueryRetry retry)
    {
        String errorMsg = "Received QueryRetry: " + retry.getCause() + " " + retry.getName();
        log.error(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                                                  .getName() + " " + errorMsg);
        transaction.setErrorID(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        transaction.setError(errorMsg);
        transaction.setRequestCompleted(false);
    }

    @Override
    public void onAudioReceived(byte[] audioData)
    {
        // Default no-op; TTS subclass overrides to forward to the audio sink.
    }

    @Override
    public void onError(Throwable cause)
    {
        log.error(Logger.EVENT_UNSPECIFIED, Thread.currentThread()
                        .getName() + " PDX channel error: " + cause.getMessage(), cause);
        transaction.setError("PDX channel error: " + cause.getMessage());
        transaction.setErrorID(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        transaction.setRequestCompleted(false);
    }
}
