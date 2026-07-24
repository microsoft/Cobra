# webapi-jetty

Repo that contains the build scripts for Webapi Jetty for builds on a Linux host

## Steps for updates
- Download the latest Jetty archive from https://jetty.org/download.html
- Put the .tar.gz file in the **webapi-jetty-dist** folder
- Run the **prep-jetty-dist.sh** script to create a Webapi Jetty bundle zip
    - ./prep-jetty-dist.sh 10.0.26 jetty-home-10.0.26
- Move the created zip file to the **rpm** folder
- Run the **build-rpm.sh** script to create an RPM
    - ./build-rpm.sh
- Copy the created RPM file to webapi's docker folder
    - e.g. webapi/docker/nina-webapi-jetty-10.0.26-202508281501.el7.noarch.rpm
- Update the Dockerfile in webapi/docker to use the new RPM file

## webapi-jetty-dist/
  - Contains the scripts to build a Webapi Jetty bundle zip to be hosted on an http server.
  - Requires a Jetty .tar.gz [distribution download](https://www.eclipse.org/jetty/) as input
  - See **prep-jetty-dist.sh**

## rpm/
  - Uses the hosted Webapi Jetty bundle zip to create an RPM
  - Requires perl

## TLS keystore
  - The Jetty `ssl`/`https` modules expect a keystore at
    `nina-webapi-base-*/etc/keystore`. No keystore is shipped in this
    repository, so no private key material is committed to source control.
  - You must provide your own keystore before running the server (or terminate
    TLS at an ingress / load balancer). See
    `webapi-jetty-dist/nina-webapi-base-*/etc/keystore.README.md` for
    instructions on generating or supplying one, and keeping the `ssl.ini`
    password in sync.
