## Metrics

> Metrics in a response to CreateSession

```json
{
  "sessionId": "1d6ffe60-38e3-11e7-b5e1-7dac7fbd2bae",
  "status": "complete",
  "metrics": {
    "cpu": {
      "sys": 0,
      "user": 0
    },
    "elapsed": 132,
    "timestamps": [
      "20170514202256069",
      "20170514202256201"
    ]
  },
  "requestId": "1",
  "version": "1.0",
  "responseTo": "CreateSession",
  "method": "Response"
}
```

> Metrics in a final response to Load

```json
{   
  "objects": [
    { 
      "id": "ws1"
    }
  ],
  "status": "complete",
  "metrics": {
    "cpu": {
      "sys": 0,
      "user": 100
    },
    "elapsed": 11,
    "timestamps": [
      "20170516113007678",
      "20170516113007689"
    ]
  },
  "sessionId": "039d1380-3a2b-11e7-8734-e9b8a0749f22",
  "requestId": "2",
  "version": "1.0",
  "responseTo": "Load",
  "method": "Response"
}
```

> Metrics in initial response to Recognize

```json
{
  "status": "in-progress",
  "metrics": {
    "weights": {},
    "wordsets": [
      "ws1"
    ],
    "language": "eng-USA",
    "cpu": {
      "sys": 0,
      "user": 0
    },
    "elapsed": 42,
    "timestamps": [
      "20170516113007692",
      "20170516113007734"
    ]
  },
  "sessionId": "039d1380-3a2b-11e7-8734-e9b8a0749f22",
  "requestId": "3",
  "version": "1.0",
  "responseTo": "Recognize",
  "method": "Response"
}
```

> Metrics in StartOfSpeech response

```json
{
  "metrics": {
    "cpu": {
      "sys": 400,
      "user": 600
    },
    "elapsed": 940,
    "timestamps": [
      "20170516113007692",
      "20170516113008632"
    ]
  },
  "requestId": "3",
  "data": {
    "time": 80
  },
  "sessionId": "039d1380-3a2b-11e7-8734-e9b8a0749f22",
  "version": "1.0",
  "event": "StartOfSpeech",
  "method": "Event"
}
```

> Two partial results from a series, with metrics

```json
{
  "metrics": {
    "cpu": {
      "sys": 2100,
      "user": 4400
    },
    "elapsed": 1760,
    "timestamps": [
      "20170516113007692",
      "20170516113009452"
    ]
  },
  "requestId": "3",
  "data": {
    "nBest": [
      {
        "words": [
          {
            "silenceAfterAord": 0,
            "end": 400,
            "start": 220,
            "text": "I"
          }
        ],
        "formattedText": "I",
        "rejected": false
      }
    ],
    "absEnd": 810,
    "absStart": 80,
    "utteranceComplete": false,
    "completionCause": "SUCCESS",
    "status": "IN-PROGRESS"
  },
  "sessionId": "039d1380-3a2b-11e7-8734-e9b8a0749f22",
  "version": "1.0",
  "event": "PartialResult",
  "method": "Event"
}
{
  "metrics": {
    "cpu": {
      "sys": 2200,
      "user": 4600
    },
    "elapsed": 1782,
    "timestamps": [
      "20170516113007692",
      "20170516113009474"
    ]
  },
  "requestId": "3",
  "data": {
    "nBest": [
      {
        "words": [
          {
            "silenceAfterWord": 80,
            "end": 420,
            "start": 200,
            "text": "I'm"
          }
        ],
        "formattedText": "I'm",
        "minimallyFormattedText": "I'm",
        "rejected": false
      }
    ],
    "absEnd": 840,
    "absStart": 80,
    "utteranceComplete": false,
    "completionCause": "SUCCESS",
    "status": "IN-PROGRESS"
  },
  "sessionId": "039d1380-3a2b-11e7-8734-e9b8a0749f22",
  "version": "1.0",
  "event": "PartialResult",
  "method": "Event"
}
```

> Metrics in FinalResult Event for Recognize

```json
{
  "metrics": {
    "media": "audio/l16;rate=8000",
    "firstAudioToResult": 2351,
    "firstAudioToStartOfSpeech": 752,
    "cpu": {
      "sys": 2800,
      "user": 7200
    },
    "elapsed": 2538,
    "timestamps": [
      "20170516113007692",
      "20170516113010230"
    ]
  },
  "requestId": "3",
  "data": {
    "nBest": [...omitted...
      "formattedText": "I'm good. Yourself?",
      "minimallyFormattedText": "I'm good. Yourself?",
      "rejected": false,
      "confidence": 916
      }
    ],
    "utteranceInfo": {
      "clippingDuration": 0,
      "droppedNonSpeechPackets": 0,
      "droppedSpeechPackets": 0,
      "duration": 2060,
      "finalEnergy": -55.0298,
      "initialEnergy": -55.0298,
      "initialSilence": 280,
      "level": 18945,
      "meanEnergy": 145.322,
      "snrEstimate": 19
    },
    "absEnd": 2140,
    "absStart": 80,
    "utteranceComplete": true,
    "completionCause": "SUCCESS",
    "status": "COMPLETE"
  },
  "sessionId": "039d1380-3a2b-11e7-8734-e9b8a0749f22",
  "version": "1.0",
  "event": "FinalResult",
  "method": "Event"
}
```

> Metrics in final response to Recognize

```json
{ 
  "status": "complete",
  "metrics": {
    "cpu": { 
      "sys": 2800,
      "user": 7200
    },
    "elapsed": 2540,
    "timestamps": [
      "20170516113007692",
      "20170516113010232"
    ]
  },
  "sessionId": "039d1380-3a2b-11e7-8734-e9b8a0749f22",
  "requestId": "3",
  "version": "1.0",
  "responseTo": "Recognize",
  "method": "Response"
}
```

Krypton commands can optionally include metrics in their results.

### Base metrics

When enabled, every response includes a metrics property, which includes these basic metrics. 

| Property   | Description                                                  |
| ---------- | ------------------------------------------------------------ |
| cpu        | CPU utilisation observed across the command, in milliseconds. |
| &#160;&#160;&#160;sys        | System CPU consumed during the elapsed time.                 |
| &#160;&#160;&#160;user       | User CPU consumed during the elapsed time.                   |
|            |                                                              |
| elapsed    | An integer indicating the elapsed time between the timestamps in milliseconds. |
| timestamps | Two-element list of YYYYMMDDhhmmsss formatted timestamps. The first element indicates the time the request was received, while the second element is the sending time of the message conveying the response or event. |

Note that timestamps and CPU consumption in general have a platform-specific quantization. CPU consumption may exceed the elapsed time when multiple cores contribute.

Commands having multiple responses (in-progress followed by complete) include metrics on all responses. The ending timestamp, elapsed time, and CPU usage are all updated to reflect conditions at the time of the response.

The Load and Recognize commands implement additional fields in the metrics value.

Events associated with an in-progress command include the current metrics values for the command. The ending timestamp, elapsed time, and CPU usage are all updated to reflect conditions at the time the event is emitted. Any fields added by the command implementation up to that point will also be included. This effect is seen in the Recognize StartOfSpeech, PartialResult, and FinalResult events.

