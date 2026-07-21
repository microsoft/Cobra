# TLS keystore

This directory must contain a Java keystore named `keystore` for the Jetty
`ssl`/`https` modules to start. The keystore is **not** shipped with this
repository — you must provide your own so that no private key material is
committed to source control.

The TLS connector is configured in `../start.d/ssl.ini`:

```
jetty.sslContext.keyStorePath=etc/keystore
jetty.sslContext.trustStorePath=etc/keystore
jetty.sslContext.keyStorePassword=OBF:...
jetty.sslContext.keyStoreType=JKS
```

## Providing a keystore

Place a keystore file at `etc/keystore` (relative to this Jetty base). The
keystore type and password must match the values in `ssl.ini`.

### Option A — bring your own certificate (recommended for production)

Import your server certificate and private key (issued by your CA) into a
keystore:

```
openssl pkcs12 -export \
  -in server.crt -inkey server.key -certfile ca-chain.crt \
  -name jetty -out keystore -passout pass:<your-password>
```

Then set `jetty.sslContext.keyStoreType=PKCS12` in `ssl.ini` (PKCS12 is the
recommended, portable format). Note: TLS is commonly terminated at an ingress /
load balancer, in which case this connector only needs a self-signed
certificate for internal traffic.

### Option B — generate a self-signed certificate (development / internal only)

```
keytool -genkeypair -alias jetty \
  -keyalg RSA -keysize 2048 -validity 825 \
  -dname "CN=localhost, OU=Engineering, O=Example, L=City, ST=State, C=US" \
  -keystore keystore -storetype PKCS12 \
  -storepass <your-password> -keypass <your-password>
```

## Keeping the password in sync

`ssl.ini` stores the keystore password using Jetty's obfuscation (`OBF:`),
which is only protection against casual observation, not real encryption. If
you change the password, regenerate the `OBF:` value:

```
java -cp <jetty-home>/lib/jetty-util-<version>.jar \
  org.eclipse.jetty.util.security.Password <your-password>
```

Copy the printed `OBF:...` string into `jetty.sslContext.keyStorePassword` in
`ssl.ini`.
