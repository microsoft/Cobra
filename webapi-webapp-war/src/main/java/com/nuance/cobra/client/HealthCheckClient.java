package com.nuance.cobra.client;


import com.nuance.cobra.utils.HealthCheckException;

public interface HealthCheckClient
{
    void checkHealth() throws HealthCheckException;
}
