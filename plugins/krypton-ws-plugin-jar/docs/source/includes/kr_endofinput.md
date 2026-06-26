## EndOfInput

> Generic EndOfInput example

```json
{
  "method": "EndOfInput",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

> This signals the end of audio input for the session named in sessionId. 

```json
{
  "method": "EndOfInput", 
  "version": "1.0",
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

> Response to EndOfInput

```json
{
  "method": "Response",
  "responseTo": "EndOfInput",
  "version": "1.0",   
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete | failed | no-action",
  "errors": []
}
```

EndOfInput signals that the client will send no more audio data for the current recognition turn. Typically used with client-side endpointing, push-to-talk, and so on.

Use these properties to configure the EndOfInput command.

| Property  | Type   | Value                                                        |
| --------- | ------ | ------------------------------------------------------------ |
| method    | String | EndOfInput                                                   |
| version   | String | (Optional) Protocol version: 1.0. Default is set in config.  |
| sessionId | String | Session identifier from CreateSession response.              |
| requestId | String | (Optional) Request identifier.                               |

Krypton's response to EndOfInput includes a status code:

| Status    | Case                                                         | Action taken           |
| --------- | ------------------------------------------------------------ | ---------------------- |
| complete  | Recognize command running, no previous end of input received. | End of input accepted. |
| no-action | Recognize command running, end of input already received.    | No action.             |
| no-action | Session idle.                                                | No action.             |
| failed    | Session not idle, processing commands other than Recognize.  |                        |