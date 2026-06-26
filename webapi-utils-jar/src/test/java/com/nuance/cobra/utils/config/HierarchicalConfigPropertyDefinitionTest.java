package com.nuance.cobra.utils.config;

import com.nuance.plugin.v1.shareable.IConfigProperty;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HierarchicalConfigPropertyDefinitionTest {


    static final String CONFIG_PARAM_PARENT_NAME = "parent";

    static final String CONFIG_PARAM_1_NAME = "configParam";
    static final String CONFIG_PARAM_1_DEFAULT = "default";
    static final String CONFIG_PARAM_1_DESCRIPTION = "param 1 description";

    enum TestConfigProperty implements IConfigProperty {

        CONFIG_PARAM_PARENT(CONFIG_PARAM_PARENT_NAME, CONFIG_PARAM_1_DEFAULT, CONFIG_PARAM_1_DESCRIPTION),

        CONFIG_PARAM_1(CONFIG_PARAM_1_NAME, CONFIG_PARAM_PARENT, CONFIG_PARAM_1_DEFAULT, CONFIG_PARAM_1_DESCRIPTION);

        HierarchicalConfigPropertyDefinition propertyDefinition;

        TestConfigProperty(String key, Object defaultValue, String description) {
            propertyDefinition = new HierarchicalConfigPropertyDefinition(key, null, defaultValue, null, description);
        }

        TestConfigProperty(String key, IConfigProperty parent, Object defaultValue, String description) {
            propertyDefinition = new HierarchicalConfigPropertyDefinition(key, parent, defaultValue, null, description);
        }

        @Override
        public String getKey() {
            return propertyDefinition.getKey();
        }

        @Override
        public Object getDefaultValue() {
            return propertyDefinition.getDefaultValue();
        }

        @Override
        public String getDescription() {
            return propertyDefinition.getDescription();
        }

        @Override
        public Object[] getValidValues() {
            return propertyDefinition.getValidValues();
        }

        @Override
        public IConfigProperty getParent() {
            return propertyDefinition.getParent();
        }

        @Override
        public String getConstantName() {
            return name();
        }
    }

    @Test
    public void configPropertyTest() {

        assertNull(TestConfigProperty.CONFIG_PARAM_PARENT.getParent());

        assertEquals(CONFIG_PARAM_1_NAME, TestConfigProperty.CONFIG_PARAM_1.getKey());
        assertEquals(CONFIG_PARAM_1_DEFAULT, TestConfigProperty.CONFIG_PARAM_1.getDefaultValue());
        assertNull(TestConfigProperty.CONFIG_PARAM_1.getValidValues());
        assertEquals(CONFIG_PARAM_1_DESCRIPTION, TestConfigProperty.CONFIG_PARAM_1.getDescription());
        assertEquals("CONFIG_PARAM_1", TestConfigProperty.CONFIG_PARAM_1.getConstantName());

        assertEquals(TestConfigProperty.CONFIG_PARAM_PARENT.getDescription(), TestConfigProperty.CONFIG_PARAM_1.getDescription());
    }
}