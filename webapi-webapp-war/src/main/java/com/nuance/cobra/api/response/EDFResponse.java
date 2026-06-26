package com.nuance.cobra.api.response;

import com.nuance.cobra.utils.response.EDF.CommandLog;

/**
Interface that denotes a method that returns a formatted Command response to be sent to EDF. Classes that implement
this interface will define the formatLog method to return a CommandLog object that can be written as a JsonString. The
object itself will result in a Json string that matches the format outlined in the Web API EDF Response specification.
 */
public interface EDFResponse {

    /**
    Returns a CommandLog object representing a formatted EDF log for the calling Command.
    @param response: IResponse
     @return a CommandLog object representing a formatted EDF Json response
     */
    public CommandLog formatLog(IResponse response);

}
