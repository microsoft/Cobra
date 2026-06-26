# Nuance WebAPI Server - NCS PDX Plugin Configuration

The Nuance Web API is a platform for adding sophisticated speech recognition and speech synthesis (text-to-speech) 
capabilities to mobile and web applications.

This guide covers all the server side configurations for the NCS PDX plugin.

## Installing the plugin

### Installing Binaries

The plugin comes packaged as a jar file. Example file name: `ncs-nmsp-raw-interface-plugin-1.1.0-202012081452.jar`.
Copy this jar file to the  `plugins` folder inside your WebAPI installation. Default location: `/usr/local/Nuance/webapi/plugins`.

### Activation the plugin

The `nmsp` protocol plugin is a server plugin, meaning it will automatically start-up at the server start-up.

The plugin will start its own socket listener on the default port `8911`.

##Configuring the plugin

### Server Side Configuration
Because this plugin starts at the server startup, the plugin configuration will only take effect if it is set in the `webapi.json`.

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| ncsNmspRawServer | `Object` | <p>JSON object to configure the NSMP protocol raw plugin. |
| <p class="param-1">sslEnabled</p> | `Boolean` | **optional** <p>Enable or disable ssl encryption for this server.</p>_Default value: true_<br> |
| <p class="param-1">serverPort</p> | `Integer` | **optional** <p>The port the NMSP server should listen on.</p>_Default value: 8911_ |
| <p class="param-1">maxRecognitionResults</p> | `Integer` | **optional** <p>Limit the number of returned recognition results.</p>_Default value: 3_ |

> Config Example: 
```json
"ncsNmspRawServer": {
    "sslEnabled": true,
    "serverPort": 8911,
    "maxRecognitionResults": 3
}
```

### Tenant related Configuration

| Name     | Type       | Description                           |
|----------|------------|---------------------------------------|
| dictationTypeToTopicMap | `Object` | <p>JSON object containing `dictation_type` and `dictation_language` mapping to `topic` and `speechDomain`. |
| <p class="param-1">dictation_type</p> | `Object` | The name of the dictation type mapping to a `dictation_language` JSON object. |
| <p class="param-2">dictation_language</p> | `Object` | The name of the dictation language mapping to a JSON object. |
| <p class="param-3">topic</p> | `String` | The name of the webapi topic to use. |
| <p class="param-3">speechDomain</p> | `String` | The name of the webapi speechDomain to use. |

> Config Example: 
```json
"dictationTypeToTopicMap": {
    "Dictation": {
        "eng-USA": {
            "topic": "GEN",
            "speechDomain": "messaging"
        }
    },
    "dtv": {
        "eng-USA": {
            "topic": "DTV",
            "speechDomain": "websearch"
        }
    }
}
```

| Name                                      | Type       | Description                                                                                             |
|-------------------------------------------|------------|---------------------------------------------------------------------------------------------------------|
| contextTagToTopicMap                      | `Object` | <p>JSON object containing `context_tag` and `dictation_language` mapping to `topic` and `speechDomain`. |
| <p class="param-1">context_tag</p>        | `Object` | The name of the context tag mapping to a `dictation_language` JSON object.                              |
| <p class="param-2">dictation_language</p> | `Object` | The name of the dictation language mapping to a JSON object.                                            |
| <p class="param-3">topic</p>              | `String` | The name of the webapi topic to use.                                                                    |
| <p class="param-3">speechDomain</p>       | `String` | The name of the webapi speechDomain to use.                                                             |

> Config Example:
```json
"contextTagToTopicMap": {
    "A100_C1": {
        "eng-USA": {
            "topic": "GEN",
            "speechDomain": "messaging"
        },
        "eng-USA": {
            "topic": "GEN",
            "speechDomain": "messaging"
        }
    },
    "A3017_C1": {
        "eng-USA": {
            "topic": "DTV",
            "speechDomain": "websearch"
        }
    }
}
```