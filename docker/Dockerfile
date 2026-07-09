FROM mcr.microsoft.com/azurelinux/base/core:3.0

RUN tdnf -y install shadow-utils ca-certificates gawk tar && \
    mkdir -p /etc/systemd/system && \
    tdnf -y distro-sync && \
    tdnf update glibc && \
    curl -O https://cdn.azul.com/zulu/bin/zulu-repo-1.0.0-1.noarch.rpm && \
    curl -o /gpgkey.txt https://www.azul.com/wp-content/uploads/2021/05/0xB1998361219BD9C9.txt && \
    rpm --import /gpgkey.txt && \
    tdnf -y install ./zulu-repo-1.0.0-1.noarch.rpm && \
    echo "gpgkey=file:///gpgkey.txt" >> /etc/yum.repos.d/zulu-openjdk.repo && \
    rm -f ./zulu-repo-1.0.0-1.noarch.rpm && \
    tdnf -y update zulu-repo && \
    tdnf -y install wget zulu-11 jq && \
    mkdir -p /usr/java && \
    ln -s /usr/lib/jvm/zulu-11 /usr/java/latest && \
    tdnf clean all

COPY *.rpm /tmp/

RUN tdnf -y install /tmp/nina-webapi-jetty-10.0.26-202509031706.el7.noarch.rpm && \
    tdnf install -y /tmp/nuance-webapi-platform*.rpm && \
    rm -f /tmp/nuance-webapi-platform*.rpm && \
    sed -e '/--module=console-capture/ s/^#*/#/' -i /usr/local/Nuance/webapi/nina-webapi-base-1/start.d/console-capture.ini && \
    sed -e '/jetty\.httpConfig\.requestHeaderSize/ s/.*/jetty.httpConfig.requestHeaderSize=16384/' -i /usr/local/Nuance/webapi/nina-webapi-base-1/start.d/server.ini && \
    sed -e '/jetty\.httpConfig\.responseHeaderSize/ s/.*/jetty.httpConfig.responseHeaderSize=16384/' -i /usr/local/Nuance/webapi/nina-webapi-base-1/start.d/server.ini && \
    echo "--module=http" >> /usr/local/Nuance/webapi/nina-webapi-base-1/start.d/http.ini && \
    echo "jetty.http.port=8080" >> /usr/local/Nuance/webapi/nina-webapi-base-1/start.d/http.ini && \
    cp /usr/local/Nuance/webapi/config/services.json.tmpl /usr/local/Nuance/webapi/config/services.json && \
    cp /usr/local/Nuance/webapi/config/webapi.json.tmpl /usr/local/Nuance/webapi/config/webapi.json && \
    rm -f /etc/yum.repos.d/zulu.repo && \
    echo "root:docker" | chpasswd && \
    mkdir -p /usr/agents && \
    wget -P /usr/agents `curl -s https://api.github.com/repos/open-telemetry/opentelemetry-java-instrumentation/releases/latest | jq -r '.assets[0].browser_download_url'` && \
    rpm -qa --last && \
    tdnf clean all

COPY log4j2.xml /usr/local/Nuance/webapi/config/log4j2.xml
COPY applicationinsights-agent-3.7.2.jar /usr/local/Nuance/webapi/applicationinsights-agent-3.7.2.jar

ENV JAVA /usr/java/latest/bin/java
ENV JETTY_ARGS -javaagent:/usr/local/Nuance/webapi/applicationinsights-agent-3.7.2.jar -Djetty.home=/usr/local/Nuance/webapi -Djetty.base=/usr/local/Nuance/webapi/nina-webapi-base-1 -Djava.io.tmpdir=/var/lib/jetty -server -Xms1000m -Xmx1700m -Dlog4j.configurationFile=/usr/local/Nuance/webapi/nina-webapi-base-1/config/log4j2.xml -Djdk.tls.client.protocols=TLSv1.2 -Djdk.tls.server.protocols=TLSv1.2 -jar /usr/local/Nuance/webapi/start.jar jetty.state=/var/lib/jetty/jetty1.state --module=state

WORKDIR /usr/local/Nuance/webapi/nina-webapi-base-1

#Class Data Sharing: http://blog.gilliard.lol/2019/12/04/Clojure-Containers.html
#RUN chown -R njetty:njetty /usr/local/Nuance/webapi && \
#    ${JAVA} -Xshare:dump && \
#    nohup bash -c "${JAVA} -Xshare:off -XX:DumpLoadedClassList=appcds.classlist ${JETTY_ARGS} &" && sleep 20 && pkill java || true && \
#    nohup bash -c "${JAVA} -Xshare:dump -XX:SharedClassListFile=appcds.classlist -XX:SharedArchiveFile=appcds.cache ${JETTY_ARGS} &" && sleep 20 && pkill java || true && \
#    rm -f appcds.classlist || true && \
#    chown -R njetty:njetty /usr/local/Nuance/webapi /usr/agents && \
#    dnf clean all

RUN chown -R njetty:njetty /usr/local/Nuance/webapi /usr/agents

EXPOSE 8443 8080

USER njetty:njetty

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

#CMD ${JAVA} ${TRACING_ARGS} -Xshare:on -XX:SharedArchiveFile=appcds.cache ${JETTY_ARGS}
CMD ${JAVA} ${TRACING_ARGS} ${JETTY_ARGS}

ARG PRODUCT_FAMILY="WebAPI"
ARG PRODUCT_NAME="WebAPI"
ARG PRODUCT_VERSION="SeeTheTag"
ARG BUILD_URL=""
ARG BUILD_SYSTEM="GitHub Actions"
ARG BUILD_ID="N/A"
ARG VCS_TYPE="git"
ARG VCS_URL=""
ARG VCS_ID="N/A"
ARG VCS_BRANCH="N/A"

LABEL \
    org.opencontainers.image.title="${PRODUCT_NAME}"            \
    org.opencontainers.image.version="${PRODUCT_VERSION}"        \
    org.opencontainers.image.source="${VCS_URL}"                 \
    org.opencontainers.image.revision="${VCS_ID}"                \
    org.opencontainers.image.ref.name="${VCS_BRANCH}"            \
    org.opencontainers.image.vendor="WebAPI"                     \
    build.url="${BUILD_URL}"                                      \
    build.system="${BUILD_SYSTEM}"                                \
    build.id="${BUILD_ID}"
