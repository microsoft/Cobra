package com.nuance.cobra.utils.recognition.result;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public enum RecResultType
{
    NLSML(new ArrayList<>(Arrays.asList("application/x-vnd.speechworks.emma+xml",
                                        "rec_nlsml_results"))),
    EMMA(new ArrayList<>(Arrays.asList("application/x-vnd.nuance.emma+xml"))),
    REC_TEXT(new ArrayList<>(Arrays.asList("rec_text_results"))),
    BINARY(new ArrayList<>(Arrays.asList("rec_binary_results")));

    private final ArrayList<String> typeStrings;

    RecResultType(ArrayList<String> typeString)
    {
        this.typeStrings = typeString;
    }

    public ArrayList<String> getTypeStrings() { return typeStrings; }

    public static RecResultType getDefaultType() { return RecResultType.REC_TEXT; }

    public static RecResultType fromString(String string) { return fromString(string, getDefaultType()); }

    public static RecResultType fromString(String string, RecResultType defaultType)
    {
        for (RecResultType recResultType : RecResultType.values())
        {
            for (String typeString : recResultType.getTypeStrings())
            {
                if (StringUtils.containsIgnoreCase(string, typeString))
                {
                    return recResultType;
                }
            }
        }
        return defaultType;
    }
}
