package com.nuance.plugin.kryptonws;

import com.nuance.cobra.utils.config.HierarchicalConfigPropertyDefinition;
import com.nuance.cobra.utils.config.ReverseOverrideCombiner;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.plugin.v1.shareable.IConfigProperty;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KryptonWsConfig
{

    private static final Logger logger = LogManager.getLogger(KryptonWsConfig.class);
    static final String KRYPTON_WS_PLUGIN = "KryptonWs Plugin";
    public static final String KRYPTON_WS_CONFIG = "kryptonWsDefaultConfig";

    public enum ValidationTypes {
        HASHCODE,
        JWS;        // jwt authentication. Standard claims will be validated,
    }

    public enum Property implements IConfigProperty
    {
        @Plugin(plugin = KRYPTON_WS_PLUGIN)
        KRYPTON_WS_CLOSE_WS_SESSION("kryptonWsCloseWsSession", "Boolean indicating if we close the session.", false),
        KRYPTON_WS_LOG_AUDIO("kryptonWsLogAudio", "Boolean indicating if we should log audio bytes", false),
        KRYPTON_WS_IGNORE_STRAY_AUDIO("kryptonWsIgnoreStrayAudio", "Boolean indicating if we should ignore stray audio bytes (or return errors)", true),
        KRYPTON_WS_ASYNC_WEBAPI_CALLS("kryptonWsAsyncWebapiCalls", "Boolean indicating if we should call webapi from another thread", true),
        KRYPTON_WS_DATAPACK_ONLY_RECOGNITION_PATTERN("kryptonWsDatapackOnlyRecognitionPattern", "The pattern to use to check if a Load uri should not actually be loaded", ".*/datapack-only/.*"),
        KRYPTON_WS_ENABLE_METRICS("kryptonWsEnableMetrics", "Boolean indicating if we should return metrics", false),
        KRYPTON_WS_THWART_NO_AUDIO_TIMER_SECONDS("kryptonWsThwartNoAudioTimerSeconds", "Time interval between sending a few silence packets to thwart hosted Krypton's no-audio timers when startRecognitionTimers is false. Used for long playing prompts while recognition is active (bargein) or active but muted (non-bargein prompt). A value less than 1 disables this feature.", 10),
        KRYPTON_WS_WEBSOCKET_MAX_IDLE_TIMEOUT("websocketMaxIdleTimeout", "The timeout for an idle websocket connection (in milliseconds). Must be set in plugin config (e.g. kryptonws.json) or WebAPI config (e.g. webapi.json). Value set in tenant config will be ignored.", 300000), // same as com.nuance.cobra.session.WebApiConfig.Property.WEBSOCKET_MAX_IDLE_TIMEOUT
        KRYPTON_WS_AUTH_TYPE("authType", "Auth to be used when authenticating with Cobra", ValidationTypes.HASHCODE.toString());

        private final HierarchicalConfigPropertyDefinition definition;

        Property(String key, String description, Object defaultVal){
            definition = new HierarchicalConfigPropertyDefinition(key, null, defaultVal, null, description);
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
            return new Object[0];
        }

        @Override
        public String getConstantName() {
            return name();
        }
    }

    public static WebApiJSONConfiguration getKryptonWsDefaultConfig()
    {
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        for(Property property : Property.values())
        {
            webApiJSONConfiguration.setProperty(property.getKey(), property.getDefaultValue());
        }
        return webApiJSONConfiguration;
    }

    //CombinedConfigurations need to be cleared to free memory e.g. ConfigHelpers.clearCombinedConfiguration().
    //Ok to leak for unit tests
    public static CombinedConfiguration getKryptonWsDefaultCombinedConfig()
    {
        logger.trace("new CombinedConfiguration");
        CombinedConfiguration cc = new CombinedConfiguration(new ReverseOverrideCombiner());
        cc.addConfiguration(getKryptonWsDefaultConfig(), KRYPTON_WS_CONFIG);
        return cc;
    }

}
