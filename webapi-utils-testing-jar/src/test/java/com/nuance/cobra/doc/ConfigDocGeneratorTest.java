package com.nuance.cobra.doc;

import com.nuance.cobra.utils.config.HierarchicalConfigPropertyDefinition;
import com.nuance.plugin.v1.shareable.IConfigProperty;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.nio.file.*;

import static org.junit.Assert.*;

public class ConfigDocGeneratorTest {

    static final String CONFIG_PARAM_PARENT_NAME = "configParamParent";

    static final String CONFIG_PARAM_A1_NAME = "configParamA1";
    static final int CONFIG_PARAM_A1_DEFAULT_VALUE = 4567;

    static final String CONFIG_PARAM_CG1_NAME = "configParamCG1";
    static final String CONFIG_PARAM_CG1_DEFAULT_VALUE = "defaultValue";
    static final String[] CONFIG_PARAM_CG1_VALID_VALUES = new String[]{CONFIG_PARAM_CG1_DEFAULT_VALUE, "otherValue"};

    static final String CONFIG_PARAM_BE1_NAME = "configParamBE1";
    static final String CONFIG_PARAM_BE1_DEFAULT_VALUE = "defaultValue";

    static final String CONFIG_PARAM_PI1_NAME = "configParamPI1";
    static final String CONFIG_PARAM_PI1_DEFAULT_VALUE = "defaultValue";


    enum ConfigProperties implements IConfigProperty {

        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        CONFIG_PARAM_PARENT(CONFIG_PARAM_PARENT_NAME, null, CONFIG_PARAM_PARENT_NAME + " description"),

        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        CONFIG_PARAM_A1(CONFIG_PARAM_A1_NAME, CONFIG_PARAM_PARENT, CONFIG_PARAM_A1_DEFAULT_VALUE, CONFIG_PARAM_A1_NAME + " description"),

        @Plugin(plugin = "My Plugin")
        CONFIG_PARAM_PI1(CONFIG_PARAM_PI1_NAME, CONFIG_PARAM_CG1_DEFAULT_VALUE,
                CONFIG_PARAM_PI1_NAME + " description"),

        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        CONFIG_PARAM_CG1(CONFIG_PARAM_CG1_NAME, CONFIG_PARAM_CG1_DEFAULT_VALUE, CONFIG_PARAM_CG1_VALID_VALUES,
                CONFIG_PARAM_CG1_NAME + " description"),

        @Backend(backend = Backend.Value.NLE)
        CONFIG_PARAM_BE1(CONFIG_PARAM_BE1_NAME, CONFIG_PARAM_BE1_DEFAULT_VALUE, CONFIG_PARAM_BE1_NAME + " description");


        private final HierarchicalConfigPropertyDefinition definition;

        ConfigProperties(String key, Object defaultValue, String description){
            this(key, defaultValue, null, description);
        }

        ConfigProperties(String key, Object defaultValue, Object[] validValues, String description){
            definition = new HierarchicalConfigPropertyDefinition(key, null, defaultValue, validValues, description);
        }

        ConfigProperties(String key, IConfigProperty parent, Object defaultValue, String description){
            definition = new HierarchicalConfigPropertyDefinition(key, parent, defaultValue, null, description);
        }

        @Override
        public String getKey() {
            return definition.getKey();
        }

        @Override
        public Object getDefaultValue() {
            return definition.getDefaultValue();
        }

        @Override
        public String getDescription() {
            return definition.getDescription();
        }

        @Override
        public Object[] getValidValues() {
            return definition.getValidValues();
        }

        @Override
        public IConfigProperty getParent() {
            return definition.getParent();
        }

        @Override
        public String getConstantName() {
            return name();
        }
    }


    @Test
    public void testDocGeneratorMarkdown() throws Exception {
        ConfigDocGenerator docGenerator = new ConfigDocGenerator(ConfigProperties.class);

        docGenerator.generateMarkdownDocs();

        String generatedOutput = Files.readString(
                Path.of(docGenerator.getOutputPath().toString(), "alarms.md"));
        assertTrue(generatedOutput.contains(CONFIG_PARAM_CG1_NAME));
        assertTrue(generatedOutput.contains(CONFIG_PARAM_CG1_DEFAULT_VALUE));

        System.out.println(generatedOutput);
    }
}