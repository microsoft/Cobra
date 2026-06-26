/**
 * 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp;

/**
 * @author epigeon
 *
 */
public class InvalidMessageException extends RuntimeException
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public InvalidMessageException()
  {
  }

  /**
   * @param message
   */
  public InvalidMessageException(String message)
  {
    super(message);
  }

  /**
   * @param cause
   */
  public InvalidMessageException(Throwable cause)
  {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public InvalidMessageException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
