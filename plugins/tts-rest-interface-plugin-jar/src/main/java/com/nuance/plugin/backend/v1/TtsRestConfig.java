package com.nuance.plugin.backend.v1;

import com.nuance.cobra.utils.config.HierarchicalConfigPropertyDefinition;
import com.nuance.plugin.v1.shareable.IConfigProperty;

public class TtsRestConfig
{

    static final String TTS_REST_PLUGIN = "TtsRest Plugin";
    public static final String TTS_REST_CONFIG = "ttsRestDefaultConfig";

    public enum Property implements IConfigProperty
    {
        @Plugin(plugin = TTS_REST_PLUGIN)
        TTS_REST_ENCAPSULATE_SIMPLE_TEXT("ttsRestEncapsulateSimpleText", "Boolean indicating if simple text should be encapsulated in ssml.", false);

        private final HierarchicalConfigPropertyDefinition definition;

        Property(String key, String description, Object defaultVal){
            definition = new HierarchicalConfigPropertyDefinition(key, null, defaultVal, null, description);
        }

        @Override
        public String getKey() {
            return definition.getKey(); // Returns the config field name
        }

        @Override
        public Object getDefaultValue() {
            return definition.getDefaultValue(); // Returns the default value, if one is defined
        }

        @Override
        public String getDescription() {
            return definition.getDescription(); // Returns the description
        }

        @Override
        public Object[] getValidValues() {
            return definition.getValidValues(); // Returns a list of valid values
        }

        @Override
        public String getConstantName() {
            return name(); // Gets the name of the config in the code
        }
    }

}
