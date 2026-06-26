package com.nuance.plugin.ncs7;

import com.nuance.cobra.utils.config.HierarchicalConfigPropertyDefinition;
import com.nuance.plugin.ncs.utils.HashType;
import com.nuance.plugin.v1.shareable.IConfigProperty;

public class Ncs7InterfaceConfig {

    static final String NCS7_INTERFACE_PLUGIN = "NCS7 WS Plugin";

    public enum ValidationTypes {
        HASHCODE,
        JWS;        // jwt authentication. Standard claims will be validated,
    }

    public enum Property implements IConfigProperty
    {
        @Plugin(plugin=NCS7_INTERFACE_PLUGIN)
        DICTATION_TYPE_TO_TOPIC_MAP("dictationTypeToTopicMap", "NCS NMSP configuration"),

        @Plugin(plugin=NCS7_INTERFACE_PLUGIN)
        CONTEXT_TAG_TO_TOPIC_MAP("contextTagToTopicMap", "NCS NMSP configuration"),

        @Plugin(plugin=NCS7_INTERFACE_PLUGIN)
        INSERT_AUDIO_MESSAGE("insertAudioMessage", "Whether the plugin should sent an audio message before sending an audio bytes packet.", false),

        @Plugin(plugin=NCS7_INTERFACE_PLUGIN)
        HASH_SECRET("hashSecret", "Secret used to hash user and device id. Secret must be a byte[] encoded as a Base64 string.", "TWFya0cgc2F5cyB3ZSBzaG91bGQgaGFzaCBieSBkZWZhdWx0"),
        @Plugin(plugin=NCS7_INTERFACE_PLUGIN)
        HASH_TYPE("hashType", "Hash method to use.", HashType.HASH_TYPE_NCS.getValue(), new Object[]{HashType.HASH_TYPE_NONE.getValue(), HashType.HASH_TYPE_SHA256.getValue(), HashType.HASH_TYPE_NCS.getValue()}),

        @Plugin(plugin=NCS7_INTERFACE_PLUGIN)
        AUTH_TYPE("authType", "Auth to be used when authenticating with Cobra", ValidationTypes.HASHCODE.toString());

        private final HierarchicalConfigPropertyDefinition definition;

        Property(String key, String description) {
            definition = new HierarchicalConfigPropertyDefinition(key, null, null, null, description);
        }

        Property(String key, String description, Object defaultValue) {
            definition = new HierarchicalConfigPropertyDefinition(key, null, defaultValue, null, description);
        }

        Property(String key, String description, Object defaultValue, Object[] validValues) {
            definition = new HierarchicalConfigPropertyDefinition(key, null, defaultValue, validValues, description);
        }


        @Override
        public String getDescription() {
            return definition.getDescription();
        }

        @Override
        public String getKey() {
            return definition.getKey();
        }

        @Override
        public Object[] getValidValues() {
            return definition.getValidValues();
        }

        @Override
        public Object getDefaultValue() {
            return definition.getDefaultValue();
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

}
