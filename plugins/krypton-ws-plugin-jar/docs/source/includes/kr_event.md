## Event

Event commands are issued by Krypton and carry asynchronous events and notifications to Recognize commands. Supported events are: StartOfSpeech, PartialResult, FinalResult, and EndOfSession.

### StartOfSpeech

> StartOfSpeech example

```json
{
  "method": "Event",
  "event": "StartOfSpeech",
  "version": "1.0",
  "sessionId": "e925e7f8-e98e-47ee-b6f4-a502ab5ec945",
  "requestId": "eaf65033-4caf-4edf-ad08-0ce2751ef953"  
}
```

The StartOfSpeech event indicates the beginning of audio as detected by Krypton. This event is triggered only when the recognition parameter disableUtteranceDetection is false (the default).  It includes these properties.

| Property  | Value                                                        |
| --------- | ------------------------------------------------------------ |
| method    | Event                                                        |
| event     | The event name: StartOfSpeech                                |
| version   | The protocol version of the current session: 1.0.            |
| sessionId | The identifier of the current session.                       |
| requestId | An automatically generated request identifier.               |

### Results and autoEnd

The results of Recognize are carried in events named PartialResult and FinalResult. The behavior of these events depends on the recognition parameter autoEnd, which determines whether recognition turns terminate automatically.  

#### autoEnd: true

When autoEnd is true, the Krypton plugin ends the recognition turn after end of speech is detected. In this scenario, it expects the incoming audio stream to continue uninterrupted (unless stopped by the client) until it detects a sufficient amount of silence following some speech to signify the end of speech. 

While processing, the Krypton plugin may return one or more PartialResult events, then a FinalResult event carrying the transcription of the entire sentence. 

#### autoEnd: false

When autoEnd is false, the Krypton Plugin never automatically ends the recognition. it returns a sequence of PartialResult events while transcribing the sentence. A FinalResult is returned only when the client signals EndOfInput. If the transcription turn is stopped in any other way, a FinalResult is not returned.

This table illustrates the behavior of autoEnd: false

<table class="nospacing">
  <tbody>
    <tr ><td colspan="2"><i>While transcribing each sentence</i></td></tr>
    <tr>
      <td>One or more PartialResult events with<ul><li>utterance_complete: false</li><li>status: IN-PROGRESS</li><li>completion_cause: SUCCESS</li><li>Partial nBest results</li></ul></td>
    </tr>
    <tr >
      <td colspan="2"><i>After client application sends EndOfInput to stop transcription</i></td>
    </tr>
    <tr>
      <td>One last PartialResult events with<ul><li>utterance_complete: true</li><li>status: IN-PROGRESS</li><li>completion_cause: SUCCESS</li><li>Partial nBest results</li></ul></td>
    </tr>
    <tr>
      <td>Response to EndOfInput with<ul><li>status: COMPLETE</li></ul></td>
    <tr>
    <tr>
        <td><b>Event FinalResult with<ul><li>status: COMPLETE</li><li>completion_cause: END-OF-STREAM</li><li>(No nBest results)</li></ul></b></td>
    </tr>
    <tr>
        <td>Response to Recognize with<ul><li>status: COMPLETE</li></ul></td>
    </tr>
  </tbody>
</table>



### PartialResult

> These partial results report on the first part of a sentence, as indicated by `utterance_complete: false`.

```json
{  
  "method":"Event",
  "event":"PartialResult",
  "version":"1.0",
  "sessionId":"d8e8672d-6528-4042-9fbc-12dd40be639d",
  "data":{  
    "status":"IN-PROGRESS",
    "completion_cause":"SUCCESS",
    "utterance_complete":false,
    "absStart":5410,
    "absEnd":7200,
    "nBest":[{  
        "rejected":false,
        "formattedText":"We are expecting 10 cm",
        "minimallyFormattedText":"We are expecting ten centimeters",
        "words":[{  
            "text":"We",
            "start":80,
            "end":180,
            "silenceAfterWord":0
          },
          {  
            "text":"are",
            "start":180,
            "end":300,
            "silenceAfterWord":0
          },
          {  
            "text":"expecting",
            "start":300,
            "end":760,
            "silenceAfterWord":0
          },
          {  
            "text":"10",
            "start":760,
            "end":1080,
            "silenceAfterWord":0
          },
          {  
            "text":"cm",
            "start":1080,
            "end":1700,
            "silenceAfterWord":0
          }]
      }]
  },
  "requestId":"6f98496d-1df7-4e90-902f-f1771b28eb75"
}
```

