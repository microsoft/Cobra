package com.nuance.cobra.session;

import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

import static com.nuance.cobra.session.SessionValidator.AUTHENTICATION_GENERIC_DATA;
import static com.nuance.cobra.session.SessionValidator.AUTHENTICATION_TYPE_NAME;

public class AuthTypeAndData
{
    private static final Logger logger = LogManager.getLogger();

    private ValidationTypes authType;
    private String authData = "";

    public enum ValidationTypes {
        hashcode,
        jws,        // jwt authentication. Standard claims will be validated,
        no_auth,    // No authentication, Authorization header is absent
        http_auth,  // HTTP digest authentication
        delegated;  // Request has been preauthorized by an external service (eg enterprise auth). Request is presumed to
                    // be valid if it reaches webapi, thus all request will be accepted.

        public static List<ValidationTypes> buildFromStrings(List<String> activeAuthMethodsList){
            List<ValidationTypes> validators = new LinkedList<>();
            for (String authMethod: activeAuthMethodsList) {
                validators.add(ValidationTypes.valueOf(authMethod.toLowerCase()));
            }
            return validators;
        }
    }

    public AuthTypeAndData()
    {
        throw new IllegalStateException("Utility class");
    }

    public AuthTypeAndData(CombinedConfiguration localConfig)
    {
        HierarchicalConfiguration
                requestAuthData = CobraConfigurationUtils.configurationAtOrNull(localConfig, WebApiConfig.Property.AUTH_DATA.key);

        if (requestAuthData == null || requestAuthData.isEmpty()) {
            logger.debug("Using a deprecated authentication flag. 'verificationCode' and 'jwt' have been replaced by 'authentication' object.");
            if (localConfig.containsKey(WebApiConfig.Property.VERIFICATION_CODE.key)) {
                authData = localConfig.getString(WebApiConfig.Property.VERIFICATION_CODE.key);
                authType = ValidationTypes.hashcode;
            } else {
                authType = ValidationTypes.no_auth;
            }
        } else {
            authType = ValidationTypes.valueOf(requestAuthData.getString(AUTHENTICATION_TYPE_NAME).toLowerCase());
            authData = (String) requestAuthData.getString(AUTHENTICATION_GENERIC_DATA);
        }
        logger.debug("Active authentication method : {}", authType);
    }

    public ValidationTypes getAuthType()
    {
        return authType;
    }

    public String getAuthData()
    {
        return authData;
    }
}
