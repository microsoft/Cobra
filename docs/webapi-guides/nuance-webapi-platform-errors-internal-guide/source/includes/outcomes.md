# Basic outcomes

| Name     | Http equivalent code       | Description                           |
|----------|------------|---------------------------------------|
| success | 200 | <p>The command was successful.</p> |
| noinput | 400 | <p>The command did not receive input.</p>_i.e. SpeechRecognition was started and no speech was detected_ |
| nomatch | 204 | <p>The command did not produce significant results.</p>_i.e. SpeechRecognition or Interpretation couldn't produce significant results_ |
| cancelled | 230 | <p>The command was cancelled.</p> |