The PartialResult event includes the data property, containing the transcription result of the sentence so far. 

| Property                                                     | Value                                                        |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| method                                                       | Event                                                        |
| event                                                        | The event name: PartialResult                                |
| version                                                      | The protocol version of the current session: 1.0.     |
| sessionId                                                    | The identifier of the current session.                       |
| requestId                                                    | An automatically generated request identifier.               |
| data                                                         | The transcription results.                                   |
| &#160;&#160;&#160;status                                     | Always IN_PROGRESS for partial results.                      |
| &#160;&#160;&#160;completion_cause                           | Either SUCCESS (result successfully produced) or ERROR (an error prevented the result from being computed). |
| &#160;&#160;&#160;utterance_complete                         | Whether a complete utterance was detected.                   |
| &#160;&#160;&#160;absStart                                   | Start time in milliseconds.                                  |
| &#160;&#160;&#160;absEnd                                     | End time in milliseconds.                                    |
| &#160;&#160;&#160;nBest                                      | Array of transcription results. Results include all hypothesis of the sentence (to the maximum set with the nBestListLength recognition parameter, default 10) with confidence levels for each variation. |
| &#160;&#160;&#160;&#160;&#160;&#160;rejected                 | Whether the array of words was rejected (true) or accepted (false). When marked as rejected, any text returned should not be considered reliable. |
| &#160;&#160;&#160;&#160;&#160;&#160;formattedText            | Formatted text of the result, including initial capitals for recognized names and places, numbers expressed as digits, currency shown with currency symbols, and other common abbreviations. |
| &#160;&#160;&#160;&#160;&#160;&#160;minimallyFormattedText   | Text of the result with less formatting: words are spelled out instead of expressed as abbreviations and symbols but basic capitalization and punctuation are included. For example: <br />"formattedText": "December 9, 2005",<br />"minimallyFormattedText": "December nine two thousand and five",<br /><br />"formattedText": "$500",<br />"minimallyFormattedText": "Five hundred dollars",<br /><br />In many cases, minimallyFormattedText is the same as formattedText. For example:<br />"formattedText": "I'm okay James, how about yourself?",<br />"minimallyFormattedText": "I'm okay James, how about yourself?" |
| &#160;&#160;&#160;&#160;&#160;&#160;words                    | Array of recognized words in result.                         |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;text   | The recognized word.                                         |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;start  | Start time of the word in milliseconds.                      |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;end    | End time of the word in milliseconds.                        |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;silence_after_word | The amount of silence detected between words. Currently this value is always 0. |

### FinalResult

> FinalResult example with autoEnd: false

```json
{
  "method": "Event",
  "event": "FinalResult",
  "version": "1.0",
  "sessionId": "d8e8672d-6528-4042-9fbc-12dd40be639d",
  "requestId": "6f98496d-1df7-4e90-902f-f1771b28eb75",
  "data": {
    "status": "COMPLETE",
    "completion_cause": "END-OF-STREAM"
  }
}
```

> FinalResult example with autoEnd: true

```json
{
  "method":"Event",
  "event":"FinalResult",
  "version":"1.0",
  "sessionId":"e925e7f8-e98e-47ee-b6f4-a502ab5ec945",
  "requestId":"eaf65033-4caf-4edf-ad08-0ce2751ef953",
  "data":{  
    "status":"COMPLETE",
    "completion_cause":"SUCCESS",
    "utterance_complete":true,
    "absStart":0,
    "absEnd":5410,
    "utteranceInfo":{"...metrics as in PartialResult..."},
    "nBest":[{  
        "confidence":573,
        "averageConfidence":930,
        "rejected":false,
        "formattedText":"There is more snow coming to the Montreal area in the next few days.",
        "minimallyFormattedText":"There is more snow coming to the Montreal area in the next few days.",
        "words":["...individual words as in PartialResult..."]
      }]
  }
}
```

