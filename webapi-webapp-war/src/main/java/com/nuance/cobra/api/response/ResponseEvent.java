package com.nuance.cobra.api.response;

import com.fasterxml.jackson.annotation.JsonView;

public class ResponseEvent
{
    @JsonView(Views.Visible.class)
    private final String name;

    public ResponseEvent(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

}
