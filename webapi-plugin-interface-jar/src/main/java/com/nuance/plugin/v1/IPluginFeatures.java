package com.nuance.plugin.v1;

import java.util.Optional;

/**
 * Interface for hte features that a plugin can optionally provide to interact with the framework.
 */
public interface IPluginFeatures
{
    /**
     * Plugins can return a configurator to perform custom validations on the JWT token.
     *
     * @return Validations configurator or empty.
     */
    default Optional<IJwtValidationsConfigurator> getJwtValidationsConfigurator() {
        // default: empty, the default session validator JWT validations will be performed.
        return Optional.empty();
    }

}
