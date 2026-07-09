## Recognize (and Interpret)

> Generic Recognize request

```json
{  
  "method":"Recognize",
  "version":"1.0",
  "sessionId":"xxxx from CreateSession",
  "requestId":"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "builtinWeights":{  
    "builtin_1":"high",
    "builtin_n":"medium"
  },
  "domainLmWeights":{  
    "dlm_1":"highest",
    "dlm_n":"medium"
  },
  "useWordSets":[  
    "wordset_1",
    "wordset_n"
  ],
  "recognitionParameters":{  
    "autoEnd":false,
    "enablePartialResults":true,
    "enableProfanityFilter":true,
    "immutablePartialResults":true,
    "other parameters"
  }
```

> This example assigns a weight to two DLMs loaded with CreateSession or Load, and activates a wordset added with Load. (Wordsets added with CreateSession are always in effect so are not activated in the recognition turn.)

```json
{  
  "method":"Recognize",
  "version":"1.0",
  "sessionId":"xxxx from CreateSession",
  "domainLmWeights":{  
    "dlm_bank":"highest",
    "dlm_lit":"medium"
  },
  "useWordSets":[  
    "ws_branches"
  ],
  "recognitionParameters":{  
    "autoEnd":false,
    "enablePartialResults":true,
    "enableProfanityFilter":true,
    "formattingType":"date"
  }
}
```

Recognize starts a new *recognition turn*: the sequence of operations that accepts speech as audio and transcribes it into text. Only one recognition turn at a time is supported in a session. Any attempt to start a new recognition turn while another one is still active will fail.

