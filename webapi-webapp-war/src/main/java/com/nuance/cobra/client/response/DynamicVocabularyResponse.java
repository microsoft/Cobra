package com.nuance.cobra.client.response;

import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * The DynamicVocabularyResponse class which is a child of the ResponseResult class.
 */
public class DynamicVocabularyResponse
        extends ResponseResult
{
    private static final Logger logger = LogManager.getLogger(DynamicVocabularyResponse.class);

    /**
     * Constructor with no arguments for DynamicVocabularyResponse
     */
    private DynamicVocabularyResponse()
    {
        super();

    }

    /**
     * Constructor based on status and finalresponse boolean for DynamicVocabularyResponse
     *
     * @param status
     *      The response status
     * @param finalResponse
     *      Boolean telling if the response if final or intermediate
     */
    public DynamicVocabularyResponse(ResponseStatus status, boolean finalResponse)
    {
        super(status, finalResponse);
    }

    /**
     * Constructor based on Response error object for DynamicVocabularyResponse
     * @param error
     */
    public DynamicVocabularyResponse(ResponseCode... error)
    {
        super(error);
    }

    /**
     * Building a DynamicVocabularyResponse from a Nim Result
     * @param result
     *      The result received from NiM represented as a JSONObject
     * @return
     *      A new DynamicVocabularyResponse object
     */
    public static DynamicVocabularyResponse fromNiMResult(JSONObject result)
    {
        logger.trace("DynamicVocabularyResponse fromNiMResult ");

        logger.error("This method shouldn't be called since this command is not performed through NiM");
        return new DynamicVocabularyResponse(ResponseStatus.SUCCESS, true);
    }

    /**
     * Building a DynamicVocabularyResponse from a Nim Error
     * @param error
     *      The error received from NiM represented as a JSONObject
     * @return
     *      A new DynamicVocabularyResponse object
     */
    public static DynamicVocabularyResponse fromNiMError(JSONObject error)
    {
        logger.trace("DynamicVocabularyResponse fromNiMError: {}", () -> error.toString());

        logger.error("This method shouldn't be called since this command is not performed through NiM");
        return new DynamicVocabularyResponse(ResponseCode.createResponseError(ExternalResponseErrorCode.UPLOAD_DYNAMIC_VOCABULARY_GENERIC_ERROR));
    }

}
