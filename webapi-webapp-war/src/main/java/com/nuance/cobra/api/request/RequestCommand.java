package com.nuance.cobra.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nuance.cobra.api.response.Views;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements a command sent by the client
 * It is populated by deserialization of the json sent into the object Request
 */
public class RequestCommand
{

    @JsonView(Views.Visible.class)
    public String name;

    @JsonView(Views.Visible.class)
    public String id;

    @JsonProperty("parameters")
    //DO NOT INITIALIZE THIS WITH NULL.
    //Fix the json by setting mapper.setSerializationInclusion(Include.NON_EMPTY);
    public Map<String, Object> parameters = new ConcurrentHashMap<>();

    /**
     * Creates a string to print out the RequestCommand object
     *
     * @return String
     */
    @Override
    public String toString()
    {
        return "\n---name: " + name +
               "\n---id: " + id +
               "\n" + "Parameters: " + ((parameters != null) ? Arrays.toString(parameters.entrySet()
                                                                                         .toArray()) : "null");
    }

    public JSONObject toJsonObject()
    {
        JSONObject jsonCommand = new JSONObject();
        jsonCommand.put("name", name);
        jsonCommand.put("id", id);

        JSONObject jsonParameters = new JSONObject();

        for (Map.Entry<String, Object> parameter : parameters.entrySet())
        {
            jsonParameters.put(parameter.getKey(), parameter.getValue());
        }

        jsonCommand.put("parameters", jsonParameters);

        return jsonCommand;
    }

    public String toJsonString()
    {
        return toJsonObject().toString(2);
    }

    public RequestCommand()
    {

    }

    public RequestCommand(Map<String, Object> requestCommand)
    {
        name = (String) requestCommand.get("name");
        id = (String) requestCommand.get("id");
        parameters = (Map<String, Object>) requestCommand.get("parameters");
    }
}

