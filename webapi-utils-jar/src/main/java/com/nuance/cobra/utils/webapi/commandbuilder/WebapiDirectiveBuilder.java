package com.nuance.cobra.utils.webapi.commandbuilder;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class WebapiDirectiveBuilder extends WebapiCommandBuilder
{
    private final Logger logger;

    private final String id;
    private String sessionId;

    private final String directiveName;

    public WebapiDirectiveBuilder(String sessionId, String commandId, String commandName, String directiveName, Logger logger)
    {
        super("directive", commandName);
        this.sessionId = sessionId;
        this.id = commandId;
        this.directiveName = directiveName;
        this.logger = logger;
    }

    public JSONObject buildRequest()
    {
        JSONObject rc = new JSONObject();
        JSONObject command = new JSONObject();
        JSONObject directive = new JSONObject();

        rc.put("type", type);
        rc.put("command", command);
        rc.put("sessionId", sessionId);
        rc.put("directive", directive);

        command.put("name", name);
        command.put("id", id);

        directive.put("name", directiveName);

        logger.info("Built {}", rc);
        return rc;
    }

    public WebapiDirectiveBuilder sessionId(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }
}
