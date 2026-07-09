## Audio data

Krypton is optimized to process real time audio streams. Audio is expected to be delivered at a steady rate, in reasonably sized chunks (20ms to 1s, multiples of 20ms recommended) in order for results to be delivered in a timely fashion. When operating outside of the above recommendations, latency may be observed and/or partial results may not be returned at all.

The audio stream is a raw, headerless mono stream of audio samples in one of the formats described in [Audio formats](#audio-formats).

### Binary message

> Generic response to binary Audio message

```json
{
  "method": "Response",
  "responseTo": "Audio",
  "version": "1.0",   
  "sessionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "requestId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "status": "complete|failed",
  "errors": []
}
```

The binary Audio message is a simple WebSocket message containing the audio samples to deliver and no other additional information or parameters.

### Audio formats

Krypton supports the following audio formats and associated mime types:

| Audio format                                     | Mime type                                                    |
| ------------------------------------------------ | ------------------------------------------------------------ |
| Linear PCM, 16 bit, signed little-endian, 8 kHz  | audio/L16;rate=8000  <br />audio/x-raw;format=S16LE;rate=8000 |
| Linear PCM, 16 bit, signed little-endian, 16 kHz | audio/L16;rate=16000  <br />audio/x-raw;format=S16LE;rate=16000 |
| µ-law, 8-bit, 8 kHz                              | audio/basic;rate=8000                                        |
| A-law, 8 bit, 8 kHz                              | audio/x-alaw-basic;rate=8000                                 |
| Raw Opus, 8 kHz                                  | audio/opus;rate=8000 |
| Raw Opus, 16 kHz                                 | audio/opus;rate=16000 |

When providing audio following [Recognize](#recognize-and-interpret), the audio stream is a raw, headerless mono stream of audio samples in one of the above formats.

### Opus 

Krypton supports the raw Opus audio format (<a href="https://tools.ietf.org/html/rfc6716" target="_blank">RFC 6716</a>). The recommended encoder settings for Opus for speech recognition are:

- Sampling rate: 16 kHz
- Complexity: 3. Complexity has limited impact on WER (word error rate), so it can be set as needed
- Bitrate: 28kbps recommended (20kbps minimum)
- Bitrate type: VBR (variable bitrate) or CBR (constant bitrate)
- Packet length: 20ms
- Encoder mode: SILK only mode

Encoding and encapsulation are the client's responsibility. Krypton simply decodes the Opus data it receives.

Please note that Opus is a lossy codec, so you should not expect recognition results to be identical to those obtained with PCM audio.