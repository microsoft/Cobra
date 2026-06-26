# API overview

Nuance Conversational AI provides automatic speech recognition (ASR) and text-to-speech (TTS), 
as well as natural language understanding (NLU), services for developers who want to integrate 
these technologies into a web or mobile app, for example as key components of a virtual agent. 
You can also upload specialized vocabularies to enrich your agent's conversations with greater 
context at runtime.

This overview describes the services offered by the API, with brief examples of its usage. The 
complete reference guide, available from the customer portal, documents all the API methods and
parameters. 

## Communication protocol

Conversational AI uses a secure WebSocket connection to enable full-duplex client-server 
communication. It uses JSON to transmit and receive objects (commands and responses). 

To reach the Conversational AI server you need to know the server endpoint, which your Nuance
team provides. The URI format is:
`wss://<hostname>[:port]/<path>`

Where hostname and path identify your application host and endpoint respectively. The default port
is 443, as for HTTPS. As an example:
`wss://coffeebarn.com:443/orderApp`

### REST(ful) API
Nuance Conversational AI also provides a RESTful API for checking the status of monitored 
background services. For more information, see Service monitoring.

## Session-based operation

To run API commands, you must establish a session after opening the WebSocket. Starting a session
requires an application key and a supported authentication method. These are defined during the
account setup phase as described in Get started.

## Sequence of events: TTS and ASR

![Content_Resources_Images_arch_sequence_diagram_speech_input](Content_Resources_Images_arch_sequence_diagram_speech_input.png)

1. Connect to WebSocket over HTTPS.
2. Start a Conversational AI session.
3. Optionally, upload dynamic vocabularies.
4. Loop to capture requests for speech synthesis or speech recognition.

    1. For speech synthesis, the API returns audio data in the format defined in the default settings, or as selected by the user.
    2. For speech recognition, the backend returns a startOfSpeech event in response to the speech
    recognition command. This may be followed by a series of partial (intermediate) results. When end of speech is detected there is an endOfSpeech event, with possibly additional partial results. When speech recognition stops, the backend sends a final result. The speech recognition result has an isFinal field to indicate whether the result is final or intermediate. An optional speech recognition command parameter activates selected vocabulary sets.

5. End the session. This ends the session and closes the WebSocket.