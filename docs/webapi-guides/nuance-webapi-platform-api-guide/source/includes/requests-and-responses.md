## JSON requests and responses

A request contains a JSON object that is either a command or a directive. Commands start an
activity. Directives stop or cancel a running command. 

Responses are JSON objects that return the results and the status of a command, or that signal
an event associated with a command.

## Commands

A typical format for a command is:

> Command example

``` json
{
  "type": "command",
  "sessionId": "SESSION_ID",
  "command": {
    "name": "COMMAND_NAME",
    "id": "CLIENT_DEFINED_STEP_ID",
    "parameters": {
      "PARAM_BOOL": true | false,
      "PARAM_NUM": INTEGER,
      "PARAM_STRING": "TEXT",
      ...
    }
  }
}
```


###  Command fields
<table>
    <tr>
        <th>Input</th>
        <th>Summary</th>
    </tr>
    <tr>
        <td>sessionID</td>
        <td>The server-generated session ID, returned by the startSession command and required by all following commands.
            command</td>
    </tr>
    <tr>
        <td>command</td>
        <td>JSON command object.</td>
    </tr>
    <tr>
        <td>name</td>
        <td>Name of the command, required.</td>
    </tr>
    <tr>
        <td>id</td>
        <td>Client generated step ID, required.</td>
    </tr>
    <tr>
        <td>parameters</td>
        <td>The command parameters. Can be omitted if the command doesn't require any parameters.</td>
    </tr>
</table>


## Directives

Directives stop or cancel an actively executing command. They do not take any parameters:

> Directive example

``` json
{
    "type": "directive",
    "sessionId": "SESSION_ID",
    "command": {
        "name": "COMMAND_NAME",
        "id": "CLIENT_DEFINED_STEP_ID"
    },
    "directive": {
        "name": "{stop | cancel}"
    }
}
```


### Results

Results take the following form:

> Result example

``` json 
{
    "type": "result",
    "sessionId": "SESSION_ID",
    "command": {
        "id": "CLIENT_DEFINED_STEP_ID",
        "name": "COMMAND_NAME"
    },
    "result": { ... }
}
```

#### Successful Results

A successful result indicates whether the result is a final result (final is true) or an
intermediate result (final is false):

> Successful result example

``` json

"result": {
    "final": true,
    "status": "success"
}
```

#### Error Results

An error result provides an array of error codes and associated messages, for example:

> Error result example

``` json 
"result": {
    "final": true,
    "status": "error",
    "errors": [
        { "code": 1500, "message": "SpeechSynthesis failed." }, 
        ...
    ]
}
```

### Events

Events are returned during speech recognition as described in the Speech recognition overview. 

Events take the following form:

> Event example

``` json
{
    "type": "event",
    "sessionId": "SESSION_ID",
    "command": {
        "name": "COMMAND_NAME",
        "id": "CLIENT_DEFINED_STEP_ID"
    },
    "event": {
        "name": "EVENT_NAME"
    }
}
```