package com.nuance.nmsp.servlet.nmas.upload;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;

public class UploadXMLParser
{
    private static final Logger log = ESAPI.getLogger(UploadXMLParser.class);

    static final String DATA = "data";
    static final String DELETEALL = "deleteall";
    static final String ID = "id";
    static final String CHECKSUM = "checksum";
    static final String OLD_CHECKSUM = "previous_checksum";
    static final String TYPE = "type";
    static final String ACTIONS = "actions";
    static final String CLEARALL = "clearall";
    static final String ADD = "add";
    static final String CONTACTS = "contacts";
    static final String CONTACT = "contact";
    static final String FIRSTNAME = "firstname";
    static final String LASTNAME = "lastname";
    static final String FULLNAME = "fullname";
    static final String NICKNAME = "nickname";
    static final String LOCATIONS = "locations";
    static final String LOCATION = "location";
    static final String REMOVE = "remove";
    static final String WORDS = "words";
    static final String WORD = "word";
    static final String CUSTOM_WORDS = "custom_words";
    static final String HISTORY = "history";

    @SuppressWarnings("java:S135")
    public DataBlock parseXML(InputStream in)
    {
        DataBlock datablock = new DataBlock();

        try
        {
            // First create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            // Read the XML document
            Data data = null;
            Action action = null;
            Contact contact = null;
            Location loc = null;

            while (eventReader.hasNext())
            {
                XMLEvent event = eventReader.nextEvent();
                loc = event.getLocation();

                if (event.isStartElement())
                {
                    StartElement startElement = event.asStartElement();

                    if (startElement.getName()
                                    .getLocalPart()
                                    .equals(DELETEALL))
                    {
                        datablock.setDeleteAll(true);
                    }

                    if (startElement.getName()
                                    .getLocalPart()
                                    .equals(DATA))
                    {
                        data = new Data();
                    }

                    if (event.isStartElement() && event.asStartElement()
                                                       .getName()
                                                       .getLocalPart()
                                                       .equals(ID))
                    {
                        event = eventReader.nextEvent();
                        data.setId(event.asCharacters()
                                        .getData());
                        continue;
                    }
                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(CHECKSUM))
                    {
                        event = eventReader.nextEvent();
                        data.setChecksum(event.asCharacters()
                                              .getData());
                        continue;
                    }
                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(OLD_CHECKSUM))
                    {
                        event = eventReader.nextEvent();
                        data.setOldChecksum(event.asCharacters()
                                                 .getData());
                        continue;
                    }
                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(TYPE))
                    {
                        event = eventReader.nextEvent();
                        String type = event.asCharacters()
                                           .getData();

                        if (type.equals(CONTACTS) || type.equals(CUSTOM_WORDS) || type.equals(HISTORY))
                        {
                            data.setType(type);
                        }
                        else
                        {
                            // Report error
                            datablock.setErrorMsg(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    "<" + TYPE + "> tag must be one of these values:" +
                                    CONTACTS + "," +
                                    CUSTOM_WORDS + "," +
                                    HISTORY);
                            type = null;
                            break;
                        }
                        continue;
                    }

                    if (startElement.getName()
                                    .getLocalPart()
                                    .equals(CLEARALL))
                    {
                        action = new Action(Action.CLEAR_ALL);
                        if (data != null)
                        {
                            data.addAction(action);
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    CLEARALL,
                                    DATA);
                            break;
                        }
                        action = null;
                    }

                    if (startElement.getName()
                                    .getLocalPart()
                                    .equals(ADD))
                    {
                        action = new Action(Action.ADD);
                    }

