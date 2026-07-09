## Stop

> Generic Stop request

```json
{
  "method": "Stop",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

> This stops any Load or Recognize commands in sessionId. 

```json
{
  "method": "Stop", 
  "version": "1.0",
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

> Response to Stop

```json
{
  "method": "Response",
  "responseTo": "Stop",
  "version": "1.0",   
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete | failed | no-action",
  "errors": []
}
```

Stop terminates (aborts) any asynchronous commands (Load or Recognize) that are currently running. Once stopped, asynchronous commands do not emit further events of any kind. Clients should still be prepared to receive events already in transit when Stop is received.

Use these properties to configure the Stop command.

| Property  | Type   | Value                                                        |
| --------- | ------ | ------------------------------------------------------------ |
| method    | String | Stop                                                         |
| version   | String | (Optional) Protocol version: 1.0. Default is set in config.  |
| sessionId | String | Session identifier from CreateSession response.              |
| requestId | String | (Optional) Request identifier.                               |

Krypton's response to Stop includes a status code:

| Status    | Case                                            | Action taken        |
| --------- | ----------------------------------------------- | ------------------- |
| complete  | Asynchronous command running.                   | Command is stopped. |
| no-action | Session idle, no asynchronous commands running. | No action.          |
| failed    | Asynchronous command stopping.                  | No action.          |