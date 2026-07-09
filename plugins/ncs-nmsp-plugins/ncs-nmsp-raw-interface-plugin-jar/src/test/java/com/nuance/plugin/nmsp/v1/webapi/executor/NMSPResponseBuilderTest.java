package com.nuance.plugin.nmsp.v1.webapi.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.messages.PDXQueryResult;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxException;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.Dictionary;
import com.nuance.plugin.nmsp.v1.session.Session;
import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import io.netty.channel.Channel;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NMSPResponseBuilderTest
{
    public void buildAppServerPostResult(String commandName)
            throws JsonProcessingException, PdxException
    {
        String webapiResponseString = "{\"type\":\"result\",\"sessionId\":\"37f949f0-708c-436d-8c1f-1b442054ef7b\",\"command\":{\"name\":\"interpretation\",\"id\":\"NMAS_PRFX_TRANSACTION_ID:2\"},\"result\":{\"final\":true,\"status\":\"success\",\"appserver_results\":{\"payload\":{\"text\":\"activer le youtube\",\"intent\":\"service-youtube-open\",\"nlps_version\":\"nlps(z):6.4.300.16-B10;Version: nlps-base-Zeppelin-6.4.300-B00-GMT20171206222658;\",\"command\":\"YouTube\",\"command_parameter\":\"open\",\"diagnostic_info\":{\"adk_dialog_manager_status\":\"undefined\",\"nlps_profile\":\"DTV-BCK-COMPATIBILITY\",\"nlps_profile_package_version\":\"r180030\",\"third_party_delay\":\"2\",\"nlu_annotator\":\"urn:analysisEngineDescription:default?endPoint=qNVCCP%5FDTV%5Ffr%2Dfr&brokerURL=tcp%3A%2F%2Flocalhost%3A61616&timeout=35000\",\"nlps_nlu_type\":\"nlu_preproc\",\"ext_map_time\":\"0\",\"nlu_component_flow\":\"[Flow|+SWISlotAnnotator]\",\"nlps_profile_package\":\"DTV-BCK-COMPATIBILITY\",\"application\":\"LG_TV_NLU_20_20140429\",\"nlu_language\":\"fra-FRA\",\"int_map_time\":\"1\",\"nmaid\":\"LG_TV_NLU_20_20140429\",\"nlu_version\":\"[NLU_PROJECT:DTV-fra-FRA];[Datapack:Version: nlps-fra-FRA-DTV-3.7.B32-GMT20171031193820];[VL-Models:Version: vlmodels-DTV-fra-FRA-3.7.B32-GMT20171031193110]\",\"nlu_use_literal_annotator\":\"0\",\"fieldId\":\"dm_main\"}},\"final_response\":1,\"status\":\"success\"}}}";
        String queryInfoString = "{ \"gender\":\"male\", \"_IS_LEGACY_CLIENT_\":true, \"client_hardware_model\":\"iPhone 3G\", \"lcd_height\":1, \"language\":\"enus\", \"pdx_version\":\"1\", \"locale\":\"canada\", \"original_session_id\":\"\", \"uid\":\"\", \"application_name\":\"Nmsp Sample Application\", \"device_codec\":278, \"nmaid\":\"nina4\", \"client_version\":\"J2SE_OR_J2ME_OS_VERSION\", \"audio_source\":\"SpeakerAndMicrophone\", \"field_id\":\"dm_main\", \"dictation_type\":\"DTV\", \"nlu_slot_results\":1, \"phone_OS\":\"4.0\", \"lcd_width\":1, \"dictation_language\":\"fra-FRA\", \"command\":\"DRAGON_NLU_APPSERVER_CMD\", \"phone_model\":\"iPhone 3G\", \"carrier\":\"ATT\", \"application\":\"LG_TV_DTV_PHASE2_20130411\", \"application_session_id\":\"d2093c04-e41a-4b6d-afe4-915eecbad359\", \"utterance_number\":\"5\", \"send_nbest_categories\":0, \"location\":\"<+42.5047, -71.1961> +/- 2252.00m\", \"script_version\":\"1.0\", \"phone_number\":\"\", \"phone_network\":\"wifi\", \"region\":\"ne\" }";

        WebApiResponse webApiResponse = WebApiResponse.fromJson(webapiResponseString);

        JSONObject queryInfoJson = new JSONObject(queryInfoString);
        Map<String, Object> queryInfoMap = queryInfoJson.toMap();

        Channel mockChannel = mock(Channel.class);
        WebAPIOrchestrator mockWebAPIOrchestrator = mock(WebAPIOrchestrator.class);
        when(mockWebAPIOrchestrator.getTransactionID()).thenReturn((short)1);
        Session nmspSession = new Session(mockChannel, mockWebAPIOrchestrator);

        PDXQueryResult pdxQueryResult = NMSPResponseBuilder.buildPdxNLUResult(webApiResponse, queryInfoMap, nmspSession, commandName);
        Dictionary appserverResultsDict = pdxQueryResult.getDictionary("appserver_results");
        Dictionary payload = appserverResultsDict.getDictionary("payload");
        assertEquals("activer le youtube", payload.getUTF8String("text"));
        assertEquals("service-youtube-open", payload.getUTF8String("intent"));
    }

    @Test
    public void buildAppServerPostResultNlu()
            throws JsonProcessingException, PdxException
    {
        buildAppServerPostResult("DRAGON_NLU_APPSERVER_CMD");
    }

    @Test
    public void buildAppServerPostResultAsrNlu()
            throws PdxException, JsonProcessingException
    {
        buildAppServerPostResult("DRAGON_NLU_ASR_CMD");
    }
}