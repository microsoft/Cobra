package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

public enum CommandBeginKeys
{
  APPLICATION("application"),
  COMMAND("command"),
  UID("uid"),
  PDX_VERSION("pdx_version"),
  /**
   * @deprecated CLIENT_VERSION is replaced by CLIENT_OS_VERSION, but kept for compatibility with older clients (US6302) 
   */
  CLIENT_VERSION("client_version"),
  CLIENT_OS_VERSION("client_os_version"),
  /**
   * @deprecated SCRIPT_VERSION is replaced by CLIENT_APP_VERSION, but kept for compatibility with older clients (US6302) 
  */
  SCRIPT_VERSION("script_version"),
  CLIENT_APP_VERSION("client_app_version"),
  REGION("region"),
  LCD_WIDTH("lcd_width"),
  LCD_HEIGHT("lcd_height"),
  CARRIER("carrier"),
  /**
    * @deprecated PHONE_MODEL is replaced by CLIENT_HARDWARE_MODEL, but kept for compatibility with older clients (US6302) 
  */
  PHONE_MODEL("phone_model"),
  CLIENT_HARDWARE_MODEL("client_hardware_model"),
  PHONE_NUMBER("phone_number"),
  NMAID("nmaid"),
  DEVICE_ID("device_id"),
  APPLICATION_ID("application_id"),
  CLIENT_ID("client_id"),
  /**
   * @deprecated PHONE_OS is replaced by CLIENT_OS_TYPE, but kept for compatibility with older clients (US6302) 
  */
  PHONE_OS("phone_OS"),
  CLIENT_OS_TYPE("client_os_type");

  private final String value;

  private CommandBeginKeys(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
