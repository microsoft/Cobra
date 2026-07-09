package com.nuance.cobra.client.mix;

import org.json.JSONObject;
import org.owasp.esapi.ESAPI;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class MixAccessToken
{
    private final JSONObject accessToken;
    private final Instant actualExpirationTime;

    private static final String ACCESS_TOKEN = "access_token";

    MixAccessToken(JSONObject accessToken, Instant actualExpirationTime)
    {
        this.accessToken = accessToken;
        this.actualExpirationTime = actualExpirationTime;
    }

    public String getAccessToken()
    {
        return extractAccessToken(accessToken);
    }
    
    public JSONObject getResponseJson()
    {
        JSONObject rc = new JSONObject();
        rc.put(ACCESS_TOKEN, ESAPI.encoder().encodeForHTML(accessToken.getString(ACCESS_TOKEN)));
        rc.put("scope", ESAPI.encoder().encodeForHTML(accessToken.getString("scope")));
        rc.put("token_type", ESAPI.encoder().encodeForHTML(accessToken.getString("token_type")));
        rc.put("expires_in", actualExpirationTime.minus(Instant.now().getEpochSecond(), ChronoUnit.SECONDS).getEpochSecond());
        return rc;
    }

    public Instant getActualExpirationTime()
    {
        return actualExpirationTime;
    }

    protected static String extractAccessToken(JSONObject authJson)
    {
        if(authJson == null)
        {
            return "";
        }
        return authJson.optString(ACCESS_TOKEN);
    }
}
