package com.nuance.cobra.client;

import java.util.Arrays;

public enum VocalizerPremiumVoiceModels
{
    XPREMIUM_HIGH("xpremium-high"),
    XPREMIUM("xpremium"),
    XPREMIUM_NB("xpremium-nb"),
    ENHANCED("enhanced");

    private final String value;

    VocalizerPremiumVoiceModels(String voiceModel)
    {
        value = voiceModel;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return getValue();
    }

    public static String[] getValues()
    {
        return Arrays.stream(VocalizerPremiumVoiceModels.class.getEnumConstants()).map(VocalizerPremiumVoiceModels::getValue).toArray(String[]::new);
    }

    public static VocalizerPremiumVoiceModels fromString(String voiceModel)
    {
        if (voiceModel == null)
        {
            throw new NullPointerException();
        }

        switch (voiceModel.toLowerCase())
        {
            case "xpremium-high":
                return VocalizerPremiumVoiceModels.XPREMIUM_HIGH;

            case "xpremium":
                return VocalizerPremiumVoiceModels.XPREMIUM;

            case "xpremium-nb":
                return VocalizerPremiumVoiceModels.XPREMIUM_NB;

            case "enhanced":
                return VocalizerPremiumVoiceModels.ENHANCED;

            default:
                throw new IllegalArgumentException("Not a known Vocalizer Premium Voice Model: " + voiceModel);
        }
    }

    public static boolean isPremiumVoiceModel(String voiceModel)
    {
        try
        {
            fromString(voiceModel);
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }
}