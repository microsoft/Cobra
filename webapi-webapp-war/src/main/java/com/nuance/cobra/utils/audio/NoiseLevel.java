package com.nuance.cobra.utils.audio;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.nuance.cobra.api.response.Views;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NoiseLevel {

    public NoiseLevel(double mean, double standardDeviation)
    {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    @SuppressWarnings("unused")
    private double mean = 0;

    @SuppressWarnings("unused")
    private double standardDeviation = 0;

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("mean")
    public double getMean() {
        return formatDouble(mean);
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    @SuppressWarnings("unused")
    @JsonView(Views.Visible.class)
    @JsonProperty("standardDeviation")
    public double getStandardDeviation()
    {
        return formatDouble(standardDeviation);
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    private double formatDouble(double value)
    {
        //2 decimal places is more presentable and is of sufficient precision
        return Math.floor(value*100)/100;
    }

    public static NoiseLevel mapToNoiseLevel(Map noiseLevel)
    {
        if (noiseLevel == null ||
            !noiseLevel.containsKey("mean") ||
            !noiseLevel.containsKey("standardDeviation"))
        {
            return null;
        }

        Number meanNum = (Number)noiseLevel.get("mean");
        double mean = meanNum.doubleValue();
        Number stdDevNum = (Number)noiseLevel.get("standardDeviation");
        double standardDeviation = stdDevNum.doubleValue();
        if (!(standardDeviation >= 0.0))
        {
            throw new IllegalArgumentException("StandardDeviation must be greater than or equal to 0.0!");
        }
        return new NoiseLevel(mean,standardDeviation);
    }

    public static Map noiseLevelToMap(NoiseLevel noiseLevel)
    {
        Map noiseLevelMap = new HashMap<String, Number>();
        noiseLevelMap.put("mean", noiseLevel.mean);
        noiseLevelMap.put("standardDeviation", noiseLevel.standardDeviation);
        return noiseLevelMap;
    }

    public static Map noiseLevelJsonObjectToMap(JSONObject noiseLevelJsonObject)
    {
        Map noiseLevelMap = new HashMap<String, Number>();
        noiseLevelMap.put("mean", noiseLevelJsonObject.optDouble("mean", 0));
        noiseLevelMap.put("standardDeviation", noiseLevelJsonObject.optDouble("standardDeviation", 0));
        return noiseLevelMap;
    }

    public static final NoiseLevel noiseLevelValidator (Object param, String name)
    {
        Map paramToConvert;
        if (param instanceof JSONObject)
        {
            JSONObject noiseLevelJsonObject = (JSONObject) param;
            paramToConvert = noiseLevelJsonObjectToMap(noiseLevelJsonObject);
        } else
        {
            paramToConvert = (Map)param;
        }

        NoiseLevel noiseLevel = mapToNoiseLevel(paramToConvert);

        if (noiseLevel == null)
        {
            throw new IllegalArgumentException(name + " is not valid");
        }

        return noiseLevel;
    };
}
