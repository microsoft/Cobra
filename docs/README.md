# WebAPI Documentation

This directory contains the WebAPI documentation sources built with [Slate](https://github.com/slatedocs/slate).

## Contents

- **WebAPI API guide** — command and API reference documentation.
- **WebAPI installation guide** — deployment guide using Helm charts.
- **WebAPI configuration guide** — server, tenant, and plugin configuration reference.

## Building the documents

Building plugin docs:

```bash
./build-plugin-docs.sh
```

Building WebAPI docs:

```bash
./build-webapi-docs.sh
```

Serving locally:

```bash
./run-doc.sh <document>
```

The documents can be read at `http://localhost:4567`. Use `-p <port>` for a different port.

## Slate reference

For more information on Slate Markdown syntax, see the [Slate wiki](https://github.com/slatedocs/slate/wiki/Markdown-Syntax).
