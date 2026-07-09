package com.nuance.plugin.v1.shareable;

import java.util.List;
import java.util.Map;

public interface ISnmpTrapSender {

    /**
     * Sends an snmp trap
     * @param trapMessage  The list of traps to be sent.
     */
    void sendTrap(List<Map<Integer, String>> trapMessage);
}
