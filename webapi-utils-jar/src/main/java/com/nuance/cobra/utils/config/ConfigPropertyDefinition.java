package com.nuance.cobra.utils.config;

public class ConfigPropertyDefinition {

    private final String key;
    private final Object defaultValue;
    private final String description;
    private final Object[] validValues;

    public ConfigPropertyDefinition(String key, Object defaultValue, Object[] validValues, String description){
        this.key = key;
        this.defaultValue = defaultValue;
        this.description = description;
        this.validValues = validValues;
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public Object[] getValidValues() {
        return validValues;
    }
}