> FinalResult example with autoEnd: true, and interpretation data

```json
{  
  "method":"Event",
  "event":"FinalResult",
  "version":"1.0",
  "sessionId":"e925e7f8-e98e-47ee-b6f4-a502ab5ec945",
  "requestId":"eaf65033-4caf-4edf-ad08-0ce2751ef953",
  "data":...
  "interpretationStatus":"complete",
  "interpretationData": {
    "interpretations": [
      {
        "confidence": 1,
        "origin": "GRAMMAR",
        "mentions": [
          {
            "concept": "TIME_OF_DAY",
            "end": 12,
            "begin": 5,
            "literal": "morning"
          },
          {
            "concept": "DATE",
            "end": 36,
            "begin": 31,
            "literal": "today"
          }
        ],
        "literal": "Good morning how are you doing today",
        "intention": "GREETINGS"
      }
    ]
  }
}
```

> FinalResult example with autoEnd: true, and interpretation error

```json
{  
  "method":"Event",
  "event":"FinalResult",
  "version":"1.0",
  "sessionId":"e925e7f8-e98e-47ee-b6f4-a502ab5ec945",
  "requestId":"eaf65033-4caf-4edf-ad08-0ce2751ef953",
  "data":...
  "interpretationStatus":"failed",
  "interpretationErrors": [
    {
      "message": "An error occurred.",
      "code": 0000
    }
  ]
}
```

The FinalResult event carries the final result in the data property. When `autoEnd: false`, this event contains only the status and completion cause, not the transcription results.

When `autoEnd: true` FinalResult includes the complete transcription results for the utterance.

FinalResult will contain interpretation data if one was performed. See [Interpret](#interpret).

| Property           | Value                                                        |
| ------------------ | ------------------------------------------------------------ |
| method             | Event                                                        |
| event              | The event name: FinalResult                                  |
| version            | The protocol version of the current session: 1.0. |
| sessionId          | The identifier of the current session.                       |
| requestId          | An automatically generated request identifier.               |
| data               | The transcription results.                                   |
| &#160;&#160;&#160;status             | Always COMPLETE, as this event is emitted only after all speech is transcribed. |
| &#160;&#160;&#160;completion_cause   | The reason these results are considered complete. <ul><li>SUCCESS: The result was correctly produced.</li><li>ERROR: An error prevented the result from being computed.</li><li>NO-INPUT-TIMEOUT: The noInputTimeout interval has elapsed: no result is delivered.</li><li>RECOGNITION-TIMEOUT: The recognitionTimeout interval elapsed: the result may or may not be delivered.</li><li>END-OF-STREAM: An EndOfInput command terminated the Recognize turn: the result may or may not be delivered.</li><li>NO-MATCH: The result has been produced, but is marked as rejected. This completion cause is returned for extremely noisy and/or unintelligible audio, or when the audio is not compatible with the declared audio format.</li></ul> |
| &#160;&#160;&#160;*other properties* | When autoEnd is true, transcription results for the sentence are included. |
| interpretationStatus | Either 'complete' or 'failed'. |
| interpretationData   | A JSON object containing the interpretation results. Present only when interpretationStatus is 'complete'. |
| interpretationErrors | A JSON array of JSON objects containing interpretation errors. Present only if interpretationStatus is 'failed'. |

### EndOfSession

> EndOfSession example

```json
{
  "method": "Event",
  "event": "EndOfSession",
  "version": "1.0",
  "sessionId": "d8e8672d-6528-4042-9fbc-12dd40be639d",
  "reason": "idleTimeout" | "shutdown"   
}
```

Krypton issues the EndOfSession event when the current session times out or terminates for another reason. An optional reason string may be included. 

| Property  | Value                                                        |
| --------- | ------------------------------------------------------------ |
| method    | Event                                                        |
| event     | The event name: EndOfSession                                 |
| version   | The protocol version of the current session: 1.0. |
| sessionId | The identifier of the current session.                       |
| reason    | Reason for end of session, either idleTimeout or shutdown.   |




