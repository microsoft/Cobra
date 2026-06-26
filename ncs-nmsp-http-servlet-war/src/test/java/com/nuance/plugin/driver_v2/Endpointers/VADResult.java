package com.nuance.plugin.driver_v2.Endpointers;

public class VADResult implements IVADResult {
    public int result;
    public long pos;

	@Override
	public void setPosInMs(long ms) {
		pos = ms;
	}

	@Override
	public long getPosInMs() {
		return pos;
	}

	@Override
	public void setResult(int res) {
		result = res;
	}

	@Override
	public int getResult() {
		return result;
	}

}
