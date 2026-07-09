package com.nuance.cobra.utils.response.EDF;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuance.cobra.session.Session;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class UserInfoTest {

    private UserInfo uInfo = null;

    @Test(expected = IllegalArgumentException.class)
    public void test_client_data(){
        CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        Configuration testConfig = CobraConfigurationUtils.getHighestConfig(testConfigs);
        testConfig.setProperty(WebApiConfig.Property.CLIENT_USER_ID.key, "testID");
        testConfig.setProperty(WebApiConfig.Property.CLIENT_DEVICE_ID.key, "testClientID");
        testConfig.setProperty(WebApiConfig.Property.CLIENT_OS_NAME.key, "testClientOS");
        testConfig.setProperty(WebApiConfig.Property.CLIENT_OS_VERSION.key, "10.0.1");
        testConfig.setProperty(WebApiConfig.Property.CLIENT_SESSION_ID.key, "A1B1C1D1");
        Session mockSession = new Session( "testID", CobraConfigurationUtils.newCombinedConfigurationWithDefaults());
        uInfo = new UserInfo(mockSession);

        uInfo = new UserInfo(null);
    }

    @Test
    public void test_default_userinfo(){
        uInfo = UserInfo.defaultUserInfo();
        Assert.assertEquals("notDefined", uInfo.clientId);
        Assert.assertEquals("notDefined", uInfo.clientDeviceId);
        Assert.assertEquals("notDefined", uInfo.clientSessionId);
        Assert.assertEquals("notDefined", uInfo.clientOsVersion);
        Assert.assertEquals("notDefined", uInfo.clientOsName);
    }

    @Test
    public void test_client_metadata()
            throws JsonProcessingException, ConfigurationException
    {
        CombinedConfiguration testConfigs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
        WebApiJSONConfiguration testConfig = CobraConfigurationUtils.getHighestConfigAsWebapiJsonConfiguration(testConfigs);

        Map<String, Object> details = new HashMap<>();
        details.put("clientAppVersion", "1.10.0");
        details.put("clientHardwareSubmodel", "iPhone 3G");
        Map<String, Object> clientDetails = new HashMap<>();
        clientDetails.put(WebApiConfig.Property.CLIENT_DETAILS.key, details);

        String clientDetailsJson = new ObjectMapper().writeValueAsString(clientDetails);
        testConfig.read(new StringReader(clientDetailsJson));

        Session mockSession = new Session( "testID",testConfigs);
        uInfo = new UserInfo(mockSession);
        Assert.assertEquals(details, uInfo.clientDetails);
    }
}
