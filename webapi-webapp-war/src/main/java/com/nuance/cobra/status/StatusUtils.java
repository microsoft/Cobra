package com.nuance.cobra.status;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.api.response.Views;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class StatusUtils
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<LinkedHashMap<String,ServiceData>> typeReference =
            new TypeReference<LinkedHashMap<String, ServiceData>>() {};

    static class ServiceData
    {
        @JsonView(Views.Visible.class)
        @JsonProperty(value="type",required=true)
        String type;

        @JsonView(Views.Visible.class)
        @JsonProperty(value="parameters",required=true)
        Map<String, Object> parameters;

        @JsonView(Views.Visible.class)
        @JsonProperty(value="required")
        boolean required;
    }


    Map<String, ServiceData> readServices(Logger logger)
    {
        try
        {
            final byte[] jsonData = Files.readAllBytes(Paths.get("config/services.json"));
            return objectMapper.readValue(jsonData, typeReference);
        }
        catch (IOException e)
        {
            logger.error("Failure reading services json");
            logger.debug(e, e);
            return Collections.emptyMap();
        }
    }

}
