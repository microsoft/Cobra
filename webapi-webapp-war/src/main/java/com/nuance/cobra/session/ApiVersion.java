package com.nuance.cobra.session;

public enum ApiVersion
{
    _1_0("1.0");

    final String string;

    ApiVersion(String string)
    {
        this.string = string;
    }

    public static ApiVersion forName(String string)
    {
        for (final ApiVersion value : values())
        {
            if (value.string.equals(string))
            {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown ApiVersion " + string);
    }

    @Override
    public String toString()
    {
        return string;
    }

    /**
     * Compare two ApiVersions.
     *
     * @param rhs 'right-hand-side' value.
     * @return true if this ordinal is less than the rhs ordinal, false otherwise.
     */
    public boolean lessThan(ApiVersion rhs)
    {
        return 0 > compareTo(rhs);
    }

    /**
     * Compare two ApiVersions.
     *
     * @param rhs 'right-hand-side' value.
     * @return true if this ordinal is less than or equal to the rhs odrinal, false otherwise.
     */
    public boolean lessThanOrEquals(ApiVersion rhs)
    {
        return 1 > compareTo(rhs);
    }

    /**
     * Compare two ApiVersions.
     *
     * @param rhs 'right-hand-side' value.
     * @return true if this ordinal is greater than the rhs ordinal, false otherwise.
     */
    public boolean greaterThan(ApiVersion rhs)
    {
        return 0 < compareTo(rhs);
    }

    /**
     * Compare two ApiVersions.
     *
     * @param rhs 'right-hand-side' value.
     * @return true if this ordinal is greater than or equal to the rhs ordinal, false otherwise.
     */
    public boolean greaterThanOrEquals(ApiVersion rhs)
    {
        return -1 < compareTo(rhs);
    }

}
