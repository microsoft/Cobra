package com.nuance.cobra.utils.config;

import com.nuance.plugin.v1.shareable.IConfigProperty;

public class HierarchicalConfigPropertyDefinition extends ConfigPropertyDefinition {

    private IConfigProperty parent;


    public HierarchicalConfigPropertyDefinition(String key, IConfigProperty parent, Object defaultValue,
                                                Object[] validValues, String description) {
        super(key, defaultValue, validValues, description);
        this.parent = parent;
    }

    public IConfigProperty getParent() {
        return parent;
    }
}
