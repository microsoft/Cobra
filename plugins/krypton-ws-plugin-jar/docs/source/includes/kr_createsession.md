## CreateSession
> Generic CreateSession request

```json
{
  "method": "CreateSession",
  "version": "1.0", 
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "languagePack": {
    "language": "en-us",
    "topic": "travel"
  },
  "clientData": {
    "companyName": "COMPANY",
    "applicationName": "APPLICATION",
    "applicationVersion": "d.e.f"
  },
  "sessionParameters": {
    "idleTimeout": "3600s",
    "disableCallRecording": false
  },  
  "recognitionParameters": {
    "startRecognitionTimers": false,
    "audioFormat": "audio/L16;rate=8000",
    "other parameters"
  },
  "sessionObjects": [
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
     "weight": 10,
     "maxAge etc."
    },
    {"id": "not_required",
     "type": "application/x-nuance-speaker-profile"
    }
    {"id": "not_required",
     "type": "application/x-nuance-semantic-model",
     "url": "urn:...",
     "maxAge etc."
    }
  ]
}
```

> See also the examples in [Wordsets](#wordsets), [Domain LMs](#domain-lms), [Speaker profiles](#speaker-profiles), and [Semantic models](#semantic-model-objects).

CreateSession opens a new session in a Krypton WebSocket connection that has no active session. Multiple concurrent sessions per connection are not supported, so this command fails if the connection already has one. A connection can be reused to host subsequent sessions, after the current one has been closed using the EndSession command.

The CreateSession command must indicate the language pack to use (base language and specialization topic). This information cannot be changed once the session is established.

Use these properties to configure the CreateSession command.

| Property                                           | Type    | Value                                                                                                                                                                                                                                                                                                                             |
| -------------------------------------------------- | ------- |-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| method                                             | String  | CreateSession                                                                                                                                                                                                                                                                                                                     |
| version                                            | String  | (Optional) Protocol version: 1.0. Default is set in config.                                                                                                                                                                                                                                                                       |
| sessionId                                          | String  | (Optional) Session identifier. When not specified, Krypton generates a UUID.                                                                                                                                                                                                                                                      |
| requestId                                          | String  | (Optional) Request identifier.                                                                                                                                                                                                                                                                                                    |
| hybrid                                             | Object  | (For NLPS) Integration values for NLPS.                                                                                                                                                                                                                                                                                           |
| &#160;&#160;&#160;clientId                         | String  | Used for authentication. Maps to a WebAPI tenant configuration. If not present, {clientData/companyName} + _ + {clientData/applicationName} is used instead to point to a tenant configuration.                                                                                                                                   |
| &#160;&#160;&#160;dialogSessionId                  | String  | Used to track the NSS dialog session identifier. Passed down to Core Tech engines as 'x-dialog-session-id'. Used for billing, for example.                                                                                                                                                                                                             |
| &#160;&#160;&#160;secret                           | String  | Authentication data. If not present, {trust/signature} is used instead.                                                                                                                                                                                                                                                           |
| trust                                              | Object  | (For NLPS) For authentication.                                                                                                                                                                                                                                                                                                    |
| &#160;&#160;&#160;uuid                             | String  | Not used.                                                                                                                                                                                                                                                                                                                         |
| &#160;&#160;&#160;signature                        | String  | Authentication data. Can be overridden by {hybrid/secret}                                                                                                                                                                                                                                                                         |
| languagePack                                       | Object  | Language pack to activate for the session. Server is configured to load one or more language packs at startup time. When establishing a new session, clients need to specify which one to use. Language packs cannot be dynamically loaded.                                                                                       |
| &#160;&#160;&#160;language                         | String  | Language and locale identifier in the form xx-xx (not case-sensitive), for example "en-us". <br />Codes in the form xxx-XXX, e.g. "eng-USA" are also supported for backward compatibility.                                                                                                                                        |
| &#160;&#160;&#160;topic                            | String  | (Optional) Language model name. Default is data-pack dependent. The value is case-sensitive.                                                                                                                                                                                                                                      |
| clientData                                         | Object  | Client identification data.                                                                                                                                                                                                                                                                                                       |
| &#160;&#160;&#160;companyName                      | String  | Company name.                                                                                                                                                                                                                                                                                                                     |
| &#160;&#160;&#160;applicationName                  | String  | Application name.                                                                                                                                                                                                                                                                                                                 |
| &#160;&#160;&#160;applicationVersion               | String  | Application version.                                                                                                                                                                                                                                                                                                              |
| <a name="session-parameters"></a>sessionParameters | Object  | (Optional) Collection of properties and values that apply to the entire session. All properties are optional.                                                                                                                                                                                                                     |
| &#160;&#160;&#160;idleTimeout                      | String  | (Optional) Maximum duration that a session may stay idle (*idle* means no running methods and no messages received). Default is no timeout. Once the timeout expires, the session is automatically terminated.                                                                                                                    |
| &#160;&#160;&#160;disableCallRecording             | Boolean | (Optional) Control whether the call recording should be disabled for the incoming session. When set to true, the session will not be recorded at all. If call recording feature is disabled in the configuration, parameter will have no effect. Default: false.                                                                  |
| recognitionParameters                              | Object  | (Optional) Defaults for recognition parameters in the Recognize command. Properties set here become session defaults but may be overridden in Recognize. The defaults are set by Krypton and may be customized in config. For a description of each recognition parameter, see the [Recognize](#recognize-and-interpret) command. |
| sessionObjects                                     | Array   | (Optional) An array of objects: wordsets, domain LMs, speaker profiles, and/or a semantic model to load for the duration of the session. For a description of each object, see next.                                                                                                                                              |

### Wordset objects

> Wordsets in CreateSession or Load

```json
  {"sessionObjects | objects"}  
    {"id": "package_id",
     "type": "application/x-nuance-wordset-pkg",
     "url": "urn:...",
     "maxAge etc."
    },
    {"id": "wordset_id",
     "type": "application/x-nuance-wordset",
     "body": "{inline wordset definition}"
    }
  }
```

To use wordsets in recognition turns for the entire session, load them with CreateSession - sessionObjects along with their associated domain LMs. 

To use wordsets in specific  recognition turns in the session, load them with Load - objects then activate them in Recognize. 

| Property    | Type   | Value                                                                                                                                                                                                                                                                                                                                                                                                              |
| ----------- | ------ |--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id          | String | (Optional) A unique identifier for the wordset being loaded. If not specified, Krypton generates one automatically. Allowed characters are 0-9a-zA-Z-._@. This ID is used later to activate wordsets in Recognize commands.                                                                                                                                                                                        |
| type        | String | Content type of the object being loaded. See [Wordsets](#wordsets) for instructions on creating these objects. There are three wordset types: <ul><li>**application/x-nuance-wordset-pkg**: A compiled wordset for ASR. </li><li>**application/x-nuance-wordset-nle-pkg**: A compiled wordset for NLU. </li><li/>**application/x‑nuance‑wordset**: An inline wordset specified in body as a JSON object.</li></ul> |
| url         | String | Mandatory for wordset packages. Must be reachable by Core Tech ASRaaS and NLUaaS. Must start with "urn:". Cannot be used with 'x‑nuance‑wordset'                                                                                                                                                                                                                                                                   |
| body        | String | Mandatory for inline wordsets. The actual inline wordset as JSON. Cannot be used with 'x‑nuance‑wordset-pkg'                                                                                                                                                                                                                                                                                                       |
| maxAge etc. | String | (Optional) For wordsets packages loaded from a URL, caching and timeout options may be specified. See [maxAge etc.](#maxage-etc) These options are not supported for inline wordset definitions.                                                                                                                                                                                                                   |

### Domain LM objects

> DLMs in CreateSession or Load

```json
  {"sessionObjects | objects"}  
    {"id": "domainlm_id",
     "type": "application/x-nuance-domainlm",
     "url": "urn:...",
     "weight": 10,
     "maxAge etc."
    }
}
```

To use domain LMs in recognition turns in the session, add them with CreateSession - sessionObjects or Load - objects. You may optionally adjust their importance in Recognize by changing their weight. 

<table>
                <tr>
                    <th>Property</th>
                    <th>Type</th>
                    <th>Value</th>
                </tr>
                <tr>
                    <td>id</td>
                    <td>String</td>
                    <td>(Optional) A unique identifier for the DLM being loaded. Optional. If not specified, Krypton generates one automatically. Allowed characters are 0-9a-zA-Z-._@. For DLMs added with Load, this ID is used later to activate the DLM in Recognize commands. </td>
                </tr>
                <tr>
                    <td>type</td>
                    <td>String</td>
                    <td>Content type of the object: application/x-nuance-domainlm    </td>
                </tr>
                <tr>
                    <td>url</td>
                    <td>String</td>
                    <td>URL of the domain language model. The URL must start with "urn:"</td>
                </tr>
                <tr>
                    <td>weight</td>
                    <td>Integer 0-1000</td>
                    <td>The weight of the DLM, or its importance compared to the underlying data pack, one of lowest, low, medium, high, highest, or an integer 0-1000. Default is 100. The weight may be overridden in Recognize commands with domainLmWeights.     </td>
                </tr>
                <tr>
                    <td>maxAge etc.</td>
                    <td>String</td>
                    <td>(Optional) Caching and timeout options for the DLM being loaded. Optional. See maxAge, etc. </td>
                </tr>
</table>

### Speaker profile objects

> Speaker profiles in CreateSession

```json
  {"sessionObjects | objects"}      
    {"id": "not_required",
     "type": "application/x-nuance-speaker-profile"
    }
  }
```

To enable speaker adaptation data in the session, include a speaker profile in CreateSession - sessionObjects. Speaker profiles cannot be added with Load. 

Unlike the other recognition objects (wordsets and domain LMs), this object cannot be altered in Load or activated in Recognize. Once specified, speaker adaptation is in effect for the entire session. 

| Property    | Type   | Value                                                        |
| ----------- | ------ | ------------------------------------------------------------ |
| id          | String | (Not required) A unique identifier is not required for speaker profiles. If not specified, Krypton generates one automatically, but this identifier is not used in later commands. |
| type        | String | Content type of the object: application/x-nuance-speaker-profile |
| url         | String | A speaker profile URL is actually not required. Speaker adaptation is automatically keyed on the user ID.  |

### Semantic model objects

> Semantic model in CreateSession or Load

```json
  {"sessionObjects | objects"}      
    {"id": "not_required",
     "type": "application/x-nuance-semantic-model",
     "url": "urn:nuance-mix:tag:model/{tag}/mix.nlu?=language={lang}"
    }
  }
```

> Semantic model with no type

```json
  {"sessionObjects | objects"}      
    {"id": "not_required",
     "url": "urn:nuance-mix:tag:model/{tag}/mix.nlu?=nlptype=nle;language={lang}"
    }
  }
```

Semantic models support the conversion of text to meaning (NLU). To enable automatic interpretation of the recognition result during [Recognize](#recognize-and-interpret), include a semantic model in CreateSession - sessionObjects - or Load - objects.

Only one semantic model can be loaded per session.

Unlike recognition objects (wordsets and domain LMs), a semantic model cannot be altered in Load or de/activated in Recognize. Once specified, it is in effect for the entire session.

| Property    | Type   | Value                                                           |
| ----------- | ------ | ------------------------------------------------------------    |
| id          | String | (Not required) A unique identifier for the semantic model.      |
| type        | String | (Optional) Content type of the object: application/x-nuance-semantic-model. You can omit this property, but if you do you need to add "nlptype=nle" to the url. See examples below. |
| url         | String | URL of the semantic model. Can either be: <ul><li>urn:nuance-mix:tag:model/{tag}/mix.nlu?=language={lang}</li><li>urn:nuance-mix:tag:model/{tag}/mix.nlu?=nlptype=nle;language={lang} if you omit the type</li></ul> |
| maxAge etc. | String | (Optional) Caching and timeout options for the speaker profile being loaded. See maxAge etc. next. |

### maxAge etc. 

> Generic maxAge, etc. properties with a domain LM

```json
    {"id": "dlm_bank",
     "type": "application/x-nuance-domainlm",
     "url": "urn:...",
     "weight": 10,
     "maxAge": 2628000,
     "maxStale": 604800,
     "maxFresh": 604800,
     "cookies": "cookie123, cookie234",
     "requestTimeoutMs": 20000
    }
```

Objects loaded with the url property may include caching and timeout options. These options do not apply to wordsets specified inline with the body property. All properties are optional. 

| Property         | Type    | Value                                                        |
| ---------------- | ------- | ------------------------------------------------------------ |
| maxAge           | Integer | RFC7234 cache-control directive. Indicates that the client is unwilling to accept a response whose age is greater than the specified number of seconds. |
| maxStale         | Integer | RFC7234 cache-control directive. Indicates that the client is willing to accept a response that has exceeded its freshness lifetime. If maxStale is assigned a value, then the client is willing to accept a response that has exceeded its freshness lifetime by no more than the specified number of seconds. |
| maxFresh         | Integer | RFC7234 cache-control directive. Indicates that the client is willing to accept a response whose freshness lifetime is no less than its current age plus the specified time in seconds. |
| cookies          | String  | One or more cookies to send in a Cookie header of HTTP request. Applies only when url protocol is http or https. |
| requestTimeoutMs | Integer | A timeout in milliseconds for completing the fetch of an HTTP request for the url value. Overrides default value from configuration file. <br />When version is 1.0, requestTimeout is accepted. |

### Response to CreateSession

> Successful response to CreateSession

```json
{
  "method": "Response",
  "responseTo": "CreateSession",
  "version": "1.0",   
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete",
  "sessionObjects": [
    {"id": "ws_names"},
    {"id": "dlm_bank"}
  ]
}
```

> Response with metrics

```json
{
  "method": "Response",
  "responseTo": "CreateSession",
  "version": "1.0",
  "requestId": "bc4b7d9b-d6bc-4da0-8ca8-a613c7347943",
  "metrics": {
    "timestamps": ["20200610090600956", "20200610090600972"],
    "elapsed": 16,
    "cpu": {
      "user": 200,
      "sys": 100
    }
  },
  "status": "complete",
  "sessionId": "5d78cb63-8880-43fb-a947-8749d076b705",
  "sessionObjects": [
    {"id": "ws_names"},
    {"id": "dlm_bank"}
  ]
}
```

Krypton's response to CreateSession includes standard identification, a status (complete or failed), and the objects loaded into the session. 

When CreateSession results in session scope objects being created, the response includes the same metrics specified for the Load command. See [Metrics](#metrics).

### Session scope objects

Recognition objects (wordsets, domain LMs, semantic model, and speaker profiles) loaded in CreateSession are said to have *session scope*, meaning they are loaded at the start of the session and available throughout the session. This operation makes sense when these objects are reused across multiple recognition turns. 

Wordsets, domain LMs, and a semantic model may also be loaded in the Load command, in which case they apply only to subsequent Recognize commands. Speaker profiles always have session scope, as they must be specified for the session in CreateSession, and cannot be altered or referenced in Load or Recognize.

Session scope DLMs are activated by default in every recognition turn (with the weight specified in sessionObjects) even if they are not specified in the Recognize command.

In Recognize, the IDs of session scope DLMs can be used to modify the weight. For example, the session scope domain LMs can be disabled by setting their weight to 0.

Specifying a default weight of 0 in sessionObjects means the DLM is loaded but disabled unless the Recognize command gives a weight greater than 0.

By default, session scope wordsets are activated in every recognition turn, even if they are not specified in Recognize - useWordSets. These wordsets cannot be disabled, although you may lower their probability by setting the weight to 0 for the DLM that defines the wordset’s entity.