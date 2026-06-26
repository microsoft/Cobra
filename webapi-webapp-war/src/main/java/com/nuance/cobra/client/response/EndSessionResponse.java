package com.nuance.cobra.client.response;


import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.utils.logging.EsapiLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;


/**
 * The EndSessionResponse class which is a child of the ResponseResult class.
 */
public class EndSessionResponse extends ResponseResult
{

    private static final Logger logger = LogManager.getLogger(EndSessionResponse.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(EndSessionResponse.class);

    /**
     * Constructor based on status and finalresponse boolean for EndSessionResponse
     *
     * @param status
     *      The response status
     * @param finalResponse
     *      Boolean telling if the response if final or intermediate
     */
    public EndSessionResponse(ResponseStatus status, boolean finalResponse)
    {
        super(status, finalResponse);
    }

    /**
     * Constructor based on Response error object for EndSessionResponse
     * @param error
     */
    public EndSessionResponse(ResponseCode... error)
    {
        super(error);
    }

    /**
     * Building a EndSessionResponse from a Nim Error
     * @param error
     *      The error received from NiM represented as a JSONObject
     * @return
     *      A new EndSessionResponse object
     */
    public static EndSessionResponse fromNiMError(JSONObject error)
    {
        EsapiLogger.trace(esapiLogger, "EndSessionResponse fromNiMError: %s", error);
        // Get error code
        int errorCode = 999;

        JSONObject jsonObject = error.getJSONObject("QueryError");
        if (jsonObject != null)
        {
            errorCode = jsonObject.getInt("error");
        }

        return new EndSessionResponse(ResponseCode.createResponseError(ExternalResponseErrorCode.END_SESSION_NMAS_ERROR),
                ResponseCode.createResponseErrorForNimService(errorCode));
    }
}
