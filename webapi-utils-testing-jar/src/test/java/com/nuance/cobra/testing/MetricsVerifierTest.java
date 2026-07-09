package com.nuance.cobra.testing;

import com.nuance.cobra.utils.metrics.MetricsConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MetricsEndpointHelper.class})
public class MetricsVerifierTest {
    private MetricsVerifier metricsVerifier;
    private final String pattern = "webapi_command_errors_total.*tenant=\"([^\"]*).*command=\"([^\"]*)\".*}\\D+([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
    private final String TENANT_NAME = "test-tenant";
    private final String REQUEST_COMMAND = "test-command";

    @Before
    public void setUp() {
        mockStatic(MetricsEndpointHelper.class);
        PowerMockito.when(MetricsEndpointHelper.getPattern(any(), any())).thenReturn(Pattern.compile(pattern));
        metricsVerifier = new MetricsVerifier();
    }

    @Test
    public void verifyMetricExistsTest() throws IOException, InterruptedException {
        String responseBody =
                "# HELP webapi_command_errors_total Number of command errors\n" +
                        "# TYPE webapi_command_errors_total counter\n" +
                        "webapi_command_errors_total{tenant=\"companyName_appName\",command=\"startSession\",} 1.0\n" +
                        "webapi_command_errors_total{tenant=\"test-tenant\",command=\"test-command\",} 1.0\n" +
                        "# HELP webapi_command_duration_seconds Command execution duration: \n" +
                        "# TYPE webapi_command_duration_seconds histogram\n" +
                        "webapi_command_duration_seconds_bucket{commandName=\"startSession\",le=\"0.01\",} 0.0";

        PowerMockito.when(MetricsEndpointHelper.getMetricsResponse()).thenReturn(responseBody);

        boolean result = metricsVerifier.verifyMetric("webapi_command_errors_total",
                new String[]{MetricsConstants.TENANT_LABEL, MetricsConstants.COMMAND_LABEL},
                new String[]{TENANT_NAME, REQUEST_COMMAND},
                1.0);

        assertTrue(result);
    }

    @Test
    public void verifyMetricDoesNotExistTest() throws IOException, InterruptedException {
        String responseBody =
                "# HELP webapi_command_errors_total Number of command errors\n" +
                        "# TYPE webapi_command_errors_total counter\n" +
                        "webapi_command_errors_total{tenant=\"companyName_appName\",command=\"startSession\",} 1.0\n" +
                        "# HELP webapi_command_duration_seconds Command execution duration: \n" +
                        "# TYPE webapi_command_duration_seconds histogram\n" +
                        "webapi_command_duration_seconds_bucket{commandName=\"startSession\",le=\"0.01\",} 0.0";

        PowerMockito.when(MetricsEndpointHelper.getMetricsResponse()).thenReturn(responseBody);

        boolean result = metricsVerifier.verifyMetric("webapi_command_errors_total",
                new String[]{MetricsConstants.TENANT_LABEL, MetricsConstants.COMMAND_LABEL},
                new String[]{TENANT_NAME, REQUEST_COMMAND},
                1.0);

        assertFalse(result);
    }
}
