package com.nuance.cobra.plugin;

public class TestInterfaceImpl implements TestInterface
{
    static int nbInvocations = 0;

    @Override
    public void doSomething()
    {
        nbInvocations++;
    }
}
