
# Command Chain

##  Chain multiple commands together

Command chaining permits running multiple commands as a single operation. This is useful for cases where the application needs to be ready to provide a quick response to user actions or speech input. To use one command's result as input to a subsequent command, you can use JSON pointers. For example, the JSON pointer <em>(0:result.bestResult)</em> will access the first command's result (as defined by the 0 index).

<!--- Use HTML notation with and a unique id attribute, since the ### markdown notation will result in links conflicts in TOC -->
<h3 id="heading_chain_parameters">Parameters</h3>

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| type | `String` | <p>The type of request.</p>_Allowed values: "commandChain"_ |
| sessionId | `String` | **optional** <p>The server generated sessionId. This field is optional. You must not set a sessionId if the first command of the chain is a startSession command.</p> |
| chainId | `String` | **optional** <p>Identifies a chain. If none is provided, cobra will generate a unique id.</p> |
| metadata | `Object` | **optional** <p>A map of metadata associated with the chain.</p>_Must be a json map_ |
| commands | `Object[]` | <p>The commands to be chained. Multiple command objects can be placed in the commands array. Individual command objects remain unchanged. This means that you can easily copy-paste between standard commands and command chains.</p> |

> Example request (w/ Start Session)

```json

{
  "type": "commandChain",
  "commands": [
    {
      "name": "startSession",
      "id": "1",
      "parameters": {
        "applicationKey": "...",
        "verificationCode": "...",
        "speechSynthesisCodec": "pcm_16_16k",
        "speechRecognitionCodec": "pcm_16_16k"
      }
    },
    {
      "name": "speechSynthesis",
      "id": "2",
      "parameters": {
        "type": "text",
        "data": "good morning",
        "voice": "Carol"
      }
    }
  ]
}
```
> Example request (w/o Start Session)

```json

{
  "type": "commandChain",
  "sessionId": "873315e5-d8bd-45a1-aa35-7cb47827e6ec",
  "commands": [
    {
      "name": "speechRecognition",
      "id": "1",
      "parameters": {
        "language": "en-us",
        "endPointDetection": true,
        "startOfSpeechHistoryFrames": 15,
        "startOfSpeechVoicedFrames": 7,
        "endOfSpeechHistoryFrames": 50,
        "endOfSpeechVoicedFrames": 5,
        "stopOnEndOfSpeech": true,
        "wordStream": true
      }
    },
    {
      "name": "speechSynthesis",
      "id": "2",
      "parameters": {
        "type": "text",
        "data": "_(result.bestResult)_",
        "voice": "Carol"
      }
    }
  ]
}
```
> Example request (many commands)

```json

{
  "type": "commandChain",
  "commands": [
    {
      "name": "startSession",
      "id": "1",
      "parameters": {
        "applicationKey": "...",
        "verificationCode": "...",
        "speechSynthesisCodec": "pcm_16_16k",
        "speechRecognitionCodec": "pcm_16_16k"
      }
    },
    {
      "name": "uploadDynamicVocabulary",
      "id": "2",
      "parameters": {
        "dynamicVocabularies": [
          {
            "id": "my-id",
            "type": "application/x-nuance-wordset",
            "body": {
              "thing-name": [
                {
                  "literal": "Los Angeles",
                  "spoken": [
                    "l a",
                    "los angeles"
                  ]
                },
                {
                  "literal": "San Francisco"
                }
              ],
              "stuff-name": [
                {
                  "literal": "Montreal Canadiens",
                  "spoken": [
                    "montreal canadiens",
                    "habs"
                  ]
                },
                {
                  "literal": "Boston Bruins"
                }
              ]
            }
          }
        ]
      }
    },
    {
      "name": "speechRecognition",
      "id": "3",
      "parameters": {
        "language": "en-us",
        "endPointDetection": true,
        "startOfSpeechHistoryFrames": 15,
        "startOfSpeechVoicedFrames": 7,
        "endOfSpeechHistoryFrames": 50,
        "endOfSpeechVoicedFrames": 5,
        "stopOnEndOfSpeech": true,
        "wordStream": true
      }
    },
    {
      "name": "speechSynthesis",
      "id": "4",
      "parameters": {
        "type": "text",
        "data": "_(2:result.bestResult)_",
        "voice": "Carol"
      }
    }
  ]
}
```
