package com.nuance.cobra.doc;

import com.nuance.cobra.utils.errors.RestPluginsError;
import com.nuance.plugin.v1.shareable.IConfigProperty;
import org.apache.commons.lang3.text.StrBuilder;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.fail;

public class ConfigDocGenerator extends DocGenerator {
    public final static String UNDOCUMENTED_PARAMETER = "undocumented";

    public static final String API_DOCS_FILENAME = "ConfigDocs.java";

    private Class<? extends Enum<? extends IConfigProperty>> configEnumClass;
    private Map<IConfigProperty, ParameterInfo>  annotationProperties;

    static final String WEBAPI_PREFIX = "webapi.json: ";

    /**
     * Constructor
     * @param configEnumClass Enum with the config properties
     */
    public ConfigDocGenerator(Class<? extends Enum<? extends IConfigProperty>> configEnumClass) {
        this.configEnumClass = configEnumClass;
    }

    /**
     * Information about a parameter, its property along with annotations retrieved.
     */
    protected static class ParameterInfo
    {
        IConfigProperty property;
        ParameterAnnotations annotations;
        boolean handled; // flag indicating that this parameter has been handled. Will be used to generate the remaining parameter list.

        ParameterInfo(IConfigProperty property, ParameterAnnotations annotations) {
            this.property = property;
            this.annotations = annotations;
        }
    }

    /**
     * parsed parameter annotations.
     */
    public static class ParameterAnnotations
    {
        boolean webapiJsonOnly = false;
        boolean internal = false;
        boolean requestParameter = false;
        boolean undocumented = false;
        IConfigProperty.ConfigurationGroup.Value configurationGroup = null;

        Set<IConfigProperty.Backend.Value> backends = new HashSet<>();
        Set<String> plugins = new HashSet<>();
    }


    @Override
    protected void generateDocs() throws Exception
    {
        StrBuilder outputStr = new StrBuilder();

        List<ParameterInfo> propertiesInfo = retrieveParametersInfo();

        // Note: to control the order in which the generated sections appear in the generated document is based on the
        // index.html.md file, not the order in which the sections are generated.

        // The order in which the sections are generated (do not confuse with the order in the generated document, see comment
        // above) is relevant, as parameters are marked as handled once selected.
        generateBackendsSection(propertiesInfo);
        generatePluginsSection(propertiesInfo);

        generateConfigurationGroupsSections(propertiesInfo);
        generateRemainingParametersSection(propertiesInfo);

        generateTroubleshootSection();
    }


    /**
     * Generates a section for each of the configuration group, grouped by category.
     */
    private void generateConfigurationGroupsSections(List<ParameterInfo> parameterInfos) throws IOException {
        Map<String, StrBuilder> categories = new HashMap<>();
        for (IConfigProperty.ConfigurationGroup.Category category : IConfigProperty.ConfigurationGroup.Category.values()) {
            StrBuilder tmp = new StrBuilder();
            generateMarkdownHeader(category.displayName(), tmp);
            categories.put(category.displayName(), tmp);
        }

        for (IConfigProperty.ConfigurationGroup.Value configurationGroup : IConfigProperty.ConfigurationGroup.Value.values()) {

            boolean paramFoundForGroup = false;
            for (ParameterInfo parameterInfo : parameterInfos) {
                StrBuilder outputStr = categories.get(configurationGroup.category().displayName());

                if (configurationGroup.equals(parameterInfo.annotations.configurationGroup)) {
                    if (!paramFoundForGroup) {
                        // first param found for this backend, generate the section header
                        generateConfigSectionHeader(configurationGroup.category().displayName(),
                                configurationGroup.displayName(),
                                generateConfigurationSectionDescription(configurationGroup), outputStr);
                        paramFoundForGroup = true;
                    }

                    generateParameterDocumentation(parameterInfo, outputStr);
                }
            }
        }

        categories.forEach((k, v) -> {
            try {
                createMarkdownFile(k.toLowerCase() + ".md", v);
            } catch (IOException e) {
                fail("Could not create markdown file");
            }
        });
    }

    private String generateConfigurationSectionDescription(IConfigProperty.ConfigurationGroup.Value configurationGroup) {
        // Returns the group description, if defined
        if (configurationGroup.description() != null) {
            return configurationGroup.description();
        }

        // Fallback: generate a default boring description
        return configurationGroup.displayName() + " parameters.";
    }


    /**
     * Generates a plugin section for each of the plugins for which at least one backend annotation is found. If the same parameter is applicable to
     * multiple backends it will be repeated in each section.
     */
    private void generatePluginsSection(List<ParameterInfo> parameterInfos) throws IOException {

        Set<String> allPlugins = new HashSet<>();

        // Collects plugins from all parameters
        for (ParameterInfo parameterInfo : parameterInfos) {
            allPlugins.addAll(parameterInfo.annotations.plugins);
        }

        StrBuilder outputStr = new StrBuilder();
        generateMarkdownHeader("Plugins", outputStr);

        // Generate a section for each plugin found
        for (String plugin : allPlugins) {

            generateConfigSectionHeader("Plugins",
                    plugin, plugin + " plugin parameters.", outputStr);

            for (ParameterInfo parameterInfo : parameterInfos) {
                if (parameterInfo.annotations.plugins.contains(plugin)) {
                    // first param found for this backend, generate the section header
                    generateParameterDocumentation(parameterInfo, false, outputStr);
                }
            }
        }

        createMarkdownFile("plugins.md", outputStr);
    }


