package com.nuance.cobra.utils.recognition.result.generator.emma;

import com.nuance.cobra.utils.recognition.result.generator.Grammar;
import com.nuance.cobra.utils.recognition.result.generator.Intent;
import com.nuance.cobra.utils.recognition.result.generator.Outcome;
import com.nuance.cobra.utils.recognition.result.generator.Variable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EmmaGeneratorTest {
    private EmmaGenerator emmaGenerator;

    @Before
    public void runBeforeEachTest() {
        emmaGenerator = new EmmaGenerator();
    }

    @Test
    public void testEmmaGenerator() {
        Grammar activeGrammar = new Grammar("grammar_1", "activeGrammarRef");

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

        String output = emmaGenerator.generate(activeGrammar, outcome);
        System.out.println(output);

        Assert.assertEquals(
                "emma result match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<emma:emma xmlns:emma=\"http://www.w3.org/TR/2007/CR-emma-20071211\" xmlns:nuance=\"http://nr10.nuance.com/emma\" version=\"1.0\">"
                + "<emma:grammar id=\"grammar_1\" ref=\""
                + activeGrammar.getId()
                + "\" />"
                + "<emma:interpretation id=\"interp_1\" emma:confidence=\"1\" emma:grammar-ref=\"grammar_1\" emma:tokens=\""
                + intentValue
                + " "
                + variableValue1
                + " "
                + variableValue2
                + "\" emma:mode=\"voice\">"
                + "<"
                + intentId
                + " conf=\"1\">"
                + intentValue
                + "</"
                + intentId
                + ">"
                + "<"
                + variableId1
                + " conf=\"1\">"
                + variableValue1
                + "</"
                + variableId1
                + ">"
                + "<"
                + variableId2
                + " conf=\"1\">"
                + variableValue2
                + "</"
                + variableId2
                + ">"
                + "<"
                + global1
                + " conf=\"1\">"
                + true
                + "</"
                + global1
                + ">"
                + "<"
                + global2
                + " conf=\"1\">"
                + true
                + "</"
                + global2
                + ">"
                + "<SWI_literal>"
                + intentValue
                + " "
                + variableValue1
                + " "
                + variableValue2
                + "</SWI_literal>"
                + "<SWI_meaning>{"
                + intentId
                + ":"
                + intentValue
                + " "
                + variableId1
                + ":"
                + variableValue1
                + " "
                + variableId2
                + ":"
                + variableValue2
                + " "
                + global1
                + ":true"
                + " "
                + global2
                + ":true"
                + "}</SWI_meaning>"
                + "<ssm_score conf=\"1\">100</ssm_score>"
                + "</emma:interpretation>"
                + "</emma:emma>\r\n", output);
    }

    @Test
    public void testEmmaGeneratorEmpty() {
        Outcome outcome = new Outcome();

        String output = emmaGenerator.generate(null, outcome);

        Assert.assertEquals(
                "emma result match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<emma:emma xmlns:emma=\"http://www.w3.org/TR/2007/CR-emma-20071211\" xmlns:nuance=\"http://nr10.nuance.com/emma\" version=\"1.0\">"
                + "<emma:interpretation id=\"interp_1\" emma:confidence=\"1\" emma:grammar-ref=\"grammar_1\" emma:tokens=\"\" emma:mode=\"voice\">"
                + "<SWI_literal />"
                + "<SWI_meaning>{}</SWI_meaning>"
                + "<ssm_score conf=\"1\">100</ssm_score>"
                + "</emma:interpretation>"
                + "</emma:emma>\r\n", output);
    }

    @Test
    public void testEmmaGeneratorRecoSource() {
        emmaGenerator.setAddRecoSource(true);

        Outcome outcome = new Outcome();

        String output = emmaGenerator.generate(null, outcome);

        Assert.assertEquals(
                "emma result match",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<emma:emma xmlns:emma=\"http://www.w3.org/TR/2007/CR-emma-20071211\" xmlns:nuance=\"http://nr10.nuance.com/emma\" version=\"1.0\">"
                + "<emma:interpretation id=\"interp_1\" emma:confidence=\"1\" emma:grammar-ref=\"grammar_1\" emma:tokens=\"\" emma:mode=\"voice\">"
                + "<SWI_literal />"
                + "<SWI_meaning>{}</SWI_meaning>"
                + "<ssm_score conf=\"1\">100</ssm_score>"
                + "<reco_source>NAA</reco_source>"
                + "</emma:interpretation>"
                + "</emma:emma>\r\n", output);
    }
}