If a semantic model was loaded into the session, the recognition result will be interpreted before being returned. See [Interpretation](#interpret).

Audio chunks (see [Audio data](#audio-data) received after a recognition turn starts successfully contribute to that turn, until one of the following occurs:

- The turn is terminated by a Stop command.
- The client signals end-of-utterance using EndOfInput.
- The recognizer detects end-of-speech and the turn is configured to terminate on end-of-speech.

Audio chunks received after any of the above events are discarded.

Use these properties to configure the Recognize command.

| Property                                                   | Type           | Value                                                        |
| ---------------------------------------------------------- | -------------- | ------------------------------------------------------------ |
| method                                                     | String         | Recognize                                                    |
| version                                                    | String         | (Optional) Protocol version: 1.0. Default is set in config.  |
| sessionId                                                  | String         | The sessionId from CreateSession response.                   |
| requestId                                                  | String         | (Optional) Request identifier.                               |
| builtinWeights                                             | Object         | (Optional) One or more entities provided with the data pack (see [Data pack](#data-pack)) and their weight. |
| &#160;&#160;&#160;*builtin_1 to n*                         | String         | The name of a builtin entity and its weight in the recognition turn, one of lowest, low, medium, high, highest, or an integer 0-1000. |
| domainLmWeights                                            | Object         | (Optional) One of more domain LMs and their weight. DLMs declared in Load must be enabled here by giving them a weight. DLMs declared in CreateSession are automatically enabled for all recognition turns, although you may change the weight set in CreateSession. |
| &#160;&#160;&#160;*dlm_1 to n*                             | String         | The ID of a domain LM and its weight (or importance compared to the underlying data pack) in the recognition turn, one of lowest, low, medium, high, highest, or an integer 0-1000. The DLM ID is returned by the CreateSession or Load command that defined the object. The weight overrides the weight set in CreateSession. |
| useWordSets                                                | Array          | (Optional) An array of objects: one or more wordsets declared in Load to use for this recognition turn. Wordsets declared in CreateSession are automatically used in Recognize and do not need to be enabled here. |
| &#160;&#160;&#160;*wordset_1 to n*                         | String         | The wordset IDs to activate for the recognition turn. Multiple wordsets are separated by commas. The wordset ID is returned by the Load command that defined the object. |
| <a name="recognition-parameters"></a>recognitionParameters | Object         | (Optional) Recognition parameters and values. Parameters set here override recognition parameters set in CreateSession for the recognition turn only. All parameters are optional. |
| &#160;&#160;&#160;audioFormat                              | String         | (Optional) Incoming audio mime type. Default is audio/L16;rate=8000. See [Audio formats](#audio-formats). |
| &#160;&#160;&#160;autoEnd                                  | Boolean        | (Optional) Whether the recognition turn ends when Krypton detects the end of an utterance. See utteranceEndSilence below.<ul><li>true (default): Recognition ends when an end of utterance is detected.</li><li>false: Recognition continues until the client sends a Stop or EndOfInput command. This allows multiple utterances to be transcribed in a single recognition command.</li></ul> |
| &#160;&#160;&#160;autoPunctuation                          | Boolean        | (Optional) Whether to enable auto punctuation: <ul><li>true (default): Auto punctuation is enabled if available for the language.</li><li>false: Auto punctuation is disabled.</li></ul> |
| &#160;&#160;&#160;enablePartialResults                     | Boolean        | (Optional) Whether to enable partial results:<ul><li>true (default): Partial results are returned, followed by full results. See also immutablePartialResults below.</li><li>false: Only full results are returned.</li></ul> |
| &#160;&#160;&#160;noInputTimeout                           | String         | (Optional) Interval of silence ([time designation](#time-designations)) allowed while waiting for user input after recognition timers have been started. Default is no timeout. |
| &#160;&#160;&#160;recognitionTimeout                       | String         | (Optional) Maximum duration (time designation) of the recognition turn. Default is no maximum. |
| &#160;&#160;&#160;startRecognitionTimers                   | Boolean        | (Optional) Whether timers are activated when a recognition turn starts: <ul><li>true (default): Timers start when the turn begins.</li><li>false: Timers do not start when the turn begins. They can be started later—typically following a barge-in event—using [StartRecognitionTimers](#startrecognitiontimers). </li></ul>Once started, timers cannot be stopped. |
| &#160;&#160;&#160;utteranceEndSilence                      | String         | (Optional) Minimum amount of silence (time designation) that causes an utterance to end. Default is 500ms, or half a second. Values between 0 and 99999ms. <br />The recognition engine resolution for this parameter is not milliseconds: at run time, the value passed is mapped to the first internal threshold that equals or exceeds the value passed or the highest value available if the requested value exceeds all available thresholds. |
| &#160;&#160;&#160;speechDetectionSensitivity               | Integer 0‑1000 | (Optional) A value that controls the detection of speech vs. noise. Default is 500. Values 0-1000 are accepted: 0 means least sensitive to speech (do not interpret noise as speech) while 1000 means most sensitive (interpret all noise as speech). This parameter sets the balance between rejecting breathing and other noise, while recognizing input speech. |
| &#160;&#160;&#160;disableUtteranceDetection                | Boolean        | (Optional) Whether to deactivate the internal utterance detection mechanism.<ul><li>false (default): Utterances are detected.</li><li>true: Utterances are not detected, meaning the StartOfSpeech event is not generated and these recognition parameters are not allowed: autoEnd, noInputTimeout, recognitionTimeout, and utteranceEndSilence. The client must explicitly terminate the audio stream with EndOfInput.</li></ul> |
| &#160;&#160;&#160;enableProfanityFilter                    | Boolean        | (Optional) Whether to filter profanities and other unacceptable language from transcriptions. Not all data packs support this feature.<ul><li>true: Known profanities for the language, when recognized, are replaced with asterisks (\*\*\*) to reduce the explicit offensiveness of the transcription.</li><li>false (default): Profanities are left as is in the transcription.</li></ul> |
| &#160;&#160;&#160;speechDomain                             | String         | (Optional) A mapping to internal weightsets for language models in the data pack. Providing a specific speechDomain tells Krypton to expect that type of audio input and can improve transcription quality using an internal weightset.<br />Values depend on the data pack, as listed in the [data pack](#data-pack) readme file, for example: ivr, healthcare, messaging, and so on. |
| &#160;&#160;&#160;formattingType                           | String         | (Optional) A keyword that affects how ambiguous numbers are presented in the formattedText field. For example, this is how the utterance “seven eleven” is formatted for these types:<ul><li>all_as_words: seven eleven</li><li>date: 7/11</li><li>time: 7:11</li></ul> Values depend on the data pack, as listed in the [data pack](#data-pack) readme file. See [Formatting](#formatting). |
| &#160;&#160;&#160;immutablePartialResults                  | Boolean        | (Optional) When partial results are enabled (see enablePartialResults above), whether to return stable partial results. This feature may be useful if you are using realtime analytics systems. Not all data packs support this feature.<ul><li>true: Partial results are delivered, after a slight delay, to ensure that the recognized words do not change with the rest of the received speech. Some data packs perform additional processing after the initial transcription. The transcription may change slightly during this second pass, even when immutablePartialResults is true.</li><li>false (default): Partial results are delivered as soon as speech is detected, but with low recognition confidence. These results usually change as more speech is processed and the context is better understood.</li></ul> |
| &#160;&#160;&#160;suppressCallRecording                    | Boolean        | (Optional) Whether call recording is disabled for the recognition turn. <ul><li>true: Information is not sent to the call log aggregator. </li><li>false (default): Call logs, metadata, and audio are collected by the aggregator.  </li>See Config - [callRecording](#callrecording). When call recording is disabled in config or CreateSession, this parameter has no effect. |
| &#160;&#160;&#160;nBestListLength                          | Integer 1-10   | (Optional) The number of n-best hypotheses returned in the nBest recognition result. Default is 10. |

### Interpret

The Krypton Plugin will automatically perform an interpretation on the recognition result if a semantic model was loaded into the current session.

See [CreateSession](#semantic-model-objects).

Interpretation results are returned in the [FinalResult](#finalresult) event.

### Response to Recognize

> Initial response to Recognize

```json
{
  "method": "Response",
  "responseTo": "Recognize",
  "version": "1.0",   
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "in-progress|failed"
}
```

> Final response to Recognize 

```json
{
  "method": "Response",
  "responseTo": "Recognize",
  "version": "1.0",   
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete"
}
```

> See also examples in [Event](#event)

Recognize is an asynchronous command. The initial response indicates only whether the command was understood and started correctly, returning either in-progress or failed.

During a running recognition command, Krypton generates a sequence of events—PartialResult and FinalResult—returned periodically. These events contain the progress of the transcription. See [Event](#event). 

A final response, with status either complete or failed, indicates the outcome of the Recognize command. See [Response](#response). 

### Recognize metrics

> Metrics returned in initial response to Recognize

```json
{
  "method": "Response",
  "responseTo": "Recognize",
  "version": "1.0",
  "requestId": "53af23b1-e67a-4dd9-8daf-50a723b23184",
  "sessionId": "5d78cb63-8880-43fb-a947-8749d076b705",
  "metrics": {
    "timestamps": ["20200610090907343", "20200610090907352"],
    "elapsed": 9,
    "cpu": {
      "user": 200,
      "sys": 200
    },
    "language": "eng-USA",
    "wordsets": [],
    "weights": {},
    "builtins": {
      "BOOLEAN": "high"
    }
  },
  "status": "in-progress"
}
```

> Metrics returned in FinalResult event for Recognize

```json
{  
  "event": "FinalResult",
  "metrics": {
    "firstAudioToResult": 1528,
    "effectiveWordsetWeights": [
      {
        "wordsets": [ "inline_wordset_1" ],
        "weight": 72
      },
      {
        "wordsets": [ "session_wordsetpkg_1" ],
        "weight": 72
      }
    ],
    "media": "audio/l16;rate=8000",
    "firstAudioToStartOfSpeech": 690,
    "elapsed": 1542,
    "timestamps": [
      "20171106115932563",
      "20171106115934105"
    ],
    "effectiveBuiltins": {
      "DIGITS": 0,
      "HEALTHINS": 0,
      "NUMBER0_99": 0,
      "FINSVCS": 0,
      "TELCO": 0,
      "HOTEL": 0,
      "NUMBER": 0,
      "CURRENCY": 0,
      "BOOLEAN": 393,
      "ALPHANUM": 0,
      "TIME": 0,
      "DATE": 0
    },
    "effectiveWeights": {
      "dlm_1": 180,
      "session_dlm_1": 180
    },
    "cpu": {
      "sys": 900,
      "user": 2100
    },
    "wordsetAddedWords": [
      {
        "wordsets": [ "inline_wordset_1" ],
        "words": 72
      },
      {
        "wordsets": [ "session_wordsetpkg_1" ],
        "words": 11
      }
    ],
    "DLMAddedWords": {
      "dlm_1": 18,
      "session_dlm_1": 122
    }
  }
}
```

The initial in-progress response to a Recognize request adds several properties to the base metrics (all values are strings):

| Property           | Value                                                        |
| ------------------ | ------------------------------------------------------------ |
| timestamps, etc.   | See [Base metrics](#base-metrics).                           |
| language           | The language configured for the session (3+3 ISO string).    |
| wordsets           | Array of wordsets specified in the request. Possibly empty.  |
| weights            | Domain LMs and corresponding weights as specified in the request. Possibly empty. |

The final response to Recognize contains only base metrics. When autoEnd is true, the FinalResult event includes the result plus metrics.

Intermediate events during Recognize provide updated metrics: the ending timestamp, elapsed time, and CPU values are the same or increased. PartialResult or FinalResult events with utteranceComplete: true add the following properties to the base metrics:

| Property                  | Value                                                        |
| ------------------------- | ------------------------------------------------------------ |
| timestamps, etc.          | See [Base metrics](#base-metrics).                                                   |
| media                     | The audio format configured for the session.                 |
| firstAudioToStartOfSpeech | The number of milliseconds from the arrival of the first audio message/binary packet after Recognize to transmission of the StartOfSpeech event. |
| firstAudioToResult        | The number of milliseconds from the arrival of the first audio message/binary packet after Recognize to the current time. |
| effectiveWeights          | An object of domain LMs and their effective weight settings. |
| effectiveWordsetWeights   | An array of objects, each specifying a wordset and weight property. The wordsets property is an array containing all wordsets with the same weight. |
| effectiveBuiltins         | An object of builtin names and their effective weight settings, to be used in recognition. |
| DLMAddedWords             | Domain LMs and and the number of Out Of Vocabulary words defined by each. |
| wordsetAddedWords         | Array of objects identifying OOV associated with one or more wordsets. |