    /**
     * Generates a backend section for each of the backends for which at least one backend annotation is found. If the same parameter is applicable to
     * multiple backends it will be repeated in each section.
     */
    private void generateBackendsSection(List<ParameterInfo> parameterInfos) throws IOException {
        StrBuilder outputStr = new StrBuilder();
        generateMarkdownHeader("Backends", outputStr);

        for (IConfigProperty.Backend.Value backendValue : IConfigProperty.Backend.Value.values()) {
            boolean paramFoundForBackend = false;
            for (ParameterInfo parameterInfo : parameterInfos) {
                if (parameterInfo.annotations.backends.contains(backendValue)) {
                    if (!paramFoundForBackend) {
                        // first param found for this backend, generate the section header
                        generateConfigSectionHeader("Backends",
                                backendValue.getBackendName(),
                                backendValue.getBackendName() + " parameters.", outputStr);
                        paramFoundForBackend = true;
                    }

                    generateParameterDocumentation(parameterInfo, false, outputStr);
                }
            }
        }

        createMarkdownFile("backends.md", outputStr);
    }


    /**
     * Calls generateParameterDocumentation with default values.
     */
    private void generateParameterDocumentation(ParameterInfo parameterInfo, StrBuilder outputStr) {
        generateParameterDocumentation(parameterInfo, true, outputStr);
    }


    /**
     * Returns whether or not a parameter documentation should be generated in the documentation.
     */
    protected boolean isParameterVisible(ParameterInfo parameterInfo, boolean skipHandled) {
        if (skipHandled && parameterInfo.handled) {
            return false;
        }

        ParameterAnnotations parameterAnnotations = parameterInfo.annotations;
        if (parameterAnnotations.undocumented || parameterAnnotations.requestParameter || parameterAnnotations.internal) {
            //Some parameters are better left undocumented (e.g. verificationCode)
            return false;
        }

        return true;
    }

    /**
     * Generate the documentation for a given parameter. Performs a number of additional validations and mark the
     * parameter as "handled". Parameters that should not appear in the documentation are skipped.
     */
    private void generateParameterDocumentation(ParameterInfo parameterInfo, boolean skipHandled, StrBuilder outputStr)
    {
        if (! isParameterVisible(parameterInfo, skipHandled)) {
            return;
        }

        parameterInfo.handled = true;

        IConfigProperty prop = parameterInfo.property;

        String typeName;
        String keyName;
        String description = prop.getDescription();
        String defaultValue = "";
        String allowedValues = "";

        int nbLevels = 0;

        for (IConfigProperty currentProp = prop; currentProp.getParent() != null; currentProp = currentProp.getParent()) {
            ++nbLevels;
        }


        if (prop.getDefaultValue() == null)
        {
            keyName = prop.getKey();
            typeName = String.class.getSimpleName();
        }
        else
        {
            keyName = prop.getKey();
            defaultValue = String.format("Default value: %s", prop.getDefaultValue());

            if (prop.getDefaultValue() instanceof List)
            {
                typeName = "List";
            }
            else
            {
                typeName = prop.getDefaultValue().getClass().getSimpleName();
            }
        }

        if (prop.getValidValues() != null && prop.getValidValues().length > 0)
        {
            allowedValues = "Allowed values: " + Arrays.toString(prop.getValidValues())
                    .substring(1, Arrays.toString(prop.getValidValues())
                            .length() - 1);

        }

        if (description == null || description.isEmpty())
        {
            fail("Empty description for configuration key: " + prop);
            return;
        }

        // ensure that no description is manually tagged with "webapi.json:"
        if (description.startsWith("webapi.json:")) {
            fail(prop + ": \"webapi.json:\" should no longer be used in description. Use WebapiJsonOnly annotation instead.");
        }

        ParameterAnnotations parameterAnnotations = parameterInfo.annotations;

        // ensure that parameters that use the old marking for undocumented parameters also have a proper annotations.
        if (description.equals(UNDOCUMENTED_PARAMETER))
        {
            if (! (parameterAnnotations.undocumented || parameterAnnotations.requestParameter || parameterAnnotations.internal)) {
                fail("Missing annotation (Undocumented, Internal or RequestParameter) for undocumented parameter " + prop);
            }
        }

        if (parameterAnnotations.webapiJsonOnly) {
            description = WEBAPI_PREFIX + description;
        }

        outputStr.append("| ");
        for (int i = 0; i < nbLevels; ++i) {
            outputStr.append("&#160;&#160;&#160;&#160;");
        }
        outputStr.append(keyName);
        outputStr.append(" | `").append(typeName).append("`");
        outputStr.append(" | ").append(description);
        if (defaultValue.length() > 0) {
           outputStr.append(" <br> ").append(defaultValue);
        }
        if (allowedValues.length() > 0) {
            outputStr.append(" <br> ").append(allowedValues);
        }
        outputStr.append(" |").appendNewLine();

    }


