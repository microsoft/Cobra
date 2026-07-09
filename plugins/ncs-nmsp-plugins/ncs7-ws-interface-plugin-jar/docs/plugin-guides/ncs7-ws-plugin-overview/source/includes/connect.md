 <h3 id="heading_speech_synthesis_parameters">Connect</h3>
 
 | Name     | Type       |
 |----------|------------|
 | codec | MIME type of the audio audio-codecs | 
 | device_id | String that uniquely identifies the device that the query is being sent from, such as a MAC address. | 
 | user_id| String that uniquely identifies the user who is speaking, if available. | 
 | app_id| The Cerence Mobile App ID (NMAID) registered with Cerence Inc. | 
 | app_key|	The application key received from Cerence Inc. The value is a string of hexadecimal digits of the application key. |
 
> Example request:

```json
{
    "message": "connect",
    "codec": "audio/L16;rate=16000",
    "device_id": "19dff95ac5c247dcaac7a27cbebc6b55",
    "user_id": "19dff95ac5c247dcaac7a27cbebc6b55",
    "app_id": "TEST_NMAID_2017",
    "app_key": "<REDACTED>"
}
```
> Example response:

```json
{
    "message": "connected",
    "session_id": "653a6f210179452fb8074380393a40c3"
}
```