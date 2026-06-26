package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;

import java.util.HashMap;
import java.util.Map;

public class UserInfo
{
    @JsonProperty("clientId")
    public String clientId;

    @JsonProperty("clientDeviceId")
    public String clientDeviceId;

    @JsonProperty("clientSessionId")
    public String clientSessionId;

    @JsonProperty("clientOsName")
    public String clientOsName;

    @JsonProperty("clientOsVersion")
    public String clientOsVersion;

    @JsonProperty("clientSubscriberId")
    public String clientSubscriberId;

    @JsonProperty("clientDetails")
    public Map<String, Object> clientDetails;

    public UserInfo()
    {

    }

    public UserInfo(Session session)
    {
        if (session == null)
        {
            throw new IllegalArgumentException("UserInfo -- tried to set values, but session is null");
        }

        clientId = session.getSessionConfig().getProperty(WebApiConfig.Property.CLIENT_USER_ID.key).toString();
        clientDeviceId = session.getSessionConfig().getProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key).toString();
        clientOsName = session.getSessionConfig().getProperty(WebApiConfig.Property.CLIENT_OS_NAME.key).toString();
        clientOsVersion = session.getSessionConfig().getProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key).toString();
        clientSessionId = session.getSessionConfig().getProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key).toString();
        clientSubscriberId = session.getSessionConfig().getProperty(WebApiConfig.Property.CLIENT_SUBSCRIBER_ID.key).toString();

        HierarchicalConfiguration clientDetailsHierConfig = CobraConfigurationUtils.configurationAtOrNull(session.getSessionConfig(), WebApiConfig.Property.CLIENT_DETAILS.key);
        clientDetails = CobraConfigurationUtils.hierarchicalConfigurationToMap(clientDetailsHierConfig);
    }

    public static UserInfo defaultUserInfo()
    {
        final String NOT_DEFINED = "notDefined";

        UserInfo userInfo = new UserInfo();
        userInfo.clientId = NOT_DEFINED;
        userInfo.clientDeviceId = NOT_DEFINED;
        userInfo.clientSessionId = NOT_DEFINED;
        userInfo.clientOsVersion = NOT_DEFINED;
        userInfo.clientOsName = NOT_DEFINED;
        return userInfo;
    }

    public Map<String, Object> toMap() {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.convertValue(this, Map.class);
    }

    public Map<String, Object> toFreeformMap () {
        Map<String, Object> result = new HashMap<>();
        if (clientDetails != null) {
            result.putAll(clientDetails);
        }
        result.putAll(this.toMap());

        return result;
    }
}
