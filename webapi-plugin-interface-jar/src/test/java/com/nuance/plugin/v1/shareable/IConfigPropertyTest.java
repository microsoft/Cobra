package com.nuance.plugin.v1.shareable;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.*;

public class IConfigPropertyTest {


    /**
     * Dummy implementation of a config property, to cover the annotations code.
     */
    enum ConfigProperty implements IConfigProperty {

        @WebapiJsonOnly
        CONFIG_PARAM_WEBAPI_JSON,

        @Internal
        CONFIG_PARAM_INTERNAL,

        @Undocumented
        CONFIG_PARAM_UNDOCUMENTED,

        @Plugin(plugin = "Plugin1")
        CONFIG_PARAM_PI1,

        @Plugin(plugin = "Plugin1")
        @Plugin(plugin = "Plugin2")
        CONFIG_PARAM_PI2,

        @ConfigurationGroup(group = ConfigurationGroup.Value.ALARMS_SNMP)
        CONFIG_PARAM_CG1,

        @Backend(backend = Backend.Value.NLE)
        CONFIG_PARAM_BE1,

        @Backend(backend = Backend.Value.NLE)
        @Backend(backend = Backend.Value.NCS)
        CONFIG_PARAM_BE2;


        @Override
        public String getKey() {
            return null;
        }

        @Override
        public Object getDefaultValue() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Object[] getValidValues() {
            return new Object[0];
        }

        @Override
        public String getConstantName() {
            return null;
        }
    }

    @Test
    public void configPropertyTest() throws Exception {
        // verify default implementation for getParent()
        assertNull(ConfigProperty.CONFIG_PARAM_WEBAPI_JSON.getParent());

        // verify enum methods
        assertEquals("NVC-gRPC", ConfigProperty.Backend.Value.NVC_GRPC.getBackendName());
        assertEquals("Speech Recognition", ConfigProperty.ConfigurationGroup.Value.COMMAND_SPEECH_RECOGNITION.displayName());
        assertEquals("Alarms", ConfigProperty.ConfigurationGroup.Category.ALARMS.displayName());

        // verify annotations retrieval
        checkAnnotationPresent(IConfigProperty.WebapiJsonOnly.class, ConfigProperty.CONFIG_PARAM_WEBAPI_JSON);
        checkAnnotationPresent(IConfigProperty.Internal.class, ConfigProperty.CONFIG_PARAM_INTERNAL);
        checkAnnotationPresent(IConfigProperty.Undocumented.class, ConfigProperty.CONFIG_PARAM_UNDOCUMENTED);

        checkAnnotationPresent(IConfigProperty.ConfigurationGroup.class, ConfigProperty.CONFIG_PARAM_CG1);
        IConfigProperty.ConfigurationGroup configGroupAnnotation =
                retrieveAnnotation(IConfigProperty.ConfigurationGroup.class, ConfigProperty.CONFIG_PARAM_CG1);
        assertNotNull(configGroupAnnotation);
        assertEquals(IConfigProperty.ConfigurationGroup.Value.ALARMS_SNMP, configGroupAnnotation.group());
        assertEquals(IConfigProperty.ConfigurationGroup.Category.ALARMS, configGroupAnnotation.group().category());

        checkAnnotationPresent(IConfigProperty.Plugin.class, ConfigProperty.CONFIG_PARAM_PI1);
        IConfigProperty.Plugin pluginAnnotation =
                retrieveAnnotation(IConfigProperty.Plugin.class, ConfigProperty.CONFIG_PARAM_PI1);
        assertEquals("Plugin1", pluginAnnotation.plugin());

        checkAnnotationPresent(IConfigProperty.Plugins.class, ConfigProperty.CONFIG_PARAM_PI2);
        IConfigProperty.Plugins pluginsAnnotation =
                retrieveAnnotation(IConfigProperty.Plugins.class, ConfigProperty.CONFIG_PARAM_PI2);
        for (IConfigProperty.Plugin plugin : pluginsAnnotation.value()) {
            assertTrue(plugin.plugin().equals("Plugin1") || plugin.plugin().equals("Plugin2"));
        }

        checkAnnotationPresent(IConfigProperty.Backend.class, ConfigProperty.CONFIG_PARAM_BE1);
        IConfigProperty.Backend backendAnnotation =
                retrieveAnnotation(IConfigProperty.Backend.class, ConfigProperty.CONFIG_PARAM_BE1);
        assertEquals(IConfigProperty.Backend.Value.NLE, backendAnnotation.backend());

        checkAnnotationPresent(IConfigProperty.Backends.class, ConfigProperty.CONFIG_PARAM_BE2);
        IConfigProperty.Backends backendsAnnotations =
                retrieveAnnotation(IConfigProperty.Backends.class, ConfigProperty.CONFIG_PARAM_BE2);
        for (IConfigProperty.Backend backend : backendsAnnotations.value()) {
            assertTrue(backend.backend().equals(IConfigProperty.Backend.Value.NLE) ||
                    backend.backend().equals(IConfigProperty.Backend.Value.NCS));
        }
    }

    private void checkAnnotationPresent(Class<? extends Annotation> expectedAnnotationClass, ConfigProperty configProperty)
            throws NoSuchFieldException {

        Field enumField = ConfigProperty.class.getDeclaredField(configProperty.name());
        Annotation[] annotations = enumField.getAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == expectedAnnotationClass) {
                return;
            }
        }

        fail("annotation " + expectedAnnotationClass.getSimpleName() + " not found on " + configProperty);
    }

    <T extends Annotation> T retrieveAnnotation(Class<T> annotationClass, ConfigProperty configProperty) throws NoSuchFieldException {
        Field enumField = ConfigProperty.class.getDeclaredField(configProperty.name());
        Annotation[] annotations = enumField.getAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) {
                return (T) annotation;
            }
        }

        return null;
    }
}