package com.nuance.cobra.utils.recognition.result.generator.nlsml;

import com.nuance.cobra.utils.recognition.result.generator.Grammar;
import com.nuance.cobra.utils.recognition.result.generator.Intent;
import com.nuance.cobra.utils.recognition.result.generator.Outcome;
import com.nuance.cobra.utils.recognition.result.generator.Variable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NlsmlGeneratorTest {
    private NlsmlGenerator nlsmlGenerator;

    @Before
    public void runBeforeEachTest() {
        nlsmlGenerator = new NlsmlGenerator();
    }

    @Test
    public void testNlsmlGenerator() {
        Grammar activeGrammar = new Grammar("session:0*00000002", "session:testgrammar1@speechworks.com");

        String intentId = "intentId";
        String intentValue = "intentValue";
        Outcome outcome = new Outcome();
        outcome.setIntent(new Intent(intentId, intentValue));

        String variableId1 = "variable1";
        String variableValue1 = "variableValue1";
        String variableId2 = "variable2";
        String variableValue2 = "variableValue2";

        Variable[] variables = new Variable[2];
        variables[0] = new Variable(variableId1, variableValue1);
        variables[1] = new Variable(variableId2, variableValue2);
        outcome.setVariables(variables);

        List<String> globalsInTest = new ArrayList<String>();
        String global1 = "global1";
        String global2 = "global2";
        globalsInTest.add(global1);
        globalsInTest.add(global2);
        outcome.setGlobals(globalsInTest);

        Assert.assertEquals("nlsml result match", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<result>" + "<interpretation grammar=\""
                                                  + activeGrammar.getId() + "\" confidence=\"1\">" + "<input mode=\"speech\">" + intentValue
                                                  + " " + variableValue1
                                                  + " " + variableValue2
                                                  + " " + global1
                                                  + " " + global2
                                                  + "</input>"
                                                  + "<instance>"
                                                  + "<" + intentId + " confidence=\"1\">" + intentValue + "</" + intentId + ">"
                                                  + "<" + variableId1 + " confidence=\"1\">" + variableValue1 + "</" + variableId1 + ">"
                                                  + "<" + variableId2 + " confidence=\"1\">" + variableValue2 + "</" + variableId2 + ">"
                                                  + "<" + global1 + " confidence=\"1\">" + true + "</" + global1 + ">"
                                                  + "<" + global2 + " confidence=\"1\">" + true + "</" + global2 + ">"
                                                  + "<SWI_literal>"
                                                  + intentValue
                                                  + " " + variableValue1
                                                  + " " + variableValue2
                                                  + " " + global1
                                                  + " " + global2
                                                  + "</SWI_literal>"
                                                  + "<SWI_grammarName>" + activeGrammar.getId() + "</SWI_grammarName>"
                                                  + "<SWI_meaning>{" + intentId + ":" + intentValue
                                                  + " " + variableId1 + ":" + variableValue1
                                                  + " " + variableId2 + ":" + variableValue2
                                                  + " " + global1 + ":true"
                                                  + " " + global2 + ":true"
                                                  + "}</SWI_meaning>" + "</instance>" + "</interpretation>" + "</result>\r\n",
                nlsmlGenerator.generate(activeGrammar, outcome));
    }

    @Test
    public void testNlsmlGeneratorWithDefinedVariables() {
        Grammar activeGrammar = new Grammar("session:0*00000002", "session:testgrammar1@speechworks.com");

        String intentId = "INTENT";
        String intentValue = "WAVE_FEE_REQUEST";

        Outcome outcome = new Outcome();
        outcome.setIntent(new Intent(intentId, intentValue));

        Assert.assertEquals("nlsml result match", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<result>" + "<interpretation grammar=\""
                                                  + activeGrammar.getId() + "\" confidence=\"1\">" + "<input mode=\"speech\">" + intentValue
                                                  + "</input>"
                                                  + "<instance>"
                                                  + "<" + intentId + " confidence=\"1\">" + intentValue + "</" + intentId + ">"
                                                  + "<SWI_literal>"
                                                  + intentValue
                                                  + "</SWI_literal>"
                                                  + "<SWI_grammarName>" + activeGrammar.getId() + "</SWI_grammarName>"
                                                  + "<SWI_meaning>{" + intentId + ":" + intentValue
                                                  + "}</SWI_meaning>" + "</instance>" + "</interpretation>" + "</result>\r\n",
                nlsmlGenerator.generate(activeGrammar, outcome));
    }

    @Test
    public void testNlsmlGeneratorWithDefinedVariablesNullValues() {
        Outcome outcome = new Outcome();

        String output = nlsmlGenerator.generate(null, outcome);

        Assert.assertEquals(
                "nlsml result match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<result>"
                + "<interpretation confidence=\"1\">"
                + "<input mode=\"speech\" />"
                + "<instance>"
                + "<SWI_literal />"
                + "<SWI_meaning>{}</SWI_meaning>"
                + "</instance>"
                + "</interpretation>"
                + "</result>\r\n", output);
    }


    @Test
    public void testEmmaGeneratorRecoSource() {
        nlsmlGenerator.setAddRecoSource(true);

        Outcome outcome = new Outcome();

        String output = nlsmlGenerator.generate(null, outcome);

        Assert.assertEquals(
                "nlsml result match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<result>"
                + "<interpretation confidence=\"1\">"
                + "<input mode=\"speech\" />"
                + "<instance>"
                + "<SWI_literal />"
                + "<SWI_meaning>{}</SWI_meaning>"
                + "<reco_source>NAA</reco_source>"
                + "</instance>"
                + "</interpretation>"
                + "</result>\r\n", output);
    }
}