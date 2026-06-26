package com.nuance.cobra.utils.recognition.result.generator.nlsml;

import com.nuance.cobra.utils.recognition.result.generator.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.List;

public class NlsmlGenerator implements RecognitionResultGenerator
{
    private static Logger logger = LogManager.getLogger(NlsmlGenerator.class);

    private XMLOutputter xmlOutputter;

    private boolean addRecoSource;

    public NlsmlGenerator() {
        this.xmlOutputter = new XMLOutputter();
        this.addRecoSource = false;
    }

    @Override
    public void setAddRecoSource(boolean addRecoSource) {
        this.addRecoSource = addRecoSource;
    }

    @Override
    public String generate(Grammar activeGrammar, Outcome outcome) {
        Intent intent = outcome.getIntent();

        Variable[] variables = outcome.getVariables();
        List<String> globals = outcome.getGlobals();

        Element resultEl = new Element("result");

        StringBuilder tokens = new StringBuilder();
        if (intent != null) {
            tokens.append(intent.getValue());
        }
        if (variables != null) {
            for (Variable variable : variables) {
                tokens.append(" " + variable.getValue());
            }
        }
        if (globals != null) {
            for (String global : globals) {
                tokens.append(" " + global);
            }
        }

        Element interpretationEl = new Element("interpretation");
        if (activeGrammar != null) {
            interpretationEl.setAttribute("grammar", activeGrammar.getId());
        }
        interpretationEl.setAttribute("confidence", "1");

        Element inputEl = new Element("input");
        inputEl.setAttribute("mode", "speech");
        inputEl.addContent(tokens.toString());
        interpretationEl.addContent(inputEl);

        Element instanceEl = new Element("instance");
        if (intent != null) {
            Element intentEl = new Element(intent.getId());
            intentEl.setAttribute("confidence", "1");
            intentEl.addContent(intent.getValue());
            instanceEl.addContent(intentEl);
        }
        if (variables != null) {
            for (Variable variable : variables) {
                Element variableEl = new Element(variable.getId());
                variableEl.setAttribute("confidence", "1");
                variableEl.addContent(variable.getValue());
                instanceEl.addContent(variableEl);
            }
        }
        if (globals != null) {
            for (String global : globals) {
                Element globalEl = new Element(global);
                globalEl.setAttribute("confidence", "1");
                globalEl.addContent("true");
                instanceEl.addContent(globalEl);
            }
        }

        Element swiLiteralEl = new Element("SWI_literal");
        swiLiteralEl.addContent(tokens.toString());
        instanceEl.addContent(swiLiteralEl);

        if (activeGrammar != null) {
            Element swiGrammarNameEl = new Element("SWI_grammarName");
            swiGrammarNameEl.addContent(activeGrammar.getId());
            instanceEl.addContent(swiGrammarNameEl);
        }

        StringBuilder meaning = new StringBuilder();
        meaning.append("{");
        if (intent != null) {
            meaning.append(intent.getId() + ":" + intent.getValue());
        }
        if (variables != null) {
            for (Variable variable : variables) {
                meaning.append(" " + variable.getId() + ":" + variable.getValue());
            }
        }
        if (globals != null) {
            for (String global : globals) {
                meaning.append(" " + global + ":true");
            }
        }
        meaning.append("}");

        Element swiMeaningEl = new Element("SWI_meaning");
        swiMeaningEl.addContent(meaning.toString());
        instanceEl.addContent(swiMeaningEl);

        if (addRecoSource) {
            Element recoSourceEl = new Element("reco_source");
            recoSourceEl.addContent("NAA");
            instanceEl.addContent(recoSourceEl);
        }

        interpretationEl.addContent(instanceEl);

        resultEl.addContent(interpretationEl);

        Document document = new Document(resultEl);

        Format format = Format.getCompactFormat();
        // Format format = Format.getPrettyFormat();
        // Format format = Format.getRawFormat();
        xmlOutputter.setFormat(format);

        String output = xmlOutputter.outputString(document);
        logger.debug("nlsml=" + output);

        // Conversion to JSON automatically converts byte[] to Base64
        // (equivalent of
        // Base64.encodeBase64(output.getBytes()))

        return output;
    }
}
