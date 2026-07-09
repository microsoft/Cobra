/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.serializers;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.*;
import com.nuance.plugin.nmsp.v1.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author epigeon
 * 
 */
public class BCPMessageSerializer
{
  private BCPMessageSerializer()
  {
  }

  public static ByteBuf serialize(final Session session, final BCPMessage message)
  {
    switch (message.getCommand())
    {
      case BCPMessage.BCP_RESPONSE:
        return BCPResponseSerializer.serialize((BCPResponseMessage) message);
      case BCPMessage.BCP_GENERATE_AUDIO_COMPLETE:
        return BCPGenerateAudioCompleteSerializer.serialize((BCPGenerateAudioCompleteMessage) message);
      case BCPMessage.BCP_START_OF_SPEECH:
        return BCPStartOfSpeechSerializer.serialize((BCPStartOfSpeechMessage) message);
      case BCPMessage.BCP_RECOGNITION_COMPLETE:
        return BCPRecognitionCompleteSerializer.serialize((BCPRecognitionCompleteMessage) message);
      case BCPMessage.BCP_RECOGNITION_INTERMRESULTS:
        return BCPRecognitionIntermediateResultsSerializer.serialize((BCPRecognitionIntermediateResultsMessage) message);
      case BCPMessage.BCP_SET_PARAMS_COMPLETE:
        return BCPSetParamsCompleteSerializer.serialize((BCPSetParamsCompleteMessage) message);
      case BCPMessage.BCP_GET_PARAMS_COMPLETE:
        return BCPGetParamsCompleteSerializer.serialize((BCPGetParamsCompleteMessage) message);
      case BCPMessage.BCP_CANCEL_RESPONSE:
        return BCPCancelResponseSerializer.serialize((BCPCancelResponseMessage) message);
      case BCPMessage.BCP_RETRIEVE_ARTIFACTS_COMPLETE:
        return BCPRetrieveArtifactsCompleteSerializer.serialize((BCPRetrieveArtifactsCompleteMessage)message);
      case BCPMessage.BCP_EVENT:
        return BCPEventSerializer.serialize((BCPEventMessage) message);
      case BCPMessage.BCP_INTERPRETATION_COMPLETE:
        return BCPInterpretationCompleteSerializer.serialize((BCPInterpretationCompleteMessage) message);
      case BCPMessage.BCP_END_OF_SPEECH:
        return BCPEndOfSpeechSerializer.serialize((BCPEndOfSpeechMessage) message);
      case BCPMessage.BCP_LOAD_RESOURCE_COMPLETE:
        return BCPLoadResourceCompleteSerializer.serialize((BCPLoadResourceCompleteMessage) message);
      case BCPMessage.BCP_DATA:
        return BCPDataSerializer.serialize((BCPDataMessage) message);
      case BCPMessage.BCP_BEGIN:
        return BCPBeginSerializerRegistry.getBCPBeginSerializer(message.getVersion()).serialize(session, (BCPBeginMessage) message);
      case BCPMessage.BCP_RESOURCE_TUNNEL:
        return BCPResourceTunnelSerializer.serialize((BCPResourceTunnelMessage) message);
      case BCPMessage.BCP_TRANSCODE_COMPLETE:
        return BCPTranscodeCompleteSerializer.serialize((BCPTranscodeCompleteMessage) message);
      case BCPMessage.BCP_LOG_RESPONSE:
        return BCPLogResponseSerializer.serialize((BCPLogResponseMessage) message);
      case BCPMessage.BCP_MDS_MESSAGE:
        return BCPMdsMessageSerializer.serialize((BCPMdsMessage) message);
      case BCPMessage.BCP_MDS_MESSAGE_RESPONSE:
        return BCPMdsMessageResponseSerializer.serialize((BCPMdsMessageResponse) message);
      default:
        return Unpooled.EMPTY_BUFFER;
    }
  }

}
