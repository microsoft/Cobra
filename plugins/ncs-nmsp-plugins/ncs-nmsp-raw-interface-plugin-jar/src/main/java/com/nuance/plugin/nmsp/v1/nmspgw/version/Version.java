package com.nuance.plugin.nmsp.v1.nmspgw.version;

public class Version {
  
	private int version = 0x00;
	
	
	public Version(int version) {
		this.version = version;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		return (int)(((this.version & 0x000000f0) >> 4))+ "." + (int)(this.version & 0x0000000f);
	}
	
	@Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + version;
    return result;
  }
	
	@Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Version other = (Version) obj;
    if (version != other.version)
      return false;
    return true;
  }
}
