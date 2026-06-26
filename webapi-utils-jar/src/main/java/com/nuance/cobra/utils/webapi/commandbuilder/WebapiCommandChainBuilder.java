package com.nuance.cobra.utils.webapi.commandbuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WebapiCommandChainBuilder {
    List<WebapiCommandBuilder> commands;

    public WebapiCommandChainBuilder() {
        commands = new ArrayList<>();
    }

    public void addCommand(WebapiCommandBuilder commandBuilder) {
        commands.add(commandBuilder);
    }

    public JSONObject buildRequest() {
        if(commands.isEmpty()) {
            return null;
        }
        JSONObject commandChain = new JSONObject();
        JSONArray commandsJson = new JSONArray();
        String sessionId = null;
        for(WebapiCommandBuilder commandBuilder : commands) {
            JSONObject commandJson = commandBuilder.buildRequest();
            sessionId = (String)commandJson.remove("sessionId");
            commandJson = commandJson.getJSONObject("command");
            commandsJson.put(commandJson);
        }
        commandChain.put("commands", commandsJson);
        commandChain.put("type", "commandChain");
        commandChain.put("sessionId", sessionId);

        return commandChain;
    }
}
