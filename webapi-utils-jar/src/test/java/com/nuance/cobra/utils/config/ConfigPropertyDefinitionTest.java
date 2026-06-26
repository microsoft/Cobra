package com.nuance.cobra.utils.config;

import com.nuance.plugin.v1.shareable.IConfigProperty;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigPropertyDefinitionTest {

    static final String[] VALID_VALUES = new String[]{"value1", "value2"};

    static final String CONFIG_PARAM_1_NAME = "configParam";
    static final String CONFIG_PARAM_1_DEFAULT = VALID_VALUES[0];
    static final String CONFIG_PARAM_1_DESCRIPTION = "param 1 description";

    static final String CONFIG_PARAM_2_NAME = "configParam2";
    static final String CONFIG_PARAM_2_DEFAULT = VALID_VALUES[1];
    static final String CONFIG_PARAM_2_DESCRIPTION = "param 2 description";

    enum TestConfigProperty implements IConfigProperty {

        CONFIG_PARAM_1(CONFIG_PARAM_1_NAME, CONFIG_PARAM_1_DEFAULT, CONFIG_PARAM_1_DESCRIPTION),

        CONFIG_PARAM_2(CONFIG_PARAM_2_NAME, CONFIG_PARAM_2_DEFAULT,
                VALID_VALUES, CONFIG_PARAM_2_DESCRIPTION);

        ConfigPropertyDefinition propertyDefinition;

        TestConfigProperty(String key, Object defaultValue, String description) {
            this(key, defaultValue, null, description);
        }

        TestConfigProperty(String key, Object defaultValue, Object[] validValues, String description) {
            propertyDefinition = new ConfigPropertyDefinition(key, defaultValue, validValues, description);
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
        public String getConstantName() {
            return name();
        }
    }


    @Test
    public void configPropertyTest() {

        assertEquals(CONFIG_PARAM_1_NAME, TestConfigProperty.CONFIG_PARAM_1.getKey());
        assertEquals(CONFIG_PARAM_1_DEFAULT, TestConfigProperty.CONFIG_PARAM_1.getDefaultValue());
        assertNull(TestConfigProperty.CONFIG_PARAM_1.getValidValues());
        assertEquals(CONFIG_PARAM_1_DESCRIPTION, TestConfigProperty.CONFIG_PARAM_1.getDescription());
        assertEquals("CONFIG_PARAM_1", TestConfigProperty.CONFIG_PARAM_1.getConstantName());

        assertEquals(CONFIG_PARAM_2_NAME, TestConfigProperty.CONFIG_PARAM_2.getKey());
        assertEquals(CONFIG_PARAM_2_DEFAULT, TestConfigProperty.CONFIG_PARAM_2.getDefaultValue());
        assertArrayEquals(VALID_VALUES, TestConfigProperty.CONFIG_PARAM_2.getValidValues());
        assertEquals(CONFIG_PARAM_2_DESCRIPTION, TestConfigProperty.CONFIG_PARAM_2.getDescription());
        assertEquals("CONFIG_PARAM_2", TestConfigProperty.CONFIG_PARAM_2.getConstantName());
    }
}