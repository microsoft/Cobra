package com.nuance.cobra.utils.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebapiGlobalCombinedConfigurationTest
{
    @Test(expected = Test.None.class)
    public void construct()
    {
        new WebapiGlobalCombinedConfiguration();
        new WebapiGlobalCombinedConfiguration(new ReverseOverrideCombiner());
    }

    @Test
    public void destruct()
    {
        CombinedConfiguration cc = new WebapiGlobalCombinedConfiguration(new ReverseOverrideCombiner());
        cc.addConfiguration(new WebApiJSONConfiguration(), "test");
        ConfigHelpers.clearCombinedConfiguration(cc, null);
        assertEquals(1, cc.getNumberOfConfigurations());
    }
}