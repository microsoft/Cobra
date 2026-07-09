# API reference
The API commands for Nuance Vocalizer for Cloud are based on a client-server protocol within a WebSocket connection. 

![Websocket communication](Websocket-communication.jpg)

The commands are: 

* <a href="#getinfo">GetInfo</a>: Obtain information about the Vocalizer for Cloud instance.  
* CreateSession: Start a session within the WebSocket connection. 
* Load: Optionally add more resources to specialize the text-to-speech synthesis.  
* Synthesize: Turn text into speech.
* [EndSession](#endsession): Terminate an idle session.

<aside class="notice">
This sample has been shortened for brevity.
</aside>


## Generic message format
> Generic message structure

``` json
{
  "method" : "name",
  "version" : "x.y",
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "numberPropertyName" : 123456,
  "booleanPropertyName" : true|false,
  "stringPropertyName" : "value",
  "arrayPropertyName" : ["value1", "value2", "value3"],
  "objectParameterName" : {
    "name1" : "value1",
    "name2" : "value2"}
}
```

The Vocalizer for Cloud API protocol is based on UTF-8 text messages expressed in JSON, with the exception of the synthesized audio that Vocalizer returns to the client as binary WebSocket frames. 


### Common parameters
These parameters are supported in most commands. 

<table>
    <tr>
        <th>Parameter</th>
        <th>Type</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>method</td>
        <td>String</td>
        <td>Method name.</td>
    </tr>
    <tr>
        <td>version</td>
        <td>String</td>
        <td>(Optional) Protocol version. Default is 1.0.</td>
    </tr>
    <tr>
        <td><a name="sessionid"></a>sessionId</td>
        <td>String</td>
        <td>Session identifier. Optional in CreateSession. If not supplied in CreateSession, Vocalizer generates a unique session ID as a UUID in the canonical form, for example: 123e4567-e89b-12d3-a456-426655440000. Allowed characters are 0-9 a-z A-Z - . _ @</td>
    </tr>
    <tr>
        <td><a name="requestid"></a>requestId</td>
        <td>String</td>
        <td>(Optional) Request identifier. If not supplied, Vocalizer generates a unique request ID as a UUID.</p>
        Including a requestId makes it easier to associate commands with responses, which have the same ID. Allowed characters are 0-9 a-z A-Z - . _ @</td>
    </tr>
    <tr>
        <td><i>parameters</i></td>
        <td>All</td>
        <td>Parameters with all basic JSON data types are supported: number, string, Boolean, array, and object. Any unknown parameters are silently discarded.</td>
    </tr>
</table>


## GetInfo
> GetInfo example

``` json
{
  "method": "GetInfo",
  "version": "1.0",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

GetInfo retrieves information about the Vocalizer for Cloud instance, including the maximum number of clients and the installed voice models.

### Request
These parameters configure the GetInfo command.

| Parameter | Type        | Description                                                                                                                                   |
| --------- | ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------|
| method    | String      | Method name: GetInfo.                                                                                                                         |
| version   | String      | (Optional) Protocol version. Default is 1.0.                                                                                                  |
| requestId | String      | (Optional) Request identifier. If not supplied, Vocalizer generates a unique request ID as a UUID. Allowed characters are 0-9 a-z A-Z - . _ @ |


### Response
> GetInfo response 

``` json
{
  "method": "Response",
  "responseTo": "GetInfo",
  "version": "1.0",
  "requestId": "98d232d0-4ab1-11e9-995b-a91289a14d52",
  "status": "complete",
  "address": "hostname:8080",
  "maxclients": 200,
  "serverVersion": "7.2.7.4473bbd53a5b",
  "capabilities":[  
    {  
      "language": "en-us",
      "voice": "Samantha",
      "sampleRate": 22050,
      "model": "full_vssq5f22",
      "age": "Adult",
      "gender": "Female",
      "version": "5.2.3.14268"
    },
    {
     "language": "en-us",
     "voice": "Samantha",
     "sampleRate": 8000,
     "model": "full_encryptf8",
     "age": "Adult",
     "gender": "Female",
     "version": "5.2.3.14268"
     }
   ],
  "errors":[]
}
```

The response to GetInfo includes identification properties, the capabilities of the Vocalizer instance, and any errors. 

## EndSession
> EndSession example

``` json
{
  "method": "EndSession",
  "version": "1.0",   
  "sessionId": "9843443b-a989-4afb-900b-99c36029658d",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
}
```

EndSession terminates an inactive session. If the session is active, this command fails. 

<table>
  	<tr>
    	<th>Parameter</th>
    	<th>Type</th>
    	<th>Description</th>
  	</tr>
  	<tr>
		<td>method</td>
		<td>String</td>
		<td>Method name: EndSession.</td>
	</tr>
	<tr>
		<td>version</td>
		<td>String</td>
		<td>(Optional) Protocol version. Default is 1.0.</td>
	</tr>
	<tr>
		<td>sessionId </td>
		<td>String </td>
		<td>Session identifier from previous CreateSession response.</td>
	</tr>
	<tr>
		<td>requestId</td>
		<td>String</td>
		<td>(Optional) Request identifier. If not supplied, Vocalizer generates a unique request ID as a UUID. Allowed characters are 0-9 a-z A-Z - . _ @</td>
	</tr>
</table>

### Response
> EndSession response

``` json
{  
   "method": "EndSession",
   "responseTo": "Load",
   "requestId": "ba119b46-4cd3-4d7c-92d4-06ead6d76e23",
   "sessionId": "9843443b-a989-4afb-900b-99c36029658d",
   "status": "complete|failed",
   "version": "1.0",
   "errors":[]
}
```

The response to EndSession includes identification properties, status (complete or failed), and any errors. 