                    if (startElement.getName()
                                    .getLocalPart()
                                    .equals(REMOVE))
                    {
                        action = new Action(Action.REMOVE);
                    }


                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(WORD))
                    {
                        event = eventReader.nextEvent();
                        if (action != null)
                        {
                            action.addWord(event.asCharacters()
                                                .getData());
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    WORD,
                                    ADD + "> or <" + REMOVE);
                            break;
                        }
                        continue;
                    }

                    if (startElement.getName()
                                    .getLocalPart()
                                    .equals(CONTACT))
                    {
                        contact = new Contact();
                    }

                    if (event.isStartElement() && event.asStartElement()
                                                       .getName()
                                                       .getLocalPart()
                                                       .equals(FIRSTNAME))
                    {
                        event = eventReader.nextEvent();
                        if (contact != null)
                        {
                            contact.setFirstName(event.asCharacters()
                                                      .getData());
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    FIRSTNAME,
                                    CONTACT);
                            break;
                        }
                        continue;
                    }

                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(LASTNAME))
                    {
                        event = eventReader.nextEvent();
                        if (contact != null)
                        {
                            contact.setLastName(event.asCharacters()
                                                     .getData());
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    LASTNAME,
                                    CONTACT);
                            break;
                        }
                        continue;
                    }

                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(FULLNAME))
                    {
                        event = eventReader.nextEvent();
                        if (contact != null)
                        {
                            contact.setFullName(event.asCharacters()
                                                     .getData());
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    FULLNAME,
                                    CONTACT);
                            break;
                        }
                        continue;
                    }

                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(NICKNAME))
                    {
                        event = eventReader.nextEvent();
                        if (contact != null)
                        {
                            contact.setNickName(event.asCharacters()
                                                     .getData());
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    NICKNAME,
                                    CONTACT);
                            break;
                        }
                        continue;
                    }

                    if (event.asStartElement()
                             .getName()
                             .getLocalPart()
                             .equals(LOCATION))
                    {
                        event = eventReader.nextEvent();
                        if (contact != null)
                        {
                            contact.addLocation(event.asCharacters()
                                                     .getData());
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    LOCATION,
                                    CONTACT);
                            break;
                        }
                        continue;
                    }
                }
                // ------------------------------------------------------------
                // If we reach the end of an tag element
                // we add it to the corresponding list
                // ------------------------------------------------------------
                if (event.isEndElement())
                {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName()
                                  .getLocalPart()
                                  .equals(CONTACT))
                    {
                        // Add contact to actions
                        if (action != null)
                        {
                            action.addContact(contact);
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    "/" + CONTACT,
                                    ADD + "> or <" + REMOVE);
                            break;
                        }
                        contact = null;
                    }
                    else if (endElement.getName()
                                       .getLocalPart()
                                       .equals(ADD) ||
                             endElement.getName()
                                       .getLocalPart()
                                       .equals(REMOVE))
                    {
                        // Add action to data
                        if (data != null)
                        {
                            data.addAction(action);
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    "/" + ADD + "> or </" + REMOVE,
                                    DATA);
                            break;
                        }
                        action = null;
                    }
                    else if (endElement.getName()
                                       .getLocalPart()
                                       .equals(DATA))
                    {
                        if (data != null)
                        {
                            // Add data to datablock
                            if (datablock != null)
                            {
                                if (!data.haveId())
                                {
                                    // Report error
                                    datablock.setMissingTagError(loc.getLineNumber(),
                                            loc.getColumnNumber(),
                                            "/" + DATA,
                                            ID);
                                    break;
                                }
                                if (!data
                                        .haveChecksum())
                                {
                                    // Report error
                                    datablock.setMissingTagError(loc.getLineNumber(),
                                            loc.getColumnNumber(),
                                            "/" + DATA,
                                            CHECKSUM);
                                    break;
                                }

                                if (!data.haveType())
                                {
                                    // Report error
                                    datablock.setMissingTagError(loc.getLineNumber(),
                                            loc.getColumnNumber(),
                                            "/" + DATA,
                                            TYPE);
                                    break;
                                }

                                datablock.addData(data);
                            }
                        }
                        else
                        {    // Report error
                            datablock.setMissingTagError(loc.getLineNumber(),
                                    loc.getColumnNumber(),
                                    "/" + DATA,
                                    DATA);
                            break;
                        }
                        data = null;
                    }
                }
            } // while
            eventReader.close();
        }
        catch (XMLStreamException e)
        {
            log.info(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception e1)
            {
                log.trace(Logger.EVENT_UNSPECIFIED, "Exception  ..." + e1.getMessage(), e1);
            }
        }
        return datablock;
    }
}
