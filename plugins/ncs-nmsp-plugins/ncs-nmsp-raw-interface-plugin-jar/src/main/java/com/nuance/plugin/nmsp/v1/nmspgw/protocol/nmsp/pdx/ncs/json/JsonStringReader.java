/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json;

import java.io.StringReader;
import java.util.Stack;

public class JsonStringReader extends StringReader
{
  private Stack<Integer> pushed;

  public JsonStringReader(String v)
  {
    super(v);
    pushed = new Stack<Integer>();
  }

  public int peek() throws JsonException
  {
    try
    {
      if (pushed.size() > 0)
      {
        return pushed.peek();
      }
      else
      {
        super.mark(0);
        int singleChar = super.read();
        super.reset();
        return singleChar;
      }
    }
    catch (Exception e)
    {
      throw new JsonException(e);
    }
  }

  public int readChar() throws JsonException
  {
    try
    {
      if (pushed.size() > 0)
      {
        return pushed.pop();
      }
      else
      {
        super.reset();
        int singleChar = super.read();
        super.mark(0);
        return singleChar;
      }
    }
    catch (Exception e)
    {
      throw new JsonException("Caught exception", e);
    }
  }

  public void push(int v)
  {
    pushed.push(v);
  }
}
