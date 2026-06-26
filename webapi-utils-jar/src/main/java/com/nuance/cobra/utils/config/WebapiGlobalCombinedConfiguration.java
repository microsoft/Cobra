package com.nuance.cobra.utils.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.tree.NodeCombiner;

@SuppressWarnings("java:S110")
public class WebapiGlobalCombinedConfiguration extends CombinedConfiguration
{
    public WebapiGlobalCombinedConfiguration()
    {
        super();
    }

    public WebapiGlobalCombinedConfiguration(NodeCombiner comb)
    {
        super(comb);
    }
}
