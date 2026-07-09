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
 * The StartSessionResponse class which is a child of the ResponseResult class.
 */
public class StartSessionResponse extends ResponseResult
{
    private static final Logger logger = LogManager.getLogger(StartSessionResponse.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(StartSessionResponse.class);

    /**
     * Constructor with no arguments for StartSessionResponse
     */
    private StartSessionResponse()
    {
        super();

    }

    /**
     * Constructor based on status and finalresponse boolean for StartSessionResponse
     *
     * @param status
     *      The response status
     * @param finalResponse
     *      Boolean telling if the response if final or intermediate
     */
    public StartSessionResponse(ResponseStatus status, boolean finalResponse)
    {
        super(status, finalResponse);
    }

    /**
     * Constructor based on Response error object for StartSessionResponse
     * @param error
     */
    public StartSessionResponse(ResponseCode... error)
    {
        super(error);
    }

    /**
     * Building a StartSessionResponse from a Nim Result
     * @param result
     *      The result received from NiM represented as a JSONObject
     * @return
     *      A new StartSessionResponse object
     */
    public static StartSessionResponse fromNiMResult(JSONObject result)
    {
        EsapiLogger.trace(esapiLogger, "StartSessionResponse fromNiMResult: %s", result.toString());
        ResponseStatus status = ResponseStatus.SUCCESS;
        return new StartSessionResponse(status, true);

    }

    /**
     * Building a StartSessionResposne from a Nim Error
     * @param error
     *      The error received from NiM represented as a JSONObject
     * @return
     *      A new StartSessionResponse object
     */
    public static StartSessionResponse fromNiMError(JSONObject error)
    {
        EsapiLogger.trace(esapiLogger, "StartSessionResponse fromNiMError: %s", error.toString());

        // Get error code
        int errorCode = 999;

        JSONObject jsonObject = error.getJSONObject("QueryError");
        if (jsonObject != null)
        {
            errorCode = jsonObject.getInt("error");
        }

        return new StartSessionResponse(ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_NMAS_ERROR),
                ResponseCode.createResponseErrorForNimService(errorCode));
    }

}
