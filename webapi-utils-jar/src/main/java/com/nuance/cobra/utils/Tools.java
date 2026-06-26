package com.nuance.cobra.utils;

import java.nio.file.Paths;

public class Tools {
    public static String getJettyPath() {
        String jettyBase = System.getenv("JETTY_BASE");

        if (jettyBase == null) {
            jettyBase = Paths.get(".")
                    .toAbsolutePath()
                    .normalize()
                    .toString();
        }

        return jettyBase;
    }

    public static Double reMap(double x, double inMin, double inMax, double outMin, double outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
