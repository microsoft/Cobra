package com.nuance.nmsp.servlet.nmas;

public class MyQueryRetry
{
    private final String name;
    private final int cause;
    private final String prompt;

    public MyQueryRetry(String name, int cause, String prompt)
    {
        this.name = name;
        this.cause = cause;
        this.prompt = prompt;
    }

    public String getName()
    {
        return name;
    }

    public int getCause()
    {
        return cause;
    }

    public String getPrompt()
    {
        return prompt;
    }
}
