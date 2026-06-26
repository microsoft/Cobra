## Response

> Generic Response example

```json
{
  "method": "Response",
  "responseTo": "method_name",
  "version": "1.0",
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",   
  "status": "complete" | "in-progress" | "failed",
  "errors": [
    {"code": "aaa.bbb.ccc",
     "message": "An error has occurred",
     "extra": "Additional error description",
     "severity": "fatal"
    }
  ]
}
```

Response is a generic response by Krypton to a supported command.  Each response is associated with its respective command using the responseTo property and optionally requestId if specified in the original command. In case of failure, an optional array of error objects is returned. Responses may include additional properties.

These properties are part of the Response command. 

| Property | Type   | Value                                              |
| ---------- | ------ | ------------------------------------------------------------ |
| method     | String | Response                                                     |
| responseTo | String | Name of the method that originated this response.            |
| version    | String | The protocol version: 1.0 |
| sessionId  | String | Session ID.                                                  |
| requestId  | String | Request ID, if available in the original request.            |
| status     | String | The status of the response, one of: <ul><li>failed: The command failed. </li><li>complete: The command processed successfully).</li>no-action: The command resulted in no action taken in the current state. </li><li>in-progress: An asynchronous command was successfully started, results to follow.</li></ul> |
| errors     | Array  | Objects representing errors occurred while executing the request. Optional. |
| &#160;&#160;&#160;code     | String                  |Error code.|
| &#160;&#160;&#160;message  | String    |Error message. Optional.|
| &#160;&#160;&#160;extra    | String |Additional information. Optional.|
| &#160;&#160;&#160;severity | String |Severity of the error. Optional.|