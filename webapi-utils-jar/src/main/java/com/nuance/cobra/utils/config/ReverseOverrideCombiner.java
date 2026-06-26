package com.nuance.cobra.utils.config;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.OverrideCombiner;

public class ReverseOverrideCombiner extends OverrideCombiner
{
    @Override
    public ImmutableNode combine(ImmutableNode node1, ImmutableNode node2) {
        return super.combine(node2, node1);
    }

    @Override
    protected ImmutableNode canCombine(ImmutableNode node1, ImmutableNode node2, ImmutableNode child) {
        return super.canCombine(node2, node1, child);
    }
}