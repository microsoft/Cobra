package com.nuance.plugin.audio;

public class PassThroughAudioEncoder extends AudioEncoder {
    @Override
    public byte[] onAudio(byte[] data) {
        return data;
    }

    @Override
    public void onDone() {
        //nothing to do here
    }
}
