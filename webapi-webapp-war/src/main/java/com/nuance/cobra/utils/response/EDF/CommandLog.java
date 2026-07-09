package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.command.Command;
import com.nuance.cobra.session.Session;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;

public class CommandLog
{

    public static final String EDF_VERSION = "1";
    public static final String WAPI_APPLICATION_ID = "webapi";


//    @JsonProperty("ClientID")
//    public String clientId;
//
//    @JsonProperty("api")
//    public String API;
//
//    @JsonProperty("stepId")
//    public int stepId;
//
//    @JsonProperty("sessionId")
//    public String sessionId;
//
//    @JsonProperty("userInfo")
//    public UserInfo userInfo;
//
//    @JsonProperty("sensitivity")
//    public String sensitivity;

    @JsonProperty("requestHeader")
    public RequestHeader requestHeader;

    @JsonProperty("request")
    public RequestLog request;
//
//    @JsonProperty("statistics")
//    public Command.CommandStatistics statistics;
//
//    @JsonProperty("outcome")
//    public String status;
//
//    @JsonProperty("result")
//    public IResponse result;

    //will store information related to dumped audio file
//    @JsonProperty("data")
//    public String data = null;

    @JsonIgnore
    public Session session = null;


    /*
    To be used in getting path for EDF
     */
    public static CommandLog fromJson(String json) throws Exception
    {
        LogManager.getLogger(CommandLog.class).debug("CommandLogJson {}" , json);
        CommandLog commandLog = new CommandLog();
        RequestHeader requestHeader;
        RequestLog requestLog;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSONObject jsonObject = new JSONObject(json);
        requestHeader = mapper.readValue(jsonObject.getJSONObject("requestHeader").toString(),RequestHeader.class);

        requestLog = mapper.readValue(jsonObject.getJSONObject("request").toString(),RequestLog.class);


        commandLog.requestHeader = requestHeader;
        commandLog.request = requestLog;
        LogManager.getLogger(CommandLog.class).debug("CommandLog successfully deserialized : {} " , commandLog.toJson(false));
        return commandLog;
    }

    public String toJson(boolean prettyPrint) throws JsonProcessingException
    {
        if (prettyPrint)
        {
            return Command.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }
        else
        {
            return Command.getMapper().writer().writeValueAsString(this);
        }
    }

}
