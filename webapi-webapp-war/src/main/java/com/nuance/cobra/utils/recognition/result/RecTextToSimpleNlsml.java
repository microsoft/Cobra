package com.nuance.cobra.utils.recognition.result;

import com.nuance.cobra.client.response.SpeechRecoResponse;
import com.nuance.cobra.utils.logging.EsapiLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import java.util.ArrayList;
import java.util.Arrays;

/* We want to end up with something like:
<?xml version="1.0" encoding="UTF-8"?>
<result grammar="session:0x00000001">
   <interpretation grammar="session:0x00000001" confidence="0.976">
      <instance />
      <input mode="voice">Pay $500 on my Visa bill</input>
   </interpretation>
   <interpretation grammar="session:0x00000001" confidence="0.001">
      <instance />
      <input mode="voice">Pay $500 on my visa bill</input>
   </interpretation>
   <interpretation grammar="session:0x00000001" confidence="0.001">
      <instance />
      <input mode="voice">$500 on my Visa bill</input>
   </interpretation>
</result>
 */

@SuppressWarnings("java:S125")
public class RecTextToSimpleNlsml
{
    private static Logger esapiLogger = ESAPI.getLogger(RecTextToSimpleNlsml.class);

    private RecTextToSimpleNlsml() { throw new IllegalStateException("Utility class!"); }

    public static String recTextToSimpleNlsml(SpeechRecoResponse.Data recTextResultData, float maxConfidence, String matchedGrammar)
    {
        StringBuilder sb = new StringBuilder();
        fillStart(sb, matchedGrammar);

        ArrayList<String> transcriptions = new ArrayList<>(Arrays.asList(recTextResultData.getTranscriptions()));
        ArrayList<Integer> confidences = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(recTextResultData.getAverageWeightedConfidences())));
        while (!confidences.isEmpty())
        {
            int topConfidence = -1;
            int topConfidenceIndex = -1;
            for (int i = 0; i < confidences.size(); i++)
            {
                if (confidences.get(i) >= topConfidence)
                {
                    topConfidenceIndex = i;
                    topConfidence = confidences.get(i);
                }
            }

            float confidence = topConfidence;
            confidence /= maxConfidence;
            fillInterpretation(sb, transcriptions.get(topConfidenceIndex), confidence, matchedGrammar);

            confidences.remove(topConfidenceIndex);
            transcriptions.remove(topConfidenceIndex);
        }

        //String[] transcriptions = new ArrayList<String>(recTextResultData.getTranscriptions());
        //int[] confidences = recTextResultData.getAverageWeightedConfidences();
        //for (int i = 0; i < transcriptions.length; i++)
        //{
        //    float confidence = confidences[i];
        //    confidence /= maxConfidence;
        //    fillInterpretation(sb, transcriptions[i], confidence);
        //}

        fillEnd(sb);
        EsapiLogger.debug(esapiLogger, "recTextToSimpleNlsml: %s", sb);
        return sb.toString();
    }

    private static void fillStart(StringBuilder sb, String matchedGrammar)
    {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        String resultTag = String.format("<result grammar=\"%s\">", matchedGrammar);
        sb.append(resultTag);
    }

    private static void fillInterpretation(StringBuilder sb, String input, float confidence, String matchedGrammar)
    {
        sb.append("<interpretation grammar=\"" + matchedGrammar + "\" confidence=\"" + confidence + "\">");
        sb.append("<instance/>");
        sb.append("<input mode=\"voice\">" + input + "</input>");
        //sb.append("<input mode=\"voice\" confidence=\"" + confidence + "\">" + input + "</input>");
        sb.append("</interpretation>");
    }

    private static void fillEnd(StringBuilder sb)
    {
        sb.append("</result>");
    }
}
