package com.nuance.cobra.testing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMetricsVerifier {

    public boolean verifyMetric(String name, String[] labels, String[] values, double expectedVal) throws IOException, InterruptedException {
        boolean verified = false;
        try (Scanner scanner = new Scanner(MetricsEndpointHelper.getMetricsResponse())) {
            Pattern pattern = MetricsEndpointHelper.getPattern(name, labels);
            while (scanner.hasNextLine()) {
                // assume that the current line isn't the right metric
                boolean isCorrectMetric = false;
                String line = scanner.nextLine();
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    // check each value and match to their corresponding label.
                    for (int i = 0; i < values.length; i++) {
                        // if the value matches the label, we have a partial match
                        if (!values[i].equals(matcher.group(i + 1))) {
                            // one of the label values doesn't match, go to the next line
                            isCorrectMetric = false;
                            break;
                        }
                        isCorrectMetric = true;
                    }
                    // if none of the lines toggled this flag to true, it means the metric is missing so verified will be false
                    if (isCorrectMetric) {
                        //at this point, the line is the right metric, we can say that the metric has been verified
                        double actualVal = Double.parseDouble(matcher.group(labels.length + 1));
                        Map<String, String> labelValuePair = getLabelValuePairs(labels, matcher);
                        doMatch(labelValuePair, actualVal, expectedVal);
                        verified = true;
                    }
                }
            }
        }
        return verified;
    }

    private Map<String, String> getLabelValuePairs(String[] labels, Matcher matcher) {
        Map<String, String> labelValuePair = new HashMap<>();
        for(int i = 0; i<labels.length; i++) {
            labelValuePair.put(labels[i], matcher.group(i+1));
        }
        return labelValuePair;
    }

    public abstract void doMatch(Map<String, String> labelValuePair, double actualVal, double expectedVal);
}
