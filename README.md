# WebAPI Platform

A Java/Jetty server that exposes speech recognition, text-to-speech, NLU, and
dialog functionality over HTTP and WebSocket. It mediates between client
applications and a set of pluggable speech engines.

## Requirements

- JDK 11
- Maven 3.6+

## Building

```
$ mvn clean install
```

To build the RPM distribution:

```
$ mvn clean install -P rpm
```

## Running

WebAPI is packaged as a WAR and deployed in a Jetty servlet container. The
WebSocket endpoint is served at:

```
wss://<host>:<port>/webapi-platform/websocket/
```

## Repository layout

- `webapi-webapp-war/` — main WebAPI server WAR
- `webapi-plugin-interface-jar/` — plugin SPI
- `webapi-utils-jar/`, `webapi-utils-testing-jar/` — shared utilities and test helpers
- `webapi-grpc-clients/` — generated gRPC clients for ASR/NLU/TTS/Dialog
- `webapi-concentus-jar/` — vendored Opus codec (Concentus)
- `ncs-nmsp-http-servlet-war/`, `nvc-nmsp-http-servlet-war/` — NMSP HTTP servlets
- `plugins/` — engine-specific plugins (Krypton WS, NCS NMSP, TTS REST, etc.)
- `docker/` — container build files

## Pre-commit hooks

This repo uses [pre-commit](https://pre-commit.com/) for lightweight checks.
After cloning:

```
$ pre-commit install
```

## Continuous integration

Builds run on GitHub Actions — see `.github/workflows/build.yml`.

## License

See [LICENSE](LICENSE).
