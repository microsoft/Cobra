package com.nuance.plugin.nmsp.v1;

import com.nuance.cobra.utils.config.HierarchicalConfigPropertyDefinition;
import com.nuance.plugin.audio.AudioEncoder;
import com.nuance.plugin.ncs.utils.HashType;
import com.nuance.plugin.v1.shareable.IConfigProperty;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PdxInterfaceConfig {
    private static final Logger logger = LogManager.getLogger();
    public static final int MAX_TRANSCRIPTIONS = 3;

    static final String PDX_INTERFACE_PLUGIN = "NCS NMSP Plugin";

    static BaseHierarchicalConfiguration result = null;

    public enum Property implements IConfigProperty
    {
        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        NCS_NMSP_RAW_SERVER("ncsNmspRawServer", "NCS NMSP configuration"),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        SSL_ENABLED("sslEnabled", NCS_NMSP_RAW_SERVER, true, "Whether SSL is enabled."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        SSL_KEYSTORE_PASSWORD("sslKeyStorePassword", NCS_NMSP_RAW_SERVER, "changeit", "The password for the key store."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        SSL_CERTIFICATE_PASSWORD("sslCertificatePassword", NCS_NMSP_RAW_SERVER, "changeit", "The password for the certificates key."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        SERVER_PORT("serverPort", NCS_NMSP_RAW_SERVER, 8911, "Port for NCS NMSP server protocol."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        DICTATION_TYPE_TO_TOPIC_MAP("dictationTypeToTopicMap", "A map for the NCS NMSP dictation types to a topic."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        CONTEXT_TAG_TO_TOPIC_MAP("contextTagToTopicMap", "A map for the NCS NMSP context tags to topics."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        CONFIDENCE_DYNAMIC_RANGE_TUNING("confidenceGlobalTuningPercentage", NCS_NMSP_RAW_SERVER, 0, "For some data-packs, the confidence returned by krypton or other newer recognition engines may be different then what the customer's client may expect. Use this key to tune the returned confidence. The percentage can be from -100 to +100."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        CONFIDENCE_DYNAMIC_RANGE_LANGUAGE_TUNING("confidenceTuningPercentageMap", NCS_NMSP_RAW_SERVER, null, "For some data-packs, the confidence returned by krypton or other newer recognition engines may be different then what the customer's client may expect. Use this map to tune the returned confidence per language. The percentage can be from -100 to +100."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        OVERWRITE_WORD_CONFIDENCE("overwriteWordConfidence", NCS_NMSP_RAW_SERVER, false, "Use the utterance confidence for returned word confidence. Workarounf for SBRE limitations."),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        HASH_SECRET("hashSecret", "Secret used to hash user and device id. Secret must be a byte[] encoded as a Base64 string.", "TWFya0cgc2F5cyB3ZSBzaG91bGQgaGFzaCBieSBkZWZhdWx0"),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        HASH_TYPE("hashType", "Hash method to use.", HashType.HASH_TYPE_NCS.getValue(), new Object[]{HashType.HASH_TYPE_NONE.getValue(), HashType.HASH_TYPE_SHA256.getValue(), HashType.HASH_TYPE_NCS.getValue()}),

        @Plugin(plugin=PDX_INTERFACE_PLUGIN)
        AUDIO_ENCODER_VBR(AudioEncoder.AUDIO_ENCODER_VBR_KEY, "Should NMSP specific audio encoders use VBR (Variable bit rate).", false);


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

        Property(String key, IConfigProperty parent, Object defaultValue, String description){
            definition = new HierarchicalConfigPropertyDefinition(key, parent, defaultValue, null, description);
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

    public static BaseHierarchicalConfiguration getPluginConfMap(CombinedConfiguration config) {
        if (result == null) {
            try {
                result = (BaseHierarchicalConfiguration) config.configurationAt(PdxInterfaceConfig.Property.NCS_NMSP_RAW_SERVER.getKey());
            } catch (ConfigurationRuntimeException e) {
                logger.info("Configurations for nmsp-raw-interface plugin not found. Using default values.");
                result = new BaseHierarchicalConfiguration();
            }

            if (result == null) {
                result = new BaseHierarchicalConfiguration();
            }

            setDefaultValues(result);
        }
        return result;
    }


    /**
     * Set default values in hierarchical config object, if values are not already present.
     */
    public static void setDefaultValues(BaseHierarchicalConfiguration config) {

        for (Property prop: Property.values()) {
            if (!config.containsKey(prop.getKey()) && prop.getDefaultValue() != null) {
                config.addProperty(prop.getKey(),
                        prop.getDefaultValue());
            }
        }
    }

}
