/**
 * COPYRIGHT (c) 2011 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.deserializers;

import com.nuance.plugin.nmsp.v1.nmspgw.data.Grammar;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.NMSPSAXHandler;
import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.bcp.messages.BCPLoadGrammarMessage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author epigeon
 * 
 */
public class BCPLoadGrammarSAXHandler extends NMSPSAXHandler
{
  private BCPLoadGrammarMessage message;
  private Grammar grammar;

  public BCPLoadGrammarSAXHandler(BCPLoadGrammarMessage message)
  {
    this.message = message;
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
    super.startElement(uri, localName, qName, attributes);
    if (qName.toLowerCase().matches("g\\d+"))
    {
      this.grammar = new Grammar();
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException
  {
    int intValue;
    
    try
    {
      if (qName.equalsIgnoreCase("rid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'rid' in BCPLoadGrammarMessage");
        }
        message.setRequestId(intValue);
      }
      else if (qName.equalsIgnoreCase("resid"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'resid' in BCPLoadGrammarMessage");
        }
        message.setResourceId(intValue);
      }
      else if (qName.equalsIgnoreCase("p"))
      {
        message.setPoolName(text.toString());
      }
      else if (qName.equalsIgnoreCase("i"))
      {
        grammar.setId(text.toString());
      }
      else if (qName.equalsIgnoreCase("d"))
      {
        intValue = Integer.parseInt(text.toString());
        if(intValue < 0) {
          throw new SAXException("negative value not allowed for 'd' in BCPLoadGrammarMessage");
        }
        grammar.setIndex(intValue);
      }
      else if (qName.equalsIgnoreCase("t"))
      {
        Grammar.Type type = Grammar.convertToType(text.toString());
        if (type != null)
        {
          grammar.setType(type);
          // Validate that the content type set through the type is correct
          if (grammar.getContentType() == null)
          {
            throw new SAXException("invalid grammar type for load grammar request in BCPLoadGrammarMessage");
          }
        }
        else
        {
          throw new SAXException("invalid grammar type in BCPLoadGrammarMessage");
        }
      }
      else if (qName.toLowerCase().matches("g\\d+"))
      {
        message.getGrammars().add(grammar);
      }
    }
    catch (NumberFormatException e)
    {
      throw new SAXException("Invalid XML payload in BCPLoadGrammarMessage " + e.getMessage());
    }
  }
}
