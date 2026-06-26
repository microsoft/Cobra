package com.nuance.cobra.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.utils.CobraException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @api {display_none} display_none Generic result errors
 * @apiName ResponseResult 
 * @apiGroup Errors
 * @apiVersion ${project.version}
 * @apiDescription The list of result responses that failed. These response
 * are generic in the sense they can happen for many commands
 *
 * @apiErrorExample {json} Parsing error
 *
 * {
 *   "type": "result",
 *   "result": {
 *     "final": true,
 *     "status": "error",
 *     "errors": [
 *       {
 *         "code": 1100,
 *         "message": "Error parsing the request: Unterminated string at 20 [character 0 line 3]."
 *       }
 *     ]
 *   }
 * }
 *
 * @apiErrorExample {json} Invalid session
 *
 * {
 *   "type": "result",
 *   "sessionId": "5552eb3-b619-4abb-9066-3b11a4e4746a",
 *   "command": {
 *     "name": "speechSynthesis",
 *     "id": "1"
 *   },
 *   "result": {
 *     "final": true,
 *     "status": "error",
 *     "errors": [
 *       {
 *         "code": 1200,
 *         "message": "No session was found matching the identifier provided."
 *       }
 *     ]
 *   }
 * }
 *
 *
 * @apiErrorExample {json} Directive not allowed
 *
 * {
 *   "type": "result",
 *   "sessionId": "16eb0314-d490-4347-97ed-0024c66272b1",
 *   "command": {
 *     "name": "speechSynthesis",
 *     "id": "1"
 *   },
 *   "result": {
 *     "final": true,
 *     "status": "error",
 *     "errors": [
 *       {
 *         "code": 1204,
 *         "message": "Cannot execute directive."
 *       }
 *     ]
 *   }
 * }
 *
 */

public class ResponseResult
        implements IResponse
{

    public enum ResponseStatus
    {

        SUCCESS("success", "200"),
        NO_INPUT("noInput", "400"),
        NO_MATCH("noMatch", "230"),
        CANCELLED("cancelled", "204"),
        ERROR("error", "varies");

        private final String name;
        private final String httpEquivalent;

        ResponseStatus(String name, String httpEquivalent) {
            this.name = name;
            this.httpEquivalent = httpEquivalent;
        }

        @JsonValue
        public String getName()
        {
            return name;
        }

        public static ResponseStatus fromString(String name)
        {
            for (ResponseStatus status : ResponseStatus.values()) {
                if (status.name.equalsIgnoreCase(name)) {
                    return status;
                }
            }
            return ERROR;
        }

        public String getHttpEquivalent() {
            return httpEquivalent;
        }
    }

    private static final Logger logger = LogManager.getLogger(ResponseResult.class);
    private static final int MAX_ERRORS = 5;

    @JsonView(Views.Visible.class)
    @JsonProperty("status")
    private final ResponseStatus status;

    @JsonView(Views.Visible.class)
    @JsonProperty("nextCommand")
    private String nextCommand;

    /**
     * final flag part of all responses, always true for a startsession
     */
    @JsonView(Views.Visible.class)
    @JsonProperty("final")
    private final boolean finalResponse;

    @JsonView(Views.Visible.class)
    @JsonProperty("statistics")
    private ResponseStatistics statistics;

    @JsonView(Views.Visible.class)
    @JsonProperty("errors")
    private List<ResponseCode> errors = null;

    @JsonView(Views.Visible.class)
    @JsonProperty("fileName")
    public String fileName = null;

    @JsonIgnore
    private boolean isFirstIntermediateResponse = false;

    public void setStatistics(ResponseStatistics statistics)
    {
        this.statistics = statistics;
    }

    public ResponseResult()
    {
        this(ResponseStatus.SUCCESS, true);
    }

    public ResponseResult(String nextCommand)
    {
        this(ResponseStatus.SUCCESS, false);
        this.nextCommand = nextCommand;
    }

    public ResponseResult(ResponseStatus status, boolean finalResponse)
    {
        this.finalResponse = finalResponse;
        this.status = status;
    }

    public ResponseResult(ResponseCode... errors) {
        this(true, errors);
    }

    public ResponseResult(List<ResponseCode> errors) {
        this(true, errors);
    }

    public ResponseResult(boolean isFinal, ResponseCode... errors)
    {
        finalResponse = isFinal;
        status = ResponseStatus.ERROR;
        final List<ResponseCode> list = Arrays.asList(errors);
        this.errors = list.subList(0, Math.min(list.size(), MAX_ERRORS));
    }

    public ResponseResult(boolean isFinal, List<ResponseCode> errorsList)
    {
        finalResponse = isFinal;
        status = ResponseStatus.ERROR;
        this.errors = errorsList.subList(0, Math.min(errorsList.size(), MAX_ERRORS));
    }

    public ResponseStatus getStatus()
    {
        return status;
    }

    public boolean isFinal()
    {
        return finalResponse;
    }

    public String toPrettyJson() throws CobraException
    {
        return toJson(true);
    }

    public String toJson() throws CobraException
    {
        return toJson(false);
    }

    private String toJson(boolean prettyPrint) throws CobraException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);

        //Object to JSON in String
        String jsonInString = "";
        try
        {
            if (prettyPrint)
            {
                jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            }
            else
            {
                jsonInString = mapper.writeValueAsString(this);
            }
        }
        catch (JsonProcessingException e)
        {
            logger.error("Failed to convert response to json: " + e.getMessage());
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.RESPONSE_SERIALIZE_ERROR));
        }

        return jsonInString;
    }

    public List<ResponseCode> getErrors()
    {
        return errors;
    }

    @Override
    public boolean equals(Object object) {
        // Self check
        if (this == object)
        {
            return true;
        }

        // Null check
        if (object == null)
        {
            return false;
        }

        // Type check and cast
        if (getClass() != object.getClass())
        {
            return false;
        }

        // Field comparisons
        ResponseResult response = (ResponseResult) object;
        boolean errorsEqual = (this.errors != null) ? this.errors.equals(response.errors) : true;
        return (this.status == response.status && this.finalResponse == response.finalResponse && errorsEqual);
    }

    @Override
    public int hashCode(){
        return Objects.hash(status, nextCommand, finalResponse, statistics, 
                            errors, fileName, isFirstIntermediateResponse);
    }

    public void setIsFirstIntermediateResponse(boolean firstIntermediateResponse)
    {
        this.isFirstIntermediateResponse = firstIntermediateResponse;
    }

    public boolean getIsFirstIntermediateResponse()
    {
        return this.isFirstIntermediateResponse;
    }
}
