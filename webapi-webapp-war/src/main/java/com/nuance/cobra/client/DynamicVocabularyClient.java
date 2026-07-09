package com.nuance.cobra.client;

/**
 * The abstract definition of a DynamicVocabularyClient
 *
 */
public abstract class DynamicVocabularyClient extends Client
{
    /**
     * Constructor for the DynamicVocabularyClient super class
     * @param sessionId
     */
    protected DynamicVocabularyClient(String sessionId)
    {
        super(sessionId);
    }

    /**
     * Abstract definition of the uploadDynamicVocabulary method, allowing to upload a dynamic vocabulary
     */
    public abstract void uploadDynamicVocabulary();
}
