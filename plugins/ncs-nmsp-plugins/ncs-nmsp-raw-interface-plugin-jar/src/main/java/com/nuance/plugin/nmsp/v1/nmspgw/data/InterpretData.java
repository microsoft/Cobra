/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.data;

import java.util.Vector;

/**
 * @author epigeon
 *
 */
public class InterpretData {
  private String text;
  private String poolName;
  private Vector<Parameter> parameters;
  private Vector<Grammar> grammars;

  /**
   * @return the text
   */
  public String getText()
  {
    return text;
  }

  /**
   * @param text
   *          the text to set
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * @return the poolName
   */
  public String getPoolName()
  {
    return poolName;
  }

  /**
   * @param poolName
   *          the poolName to set
   */
  public void setPoolName(String poolName)
  {
    this.poolName = poolName;
  }

  /**
   * @return the grammars
   */
  public Vector<Grammar> getGrammars()
  {
    return grammars;
  }

  /**
   * @param grammars
   *          the grammars to set
   */
  public void setGrammars(Vector<Grammar> grammars)
  {
    this.grammars = grammars;
  }

  /**
   * @return the parameters
   */
  public Vector<Parameter> getParameters()
  {
    return parameters;
  }

  /**
   * @param parameters
   *          the parameters to set
   */
  public void setParameters(Vector<Parameter> parameters)
  {
    this.parameters = parameters;
  }
}
