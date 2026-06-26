## Protocol details

> Generic message structure

```json
{
  "method" : "name",
  "version" : "1.0",
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "numberPropertyName" : 123456,
  "booleanPropertyName" : true|false,
  "stringPropertyName" : "value",
  "arrayPropertyName" : ["value1", "value2", "value3"],
  "objectPropertyName" : {
    "name1" : "value1",
    "name2" : "value2"}
}
```

The API protocol is based on UTF-8 text messages expressed in JSON, with the exception of audio data chunks, which are binary messages. JSON is used to represent text messages.

These properties are supported in most commands. 

| Property     | Type   | Value                                                        |
| ------------ | ------ | ------------------------------------------------------------ |
| method       | String | Method name.                                                 |
| version      | String | (Optional) Protocol version: 1.0. The default is set in config. |
| sessionId    | String | Session identifier. <p>Mandatory in EndSession, Load, Recognize, StartRecognitionTimers, Stop, EndOfInput.</p><p>Optional in CreateSession. When not supplied, Krypton generates a unique session ID as a UUID, for example: 123e4567-e89b-12d3-a456-426655440000.</p> |
| requestId    | String | (Optional) Request identifier. Allowed characters are 0-9 a-z A-Z - . _ @. If not supplied, Krypton generates a unique request ID as a UUID. Including a requestId makes it easier to associate commands with responses, which have the same ID. |
| *properties* | All    | Properties with all basic JSON data types are supported: number, string, Boolean, array, and object. Any unknown properties are silently discarded. |

### Time designations

Time designations consist of a non-negative real number followed by a time unit identifier. The time unit identifiers are:

- ms: milliseconds
- s: seconds

Examples include: 0s, 3s, 850ms, 0.7s, .5s, and +1.5s.

### Protocol basics

Krypton uses a client-server protocol, with support for synchronous and asynchronous commands. The commands are issued within a WebSocket connection.

Clients send commands to the Krypton instance, which responds to each command with a message indicating whether the command was started or completed successfully. For synchronous commands, such as CreateSession, EndSession, etc., Krypton responds with the result (when needed) as well as the status or errors.  

For asynchronous commands such as Load and Recognize, Krypton's response indicates only whether the command’s parameters were accepted. Asynchronous commands usually take a longer and unpredictable time to complete, depending on the characteristics of the input being processed. While asynchronous commands are running, Krypton delivers results periodically and may emit events requested by the client. Krypton then delivers one final response to signal the end of the command.

### Overlapping commands

Synchronous commands do not support overlapping. While a synchronous command is running, other commands are not executed until the previous one is complete.  

The asynchronous Load and Recognize commands support overlapping in certain conditions. 

- Audio data and StartRecognitionTimers are allowed with Recognize. 
- Stop and EndOfInput are also allowed with Recognize.
- Stop is allowed with Load. 

### Session scope

A session needs to be created on the WebSocket connection prior to running commands, except for the CreateSession command which is used to create the actual session. A WebSocket connection can have at most one session opened at a time, but can be reused to host subsequent sessions, after the current one was closed using the EndSession command.

Parameters set or resources loaded in CreateSession are said to have *session scope*, meaning they apply to all recognition commands during the session. Most resources and settings can be overridden for an individual recognition.

The session cannot exceed that of the WebSocket connection on which it was created. A session ends when any of the following occur: 

- An EndSession command is issued.
- The session reaches its configured idle timeout.
- The WebSocket connection is closed.

Sessions should always be gracefully terminated by an [EndSession](#endsession) command when no longer used.