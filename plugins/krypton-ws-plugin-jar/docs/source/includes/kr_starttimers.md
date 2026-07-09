## StartRecognitionTimers

> Generic StartRecognitionTimers request

```json
{
  "method": "StartRecognitionTimers",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

> This starts timers for the specified session

```json
{
  "method": "StartRecognitionTimers", 
  "sessionId": "1f132ec9-6cb5-4e57-af1c-72309e14746c"
}
```

> Response to StartRecognitionTimers

```json
{
  "method": "Response",
  "responseTo": "StartRecognitionTimers",
  "version": "1.0",   
  "requestId": "21a69f6b-db92-47e7-ab8b-edf145bf35b2",
  "sessionId": "1f132ec9-6cb5-4e57-af1c-72309e14746c",
  "status": "complete | failed | no-action",
  "errors": []
}
```

StartRecognitionTimers starts timers for the current recognition turn, if they are not already running.  

Use these properties to configure the StartRecognitionTimers command.

| Property  | Type   | Value                                                        |
| --------- | ------ | ------------------------------------------------------------ |
| method    | String | StartRecognitionTimers                                       |
| version   | String | (Optional) Protocol version: 1.0. Default is set in config.  |
| sessionId | String | Session identifier from CreateSession response.              |
| requestId | String | (Optional) Request identifier.                               |

Krypton's response to StartRecognitionTimers includes a status code:

| Status    | Case                                                        | Action taken        |
| --------- | ----------------------------------------------------------- | ------------------- |
| complete  | Recognize command running, timers not running.              | Timers are started. |
| no-action | Recognize command running, timers already running.          | No action.          |
| no-action | Session idle.                                               | No action.          |
| failed    | Session not idle, processing commands other than Recognize. |                     |