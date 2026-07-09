<h3 id="heading_speech_synthesis_parameters">Query Begin</h3>
 
 | Name     | Type       |
 |----------|------------|
 |transaction_id|	Integer selected by the client to differentiate responses to the current transaction from "stale" messages from a previous transaction. Must be different from the previous transaction. Must be less than 256. | 
 |command| Operation for the server to execute; i.e. NMDP_ASR_CMD | 
 |context_tag|	Context tag describing the version of Studio.nlu model to use. | 
 |language|	6-Char Code, i.e. "eng-USA" (optional)| 
 |subscriber_id| Optional. String that correlates multiple users or devices that belong to a common yet unique entity (e.g. household). Cerence recommends a UUID.|
 |phone_model|	Optional. String that correlates multiple users or devices that belong to a common yet unique entity (e.g. household). Cerence recommends a UUID.|
 |tts_voice| TTS only Use to select a specific voice. For a list of available voices, see "Languages & Voices" on the Cerence Developers portal.|
 |dictation_type| ASR only String name of the core language model to use for recognition|

<h3 id="dictionary_query">Dictionary Query Parameter</h3>
 
| Name     | Type       |
|----------|------------|
|transaction_id| Same ID as used for query_begin message. | 
|parameter_type| "dictionary"| 
|parameter_name| Name of the parameter according to the command specification.| 
|dictionary| JSON dictionary whose contents are specific to the command.|
| <p class="param-1">nlu_nbest_interpretations</p> | <p>When this parameter is set to 1, all the probable NLU results are returned. When this parameter is not provided (or set to 0), then only the top result is returned.</p> |


> Dictionary query Request-Example with text:

```json
{
    "transaction_id": 100,
    "message": "query_parameter",
    "parameter_type": "dictionary",
    "parameter_name": "REQUEST_INFO",
    "dictionary": {
        "application_data": {
            "text_input": "The text to interpret."
        },
        "nlu_nbest_interpretations": 1
    }
}
```

<h3 id="audio_query">Audio Query Parameter</h3>
 
| Name     | Type       |
|----------|------------|
|transaction_id| Same ID as used for query_begin message. | 
|parameter_type| "audio"| 
|parameter_name| AUDIO_INFO| 
|audio_id| Integer selected by the client to be unique within this connection.|

> Audio query Request-Example with text:

```json
 {
    "message": "query_parameter",
    "transaction_id": 2,
    "parameter_type": "audio",
    "parameter_name": "AUDIO_INFO",
    "audio_id": 2
 }
```

<h3 id="query_end">Query End</h3>
 
| Name     | Type       |
|----------|------------|
|audio_id| Integer specified in the audio parameter message.| 