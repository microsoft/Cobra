package com.nuance.cobra.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.api.response.Views;
import com.nuance.cobra.command.GetResourcesCommand.IResources;
import com.nuance.cobra.command.SpeechRecognitionCommand;
import com.nuance.cobra.command.SpeechSynthesisCommand;
import org.apache.commons.configuration2.CombinedConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ResourcesResponse extends ResponseResult {

    private Map<String, Object> resources;
    private CombinedConfiguration config;

    @JsonView(Views.Visible.class)
    @JsonProperty("resources")
    public Map<String, Object> getResources() {
        return this.resources;
    }

    public ResourcesResponse(CombinedConfiguration config) {
        super(ResponseStatus.SUCCESS, true);
        this.config = config;
        this.resources = new HashMap<>();
        getResourcesFromCommands();
    }

    private void getResourcesFromCommands(){
        addResource(SpeechSynthesisCommand.getResources(config));
        addResource(SpeechRecognitionCommand.getResources(config));
    }

    public void addResource(IResources resourcesToAdd){
        if(this.resources != null) {
            this.resources.put(resourcesToAdd.getName(), resourcesToAdd.getResourcesMap());
        }else{
            this.resources = new HashMap<>();
            addResource(resourcesToAdd);
        }
    }
}
