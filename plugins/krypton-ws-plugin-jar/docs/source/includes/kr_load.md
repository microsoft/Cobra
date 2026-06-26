## Load

> This Load example shows a wordset package, a wordset, a domain LM, and a semantic model being added to the session. Domain LMs and wordsets must be activated for use in Recognize commands with useWordSets and domainLmWeights. You can upload only one semantic model, you don't have to activate it.

```json
{
  "method": "Load",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "objects": [
    {"id": "wordset_pkg_id",
     "type": "application/x-nuance-wordset-pkg",
     "url": "urn:...",
     "maxAge etc."
    },
    {"id": "wordset_id",
     "type": "application/x-nuance-wordset",
     "body": "{inline wordset definition}"
    },
    {"id": "domainlm_id",
     "type": "application/x-nuance-domainlm",
     "url": "urn:...",
     "maxAge etc."
    },
    {"id": "semanticmodel_id",
     "type": "application/x-nuance-semantic-model",
     "url": "urn:nuance-mix:tag:model/{tag}/mix.nlu?=language={lang}",
     "maxAge etc."
    }
  ]
}
```

> An example of loading a semantic model without type.

```json
{
  "method": "Load",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "objects": [
    {"id": "semanticmodel_id",
     "url": "urn:nuance-mix:tag:model/{tag}/mix.nlu?=nlptype=nle;language={lang}",
     "maxAge etc."
    }
  ]
}
```

Loads adds one or more recognition objects—domain LMs and wordsets—to the current session. It can also add a maximum of one interpretation object—semantic model—to the session. After being loaded, domain LMs and wordsets must be activated in a [Recognize](#recognize-and-interpret) command in order to affect speech recognition. Semantic models do not need to be activated. There is no explicit way of unloading objects. Objects are unloaded when a session terminates.

Use these properties to configure the Load command.

| Property  | Type   | Value                                                        |
| --------- | ------ | ------------------------------------------------------------ |
| method    | String | Load                                                         |
| version   | String | (Optional) Protocol version: 1.0. Default is set in config.  |
| requestId | String | (Optional) Request identifier. If not supplied, NVC generates a unique request ID as a UUID. Allowed characters are 0-9 a-z A-Z - . _ @ |
| sessionId | String | Session identifier from CreateSession response.              |
| objects   | Array  | An array of objects: wordsets, domain LMs, speaker profiles and/or a semantic model to load for subsequent Recognize/Interpretation commands. For a description of each object, see CreateSession - [Wordset objects](#wordset-objects), [Domain LM objects](#domain-lm-objects), [Speaker profile objects](#speaker-profile-objects) and [Semantic model objects](#semantic-model-objects). |

### Response to Load

> Initial response to Load

```json
{
  "method": "Response",
  "responseTo": "Load",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "in-progress|failed"
}
```

> Final response to a successful Load operation

```json
{
  "method": "Response",
  "responseTo": "Load",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete",
  "objects": [
    {"id": "ws_branches"},
    {"id": "dlm_bank"}
  ]
}
```

> Error response

```json
{
  "method": "Response",
  "responseTo": "Load",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "failed",
  "errors" : [{
    "message" : "Object id already in use",
  }]
}
```

> Cookie response example

```json
{
  "method": "Response",
  "responseTo": "Load",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete",
  "objects": [
    {"id": "ws_branches",
     "id": "dlm_bank",
     "setCookie": ["cookie_header_1", "cookie_header_2"]
    },
  ]
}
```

>  Metrics in a final Load response

```json
{
  "method": "Response",
  "responseTo": "Load",
  "version": "1.0", 
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete",
  "objects": [
    {"id": "some_id1"},
    {"id": "some_id2"}
  ],
  "metrics": {
    "timestamps": [ "20170517141147559", "20170517141147676" ],
    "elapsed": 117,
    "cpu": { "user": 200, "sys": 0 }
  }
}
```

Load is an asynchronous command. Krypton's initial response indicates only whether the command was understood and started correctly, returning either in-progress or failed.

Later, a final response carries the outcome of the command. In case of success, the response includes an array in which the objects appear in the same order as the request, along with their identifiers (either client-supplied or automatically generated by the server).

### Cookies 

Any object loaded via an http or https URL may include Set-Cookie headers in the response from the server. These header values are passed verbatim to the client as an additional setCookie property on the returned object. 

Krypton support for cookies is limited to transport between the protocol client and the HTTP server responding to a loaded object URL. Cookies are not parsed, validated, stored, or managed in any cookie jar. The client is responsible to enforce any domain limits appropriately in requests.

### Load metrics

The initial in-progress response to a Load request supports the basic metrics.

### Loading resources

This table summarizes the different ways of loading Krypton resources.

| Description               | Code                                                         |
| ------------------------- | ------------------------------------------------------------ |
| Domain LM from web server | `{"id": "dlm_bank",`<br />&#160;&#160;`"url": "http[s]://host/path/banking-dlm.zip",`<br />&#160;&#160;`"type": "application/x-nuance-domainlm"}` |
| Wordset defined inline    | `{"id": "ws_places",`<br />&#160;&#160;`"body": "{\"PLACES\":[{\"literal\":\"Abington Pigotts\"},...]}",`<br />&#160;&#160;`"type": "application/x-nuance-wordset"}` |
| Wordset package from web server | `{"id": "ws_pkg_places",`<br />&#160;&#160;`"url": "urn:http[s]://host/path/places-ws-pkg.zip",`<br />&#160;&#160;`"type": "application/x-nuance-wordset-pkg"}` |
| Semantic model with type attribute set | `{"id": "sm_places",`<br />&#160;&#160;`"url": "urn:nuance-mix:tag:model/{tag}/mix.nlu?=language={lang}",`<br />&#160;&#160;`"type": "application/x-nuance-semantic-model"}` |
| Semantic model without type attribute set | `{"id": "sm_places",`<br />&#160;&#160;`"url": "urn:nuance-mix:tag:model/{tag}/mix.nlu?=nlptype=nle;language={lang}"}` |