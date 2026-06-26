package com.nuance.cobra.client;

import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.SessionManager;

public abstract class Client
{
    

    /**
     * The session ID
     */
    protected String sessionId = null;
    
    
    Client(String sessionId)
    {
        if (sessionId != null)
        {
            this.sessionId = sessionId;
        }
    }
    

    /**
     * Getter that returns the sessionId
     * @return String
     *          The session Id
     */
    public String getSessionId()
    {
        return sessionId;
    }


    public boolean hasActiveSession() {
        Session session = SessionManager.getInstance().getSession(getSessionId(),true);
        return (session != null && !session.isSessionEnded());
    }
}
