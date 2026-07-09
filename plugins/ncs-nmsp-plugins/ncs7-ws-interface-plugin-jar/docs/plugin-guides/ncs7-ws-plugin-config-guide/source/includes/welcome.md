# Nuance WebAPI Server - NCS7 WS Plugin Configuration

The Nuance Web API is a platform for adding sophisticated speech recognition and speech synthesis (text-to-speech) 
capabilities to mobile and web applications.

This guide covers all the server side configurations for the NCS7 WS plugin.

## Installing the plugin

### Installing Binaries

The plugin comes packaged as a jar file. Example file name: `ncs7-ws-interface-plugin-1.1.0-202012081452.jar`.
Copy this jar file to the  `plugins` folder inside your WebAPI installation. Default location: `/usr/local/Nuance/webapi/plugins`.

### Activation the plugin

To activate the plugin, add the jar name (can use regular expression filters) to the `activePluginList`.

If everything is set up correctly, the plugin can be accessible at the following URL: `wss://[SERVER_HOST]:[SERVER_PORT]/webapi-platform/websocket/[TENANT_NAME]/ncs7/[v1|v2]` 

> Plugin enabling Example: 
```json
"activePluginsList":[
    {
        "jarName":"ncs7-ws-interface-plugin-.*.jar",
            "settings":{
            }
    }
],
```

## Configuring the plugin

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


| Name                                | Type       | Description                                                                                   |
|-------------------------------------|------------|-----------------------------------------------------------------------------------------------|
| contextTagToTopicMap                | `Object` | <p>JSON object containing `context_tag` and `language` mapping to `topic` and `speechDomain`. |
| <p class="param-1">context_tag</p>  | `Object` | The name of the context tag mapping to a `language` JSON object.                    |
| <p class="param-2">language</p>     | `Object` | The name of the language mapping to a JSON object.                                  |
| <p class="param-3">topic</p>        | `String` | The name of the webapi topic to use.                                                          |
| <p class="param-3">speechDomain</p> | `String` | The name of the webapi speechDomain to use.                                                   |

> Config Example:
```json
"contextTagToTopicMap": {
    "A100_C1": {
        "eng-USA": {
            "topic": "GEN",
            "speechDomain": "messaging"
        },
        "spa-SPA": {
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