package com.nuance.cobra.utils;

import java.util.concurrent.Semaphore;


public class SemaphoreWrapper
{
    private Semaphore semaphore;
    private int maxPermit;

    public SemaphoreWrapper(int permits)
    {
        semaphore = new Semaphore(permits);
        maxPermit = permits;
    }

/*    public static SemaphoreWrapper create(int permits)
    {
        WebAPIRateLimiter limiter = new WebAPIRateLimiter(permits);
        return limiter;
    }*/

    public boolean tryAcquire()
    {
        return semaphore.tryAcquire();
    }


    public void release()
    {
        if (semaphore.availablePermits() < maxPermit) //This condition is to restrict semaphore leakage
        {
            semaphore.release();
        }
    }

    public void release(int permit)
    {
        if (semaphore.availablePermits() <= (maxPermit - permit)) //This condition is to restrict semaphore leakage
        {
            semaphore.release(permit);
        }
    }

    public void releaseAll()
    {
        semaphore = new Semaphore(maxPermit);
    }

    public int getAvailablePermits()
    {
        return semaphore.availablePermits();
    }

    public int getMaxPermit()
    {
        return maxPermit;
    }

}

