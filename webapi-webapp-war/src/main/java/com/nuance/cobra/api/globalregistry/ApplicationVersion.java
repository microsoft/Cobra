package com.nuance.cobra.api.globalregistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value Object for global registry "ApplicationVersion' element
 */
public class ApplicationVersion
{
    private Map<String, Grammar> grammars;

    public Map<String, Grammar> getGrammars()
    {
        if (grammars == null)
        {
            return new ConcurrentHashMap<String, Grammar>();
        }
        return grammars;
    }

    public void setGrammars(Map<String, Grammar> grammars)
    {
        this.grammars = grammars;
    }

    public void addGrammars(String key, Grammar grammar)
    {
        if (grammars == null)
        {
            grammars = new ConcurrentHashMap<String, Grammar>();
        }
        grammars.put(key, grammar);
    }

    public Grammar getGrammar(String key)
    {
        if (null == grammars)
        {
            return null;
        }
        return grammars.get(key);
    }

}