    private void generateRemainingParametersSection(List<ParameterInfo> parameterInfos) throws IOException {

        boolean remainingParameterFound = false;
        StrBuilder outputStr = new StrBuilder();

        generateMarkdownHeader("Remaining Parameters", outputStr);

        for (ParameterInfo parameterInfo : parameterInfos) {
            if (isParameterVisible(parameterInfo, true)) {
                if (!remainingParameterFound) {
                    // first remaining param found, generate the section header
                    generateConfigSectionHeader("Remaining Parameters", "Remaining parameters",
                            "Uncategorized parameters.", outputStr);
                    remainingParameterFound = true;
                }

                generateParameterDocumentation(parameterInfo, outputStr);
            }
        }

        // close the section only if one param for this backend was found.
        if (remainingParameterFound) {
            createMarkdownFile("remaining-parameter.md", outputStr);
        }
    }

    private void generateTroubleshootSection() throws IOException {
        StrBuilder outputStr = new StrBuilder();

        generateMarkdownHeader("Troubleshooting", outputStr);
        generateConfigSectionHeader("", "Plugin Errors", "Error codes in plugins", outputStr);
        outputStr.replaceFirst("Type", "Error Code");
        for (RestPluginsError value : RestPluginsError.values()) {
            outputStr.append("| ").append(value.toString());
            outputStr.append(" | `").append(value.getValue()).append("`");
            outputStr.append(" | ").append(value.getMessage());
            outputStr.append(" |").appendNewLine();
        }
        createMarkdownFile("troubleshoot.md", outputStr);
    }


    /**
     * Create the list of parameters information, ordered in the same order they appear in the enum. They include a
     * digested version of the annotations retrieved on them.
     */
    private List<ParameterInfo> retrieveParametersInfo () throws DocGeneratorException {
        IConfigProperty[] configProperties;

        try {
            Method method = configEnumClass.getDeclaredMethod("values");
            Object obj = method.invoke(null);
            configProperties = (IConfigProperty[]) obj;

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  e) {
            throw new DocGeneratorException("Error while retrieving properties definitions", e);
        }

        List<ParameterInfo> parametersInfo = new LinkedList<>();
        for (IConfigProperty property : configProperties) {
            parametersInfo.add(new ParameterInfo(property, retrieveParameterAnnotations(property)));
        }

        return parametersInfo;
    }


    private ParameterAnnotations retrieveParameterAnnotations(IConfigProperty property) throws DocGeneratorException
    {
        ParameterAnnotations parameterAnnotations = new ParameterAnnotations();

        Field enumField = null;
        try {
            enumField = configEnumClass.getDeclaredField(property.getConstantName());
        } catch (NoSuchFieldException e) {
            // should never be thrown
            throw new DocGeneratorException("Error while retrieving config value property", e);
        }

        Annotation[] annotations = enumField.getAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == IConfigProperty.Internal.class) {
                parameterAnnotations.internal = true;
            }

            if (annotation.annotationType() == IConfigProperty.RequestParameter.class) {
                parameterAnnotations.requestParameter = true;
            }

            if (annotation.annotationType() == IConfigProperty.Undocumented.class) {
                parameterAnnotations.undocumented = true;
            }

            if (annotation.annotationType() == IConfigProperty.WebapiJsonOnly.class) {
                parameterAnnotations.webapiJsonOnly = true;
            }

            if (annotation.annotationType() == IConfigProperty.Backend.class) {
                parameterAnnotations.backends.add(((IConfigProperty.Backend) annotation).backend());
            }

            if (annotation.annotationType() == IConfigProperty.Backends.class) {
                IConfigProperty.Backend[] backendAnnotations = ((IConfigProperty.Backends) annotation).value();
                for (IConfigProperty.Backend backendAnnotation : backendAnnotations) {
                    parameterAnnotations.backends.add(backendAnnotation.backend());
                }
            }

            if (annotation.annotationType() == IConfigProperty.Plugin.class) {
                parameterAnnotations.plugins.add(((IConfigProperty.Plugin) annotation).plugin());
            }

            if (annotation.annotationType() == IConfigProperty.Plugins.class) {
                IConfigProperty.Plugin[] pluginAnnotations = ((IConfigProperty.Plugins) annotation).value();
                for (IConfigProperty.Plugin pluginAnnotation : pluginAnnotations) {
                    parameterAnnotations.plugins.add(pluginAnnotation.plugin());
                }
            }

            if (annotation.annotationType() == IConfigProperty.ConfigurationGroup.class) {
                parameterAnnotations.configurationGroup = (((IConfigProperty.ConfigurationGroup) annotation).group());
            }

        }

        return parameterAnnotations;
    }
}