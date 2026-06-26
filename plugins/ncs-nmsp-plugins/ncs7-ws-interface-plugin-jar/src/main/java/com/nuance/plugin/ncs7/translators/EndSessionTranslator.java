package com.nuance.plugin.ncs7.translators;

import com.nuance.cobra.utils.webapi.WebApiResponse;
import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.json.NcsResponse;
import org.json.JSONObject;

import javax.websocket.CloseReason;
import java.io.IOException;

public class EndSessionTranslator extends AbstractCommandTranslator {

    private NcsResponse ncsResponse;

    public EndSessionTranslator(Orchestrator orchestrator, NcsResponse ncsResponse) {
        super(orchestrator, null);
        this.ncsResponse = ncsResponse;
    }

    @Override
    public void execute() throws IOException {
        if (orchestrator.getWebapiSessionID() != null
                && orchestrator.getWebSocketSession().isOpen()
                && orchestrator.getWebApi().getWebapiSession(orchestrator.getWebapiSessionID()) != null) {
            doRequest();
            waitForResponse();
        } else {
            doResponse(null);
        }
    }

    @Override
    public void commandCleanup() {
        //nothing for now
    }

    private void doRequest() throws IOException
    {
        JSONObject webApiRequest = WebApiCommandsBuilder.buildBasicCommand("endSession", orchestrator.getWebapiSessionID(), getCommandID(ncsResponse));

        sendToWebapi(orchestrator, webApiRequest.toString(), false);
    }

    ExecutionResult doResponse(WebApiResponse webApiResponse) throws IOException {
        if (ncsResponse != null) {
            respondToClient(orchestrator, ncsResponse.toJson());
        }
        orchestrator.setWebapiSessionID(null);

        if (orchestrator.getProtocolVersion() == Orchestrator.ProtocolVersion.v1) {
            orchestrator.getWebSocketSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Connection closed after client disconnect message."));
            // then com.nuance.plugin.ncs7.NCS7InterfacePlugin.onSocketClosed is called automatically
        }

        return new ExecutionResult(ExecutionStatus.SUCCESS, true);
    }
}
