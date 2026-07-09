package com.nuance.plugin.v1.shareable.edf;

public interface IEdfLogPoster {
    /**
     * Publish an EDF log.
     * @param log
     */
    void publishEdfLog(IEdfMessage log);

    /**
     * Publish an EDF v2 log
     * @param log message to publish
     */
    void publishEdfLog(IEdfV2Message log);

    /**
     * Publish an NEAP log
     * @param log message to publish
     */
    public void publishNeapLog(INeapMessage log);
}
