ARG BASE_IMAGE=webapi-platform:latest
FROM ${BASE_IMAGE}

COPY ./target/*.jar /usr/local/Nuance/webapi/plugins/

USER root:root

COPY *.rpm /tmp/

RUN tdnf install -y /tmp/*nmsp-http-servlet*.rpm && \
    rm -f /tmp/*nmsp-http-servlet*.rpm && \
    tdnf clean all

USER njetty:njetty

EXPOSE 8911
