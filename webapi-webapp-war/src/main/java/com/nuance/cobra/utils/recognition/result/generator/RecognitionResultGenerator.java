package com.nuance.cobra.utils.recognition.result.generator;

public interface RecognitionResultGenerator
{
    void setAddRecoSource(boolean addRecoSource);

    String generate(Grammar activeGrammar, Outcome outcome);
}
