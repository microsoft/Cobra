package com.nuance.cobra.api.response;


import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.request.RequestCommand;
import com.nuance.cobra.utils.CobraException;
import com.rits.cloning.Cloner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"type","sessionId","command","commands","result","event"})
@JsonIgnoreProperties({"errors"}) //ignore duplicates
public class Response
        implements IResponse, Cloneable
{
    private static final Logger logger = LogManager.getLogger(Response.class);

    @JsonView(Views.Visible.class)
    @JsonProperty("type")
    private final String type;

    @JsonView(Views.Visible.class)
    @JsonProperty("sessionId")
    private String sessionId;

    @JsonIgnore
    private final RequestCommand requestCommand;

    @JsonIgnore
    private final List<Map<String, Object>> requestCommands;

    @JsonIgnore
    private RequestCommand edfCopy = null;

    @JsonView(Views.Visible.class)
    @JsonProperty("command")
    private RequestCommand getCommand()
    {
        if (edfCopy == null)
        {
            return requestCommand;
        }
        else
        {
            return edfCopy;
        }
    }

//    @JsonView(Views.Visible.class)
    @JsonProperty("commands")
    private List<Map<String, Object>> getCommands()
    {
        return requestCommands; // TODO edfCopy?
    }

    @JsonView(Views.Visible.class)
    @JsonProperty("result")
    private ResponseResult result;

    @JsonView(Views.Visible.class)
    @JsonProperty("event")
    private final ResponseEvent event;

    @JsonProperty("fileName")
    public String fileName = null;

    private Response()
    {
        //For an invalid request, the type is always "result"
        type = "result";
        sessionId = null;
        requestCommand = null;
        requestCommands = null;
        result = null;
        event = null;
    }

    private Response(boolean isCommandChain)
    {
        if (isCommandChain)
        {
            type = "commandChainResult";
        }
        else
        {
            type = "result";
        }
        sessionId = null;
        requestCommand = null;
        requestCommands = null;
        result = null;
        event = null;
    }

    private Response(Request request, boolean isCommandChain)
    {
        if (isCommandChain)
        {
            type = "commandChainResult";
            requestCommand = null;
            requestCommands = request.requestCommands;
        }
        else
        {
            type = "result";           
            requestCommand = request.requestCommand;
            requestCommands = null;
        }
        sessionId = request.sessionId;
        result = null;
        event = null;
    }

    private Response(Request request, ResponseEvent event)
    {
        type = "event";
        sessionId = request.sessionId;
        requestCommand = request.requestCommand;
        requestCommands = null;
        result = null;
        this.event = event;
    }

    /**
     * Creates a Result Response object by extracting info from the originating command chain Request
     *
     * @param request
     *         the Request for which a Response is given
     * @param isSessionValid
     *         is the session valid
     * @return the Response object.
     */
    public static Response fromCommandChainRequest(Request request, boolean isSessionValid)
    {
        Response response = new Response(request, true);

        if(!isSessionValid)
        {
            response.setSessionId(null);
        }

        return response;
    }    

    /**
     * Creates a Result Response object by extracting info from the originating Request
     *
     * @param request
     *         the Request for which a Response is given
     * @param isSessionValid
     *         is the session valid
     * @return the Response object.
     */
    public static Response fromRequest(Request request, boolean isSessionValid)
    {
        Response response = new Response(request, false);

        if(!isSessionValid)
        {
            response.setSessionId(null);
        }

        return response;
    }

    /**
     * Creates a Result Response object by extracting info from the originating Request
     *
     * @param request
     *         the Request for which a Response is given
     *
     * @return the Response object.
     */
    public static Response fromRequest(Request request)
    {
        return Response.fromRequest(request, true);
    }

    /**
     * Creates an Event Response object by extracting info from the originating Request.
     *
     * @param request
     *         the Request for which a Response is given.
     * @param event
     *         the Event which occurred.
     *
     * @return the Response object.
     */
    public static Response eventFromRequest(Request request, ResponseEvent event)
    {
        return new Response(request, event);
    }

    public static Response fromInvalidRequest()
    {
        return new Response();
    }

    public static Response fromInvalidCommandChainRequest()
    {
        return new Response(true);
    }

    /**
     * Sets the result portion of the response
     * @param jsonResult
     *      Json formatted string containing the result portion of the response.
     */
    public void setResult(ResponseResult jsonResult)
    {
        result = jsonResult;
    }

    /**
     * Gets the result.
     *
     * @return
     */
    public ResponseResult getResult()
    {
        return result;
    }

    public String toJson() throws CobraException
    {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setSerializationInclusion(Include.NON_EMPTY);

        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        //Object to JSON in String
        String jsonInString;
        try
        {
            jsonInString = mapper.writerWithView(Views.Visible.class).writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            logger.error("Failed to convert response to json: "+ e.getMessage());
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.RESPONSE_SERIALIZE_ERROR));
        }
        
        return jsonInString;
    }

    @Override
    public List<ResponseCode> getErrors()
    {
        return this.getResult()
                   .getErrors();
    }

    @Override
    public Response clone()
    {

        Cloner cloner = new Cloner();
        return cloner.deepClone(this);
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public RequestCommand getEdfCopy() {
        edfCopy = new RequestCommand();
        edfCopy.id = new String(requestCommand.id);
        edfCopy.name = new String(requestCommand.name);
        if (requestCommand.parameters != null)
        {
            edfCopy.parameters = new HashMap<>(requestCommand.parameters);
        }
        else
        {
            edfCopy.parameters = new HashMap<>();
        }
        return edfCopy;
    }

    public void setEdfCopy(RequestCommand edfCopy)
    {
        this.edfCopy = edfCopy;
    }
}
