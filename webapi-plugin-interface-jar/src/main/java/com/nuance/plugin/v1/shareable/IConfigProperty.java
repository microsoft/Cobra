package com.nuance.plugin.v1.shareable;

import java.lang.annotation.*;

/**
 * Definition of a configuration property
 * Annotations to tag the properties specifying their usage and scope are provided.
 */
public interface IConfigProperty {

    static final String GENERAL = "General";

    /**
     * Used to tag config parameters (enum Property fields) that are only used internally to pass data , to the code processing it.
     * Should never be specified in the configuration files.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Internal {
    }

    /**
     * Used to tag config parameters that should not be documented (e.g. used for stubs and testing but not to used in production)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Undocumented {
    }

    /**
     * Used to tag config parameters that can be inlcuded in client request, but should not be set in configuration.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RequestParameter {
    }

    /**
     * Used to tag parameters that can only be used in the webapi server config (i.e. not in tenant config).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface WebapiJsonOnly {
    }

    /**
     * Used to tag parameters set in the tenant configuration
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface TenantConfig {
    }

    /**
     * Used to tag parameters that can only be used in tenant config (i.e. not in tenant config).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface TenantConfigOnly {
    }

    /**
     * Used to tag parameters that belong to specific plugins.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Repeatable(Plugins.class)
    public @interface Plugin {
        String plugin();
    }

    /**
     * Tagging with  multiple plugin values.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Plugins {
        Plugin[] value();
    }

    /**
     * Used to tag enum fields that are only used internally to pass data
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Repeatable(Backends.class)
    public @interface Backend {

        enum Value {
            NCS("NCS"),
            KRYPTON("Krypton"),
            KRYPTON_GRPC("Krypton-gRPC"),
            NVC_GRPC("NVC-gRPC"),
            NLUAAS_GRPC("NLU-gRPC"),
            FORGETME_GRPC("ForgetMe-gRPC"),
            NLE("NLE"),
            NLU_CACHE("NLU-CACHE"),
            MIX("MIX");

            Value(String backendName) {
                this.backendName = backendName;
            }

            public String getBackendName() {
                return backendName;
            }

            private String backendName;
        }

        Value backend();
    }

    /**
     * Tagging with multiple backend values.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Backends {
        Backend[] value();
    }

    /**
     * Additional tags to identify which group a parameter belongs to.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigurationGroup {

        enum Category {
            REPORTING("Reporting"),
            ALARMS("Alarms"),
            AUTHENTICATION("Authentication"),
            COMMANDS("Commands"),
            GENERAL("Configuration"); // catch-all category in configuration document.

            Category(String displayNameValue) {
                this.displayNameValue = displayNameValue;
            }

            public String displayName() {
                return  displayNameValue;
            }

            private String displayNameValue;
        }

        enum Value {
            GENERAL_CONFIG(GENERAL),
            REPORTING_GENERAL(GENERAL, Category.REPORTING),
            REPORTING_EDF("EDF", Category.REPORTING),
            REPORTING_EVENTHUB("EventHub", Category.REPORTING),
            REPORTING_FLUENTD("FluentD", Category.REPORTING),
            REPORTING_SECURITY("Security", Category.REPORTING),
            ALARMS_GENERAL(GENERAL, Category.ALARMS),
            ALARMS_HTTP("HTTP", Category.ALARMS),
            ALARMS_SNMP("SNMP", Category.ALARMS),
            AUTHENTICATION_GENERAL(GENERAL, Category.AUTHENTICATION),
            AUTHENTICATION_BASIC("Basic", Category.AUTHENTICATION),
            AUTHENTICATION_JWT("JWT", Category.AUTHENTICATION),
            CORE_TECH_SAAS("Mix"),
            GLOBAL_REGISTRY("Global Registry"),
            COMMAND_GENERAL(GENERAL, Category.COMMANDS),
            COMMAND_SPEECH_SYNTHESIS("Speech Synthesis", Category.COMMANDS),
            COMMAND_SPEECH_RECOGNITION("Speech Recognition", Category.COMMANDS),
            COMMAND_START_SESSION("Start Session", Category.COMMANDS),
            COMMAND_INTERPRETATION("Interpretation", Category.COMMANDS),
            COMMAND_USER_RESET("UserReset", Category.COMMANDS),
            COMMAND_UPLOAD_DYNAMIC_VOCABULARY("Upload Dynamic Vocabularies", Category.COMMANDS),
            DEBUGGING("Debugging"),
            WEBAPI_WS_INTERFACE("WebAPI Websocket Interface"),
            TENANT_CONFIGURATION_FILES("Tenant Configuration Files",
                    "These parameters are used to configure how the tenant configuration files are retrieved. They can be read from disk or fetched from an HTTP server."),
            CACHING_HTTP_CLIENT("Caching HTTP Client"),
            PROMETHEUS_METRICS("Prometheus Metrics"),
            NLPS("NLPS"),
            KEY_VAULT("Key vault"),
            AZURE("Azure");

            Value(String displayName) {
                this(displayName, Category.GENERAL,null);
            }


            Value(String displayName, Category category) {
                this(displayName, category, null);
            }

            Value(String displayName, String description) {
                this(displayName, Category.GENERAL, description);
            }

            Value(String displayName, Category category, String description) {
                this.displayNameValue = displayName;
                this.categoryValue = category;
                this.descriptionValue = description;
            }

            public Category category() {
                return categoryValue;
            }

            public String displayName() {
                return displayNameValue;
            }

            public String description() {
                return descriptionValue;
            }

            private String descriptionValue;
            private String displayNameValue;
            private Category categoryValue;
        }

        Value group();
    }


    /**
     * Returns the key used to retrieve the property value in the configuration.
     * @return key
     */
    String getKey();

    /**
     * Returns the default property value. Return null if no default value exist (mandatory parameter).
     * @return default value
     */
    Object getDefaultValue();

    /**
     * Returns a description of the property. This will be used int he generated server configuration document.
     * @return description
     */
    String getDescription();

    /**
     * Returns the list of possible values
     * @return possible values
     */
    Object[] getValidValues();

    /**
     * return the parent for hierarchical configurations. By default returns null (used by flat configurations.
     */
    default IConfigProperty getParent() { return null; }

    /**
     * Returns the enum constant name defined for this property
     */
    String getConstantName();
}
