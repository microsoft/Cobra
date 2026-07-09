/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.vap.messages;


import java.util.LinkedList;
import java.util.List;

/**
 * @author epigeon
 *
 */
public class VAPRecordMessage extends VAPMessage {
    private List<byte[]> audio = new LinkedList<>();
    private int size = 0;

    /**
     * @param version
     */
    public VAPRecordMessage(short version) {
        super(version, VAPMessage.VAP_RECORD);
    }

    /**
     * @param audio
     *          the audio to set
     */
    public void addAudio(byte[] audio) {
        if (audio != null) {
            this.audio.add(audio);
            size += audio.length;
        }
    }

    /**
     * @return the audio
     */
    public List<byte[]> getAudio() {
        return audio;
    }

    public int getPackageSize() {
        return size;
    }

    public String toString() {
        if (audio != null) {
            return super.toString() + " length: " + audio.size();
        } else {
            return super.toString();
        }
    }
}
