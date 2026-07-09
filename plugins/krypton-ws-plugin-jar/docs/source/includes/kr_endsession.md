## EndSession

> Generic EndSession request

```json
{
  "method": "EndSession",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

> This closes the session named in sessionId.

```json
{
  "method": "EndSession", 
  "sessionId": "0417ef12-7613-40aa-8c5f-dfa103e1fd77"
}
```

> Response to EndSession

```json
{
  "method": "Response",
  "responseTo": "EndSession",
  "version": "1.0",   
  "sessionId": "0417ef12-7613-40aa-8c5f-dfa103e1fd77",
  "requestId": "0417ef12-7613-40aa-8c5f-dfa103e1fd77",
  "status": "complete"
}
```

EndSession terminates a session that exists and is still alive on the receiving Krypton websocket connection.

Use these properties to configure the EndSession command.

| Property                   | Type    | Value                                                        |
| -------------------------- | ------- | ------------------------------------------------------------ |
| method                     | String  | EndSession                                                   |
| version                    | String  | (Optional) Protocol version: 1.0. Default is set in config.  |
| sessionId                  | String  | Session identifier from CreateSession response.              |
| requestId                  | String  | (Optional) Request identifier.                               |

Krypton's response to EndSession includes a status code: complete or failed. 

