# WebAPI : Krypton WebSocket Plugin

> <a name="docs"></a>Documentation in the WebAPI product suite
>
> - <i>WebAPI Guide</i>: Write client apps using the WebAPI
>
> - ***WebAPI : Krypton WebSocket Plugin***: Write client apps using the Krypton WebSocket Plugin
>
> <a name="docs"></a>Documentation in the Krypton product suite
>
> - <i>Krypton WebSocket API</i>: Write client apps using the Krypton WebSocket API

The WebAPI Krypton WebSocket Plugin allows client applications to request transcription services through a secure and persistent HTTP connection. 

Krypton is Nuance’s real-time large vocabulary automatic speech recognition (ASR) engine. It converts an audio stream of human speech into text by recognizing the speech and transcribing it into text. Krypton can work independently or as part of a virtual assistant or other speech system.

Krypton works with a data pack in a specific language and locale, and can optionally use recognition aides such as domain language models and wordsets to improve recognition and transcription in specific environments or businesses.

<aside class="more_info">The Krypton WebSocket Plugin exposes an API very close to that of the Krypton Websocket API version 1.0. Some commands, such as StatelessRecognize, Ping, GetInto, and 'GET metrics' are not supported. But it has the added functionality of being able to perform interpretation on recognition results.</aside>

<aside class="more_info">In this documentation, occurrences of the term Krypton refer to the WebAPI Krypton WebSocket Plugin.</aside>

## Prerequisites

Before starting to develop Krypton Plugin applications, you need:

- An instance of WebAPI deployed and licensed, with the Krypton WebSocket Plugin.
- Optionally, domain language models, wordsets, speaker profiles, and a semantic model generated using the Mix portal.