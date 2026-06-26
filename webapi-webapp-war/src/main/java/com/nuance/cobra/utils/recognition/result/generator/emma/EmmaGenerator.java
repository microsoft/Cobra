package com.nuance.cobra.utils.recognition.result.generator.emma;

import com.nuance.cobra.utils.recognition.result.generator.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.List;

public class EmmaGenerator implements RecognitionResultGenerator {
    private static Logger logger = LogManager.getLogger(EmmaGenerator.class);

    private XMLOutputter xmlOutputter;

    private boolean addRecoSource;

    public EmmaGenerator() {
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

        Namespace emmaNs = Namespace.getNamespace("emma", "http://www.w3.org/TR/2007/CR-emma-20071211");
        Namespace nuanceNs = Namespace.getNamespace("nuance", "http://nr10.nuance.com/emma");
        Element emmaEl = new Element("emma", emmaNs);
        emmaEl.addNamespaceDeclaration(nuanceNs);
        emmaEl.setAttribute("version", "1.0");

        String grammarId = "grammar_1";
        if (activeGrammar != null) {
            Element grammarEl = new Element("grammar", emmaNs);
            grammarEl.setAttribute("id", grammarId);
            grammarEl.setAttribute("ref", activeGrammar.getId());
            emmaEl.addContent(grammarEl);
        }

        StringBuilder tokens = new StringBuilder();
        if (intent != null) {
            tokens.append(intent.getValue());
        }
        if (variables != null) {
            for (Variable variable : variables) {
                tokens.append(" " + variable.getValue());
            }
        }

        Element interpretationEl = new Element("interpretation", emmaNs);
        interpretationEl.setAttribute("id", "interp_1");
        Attribute confidenceAttr = new Attribute("confidence", "1", emmaNs);
        interpretationEl.setAttribute(confidenceAttr);
        Attribute grammarRefAttr = new Attribute("grammar-ref", grammarId, emmaNs);
        interpretationEl.setAttribute(grammarRefAttr);
        Attribute tokensAttr = new Attribute("tokens", tokens.toString(), emmaNs);
        interpretationEl.setAttribute(tokensAttr);
        Attribute modeAttr = new Attribute("mode", "voice", emmaNs);
        interpretationEl.setAttribute(modeAttr);

        if (intent != null) {
            Element intentEl = new Element(intent.getId());
            intentEl.setAttribute("conf", "1");
            intentEl.addContent(intent.getValue());
            interpretationEl.addContent(intentEl);
        }

        if (variables != null) {
            for (Variable variable : variables) {
                Element variableEl = new Element(variable.getId());
                variableEl.setAttribute("conf", "1");
                variableEl.addContent(variable.getValue());
                interpretationEl.addContent(variableEl);
            }
        }

        if (globals != null) {
            for (String global : globals) {
                Element globalEl = new Element(global);
                globalEl.setAttribute("conf", "1");
                globalEl.addContent("true");
                interpretationEl.addContent(globalEl);
            }
        }

        Element swiLiteralEl = new Element("SWI_literal");
        swiLiteralEl.addContent(tokens.toString());
        interpretationEl.addContent(swiLiteralEl);

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
        interpretationEl.addContent(swiMeaningEl);

        Element ssmScoreEl = new Element("ssm_score");
        ssmScoreEl.setAttribute("conf", "1");
        ssmScoreEl.addContent("100");
        interpretationEl.addContent(ssmScoreEl);


        if (addRecoSource) {
            Element recoSourceEl = new Element("reco_source");
            recoSourceEl.addContent("NAA");
            interpretationEl.addContent(recoSourceEl);
        }

        emmaEl.addContent(interpretationEl);

        Document document = new Document(emmaEl);

        Format format = Format.getCompactFormat();
        // Format format = Format.getPrettyFormat();
        // Format format = Format.getRawFormat();
        xmlOutputter.setFormat(format);

        String output = xmlOutputter.outputString(document);
        logger.debug("emma=" + output);

        // Conversion to JSON automatically converts byte[] to Base64
        // (equivalent of
        // Base64.encodeBase64(output.getBytes()))

        return output;
    }

}
