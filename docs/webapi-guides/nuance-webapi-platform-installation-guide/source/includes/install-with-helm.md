# Installing Web API on Kubernetes

The following instructions explain how to deploy Web API on Kubernetes using Helm.  Kubernetes automates the management
of containerized workloads. Helm manages the deployment of applications on Kubernetes. Docker provides tools to
manipulate the container images. 

The procedure, and software and platform requirements are mostly similar to the ones used to deploy Krypton on Kubernetes.


## Installation and Runtime Environment

### Installation Host

The following software must be installed on the Linux host on which the installation procedure is executed. Normally,
this host is not part of the Kubernetes cluster running the Web API software.

- Docker 13.1 or a version compatible with your Kubernetes version. See [https://www.docker.com](https://www.docker.com/).

- Helm 2 or 3. If Helm 2 is used, the Tiller server component must be installed in the Kubernetes cluster.
Refer to the [Helm Quick Start Guide](https://docs.helm.sh/using_helm/#quickstart-guide) for instructions. A compatible
version of kubectl, with the configuration pointing to the Kubernetes cluster on which to install Web API is also needed
in the installation host.


### Web API Environment on Kubernetes

The following describes the environment needed to deploy Web API in a Kubernetes cluster.

- Kubernetes version 1.12 or later must be installed on the cluster.

- Traffic routing: The Web API service type can be configured to use Ingress or LoadBalancer. NodePort, although not
not typically used in production, is also supported. 

- Configuration: Web API needs 2 types of configuration:

    - The server configuration applies basic application parameters to the installation. Helm uses the charts to 
    generate the configuration, based on values provided at installation time, and mounts it in the container.
    
    - The tenant configuration contains most of the parameters for a given customer. Web API fetches this configuration
    from an HTTP server. The base URL to retrieve the tenant configuration is part of the server config and is
    configured at installation time.
    
        - For simple deployments that only need one tenant config file, it is possible to start a simple web server
        as a sidecar in the same pod where Web API runs. The base URL to fetch the configuration will then be
        `http://localhost/tenants`. See `tenantWebServer` Helm values below for more details.

- Alerting: Web API generates alarms for error conditions impacting its operations. Installers can configure Web API to 
send SNMP traps or send a POST request to a configurable HTTP endpoint. Installers can also customize the Velocity
template for the body of the POST requests. This allows integration with application monitoring solutions like Zenoss.

Optionally, you may use the following components to monitor the applications and the Kubernetes cluster:

- Prometheus, a metrics collection and alerting solution. The preferred way to deploy Prometheus on Kubernetes
on Kubernetes is to use the [Prometheus Operator](https://github.com/coreos/prometheus-operator). The Web API chart
includes the Custom Resources Definition (CRD) needed so that the prometheus operation can monitor the Web API service.

- Grafana, a piece of data visualization software, can be used to view the metric collected in dashboards. The 
prometheus-operator provides an option to install Grafana with a few preconfigured dashboards to monitor the Kubernetes
cluster. Web API also provides dashboards to monitor the application and Java Virtual Machine (JVM) metrics. They are 
included in the installation package,

In addition, Web API can be integrated with log aggregation solutions to collect, store and analyze its logs. Installers
can configure Web API to write its output to stdout.

For example, you can use Fluentbit and Fluentd with the [Logging Operator](
https://github.com/banzaicloud/logging-operator), which provides a convenient way to deploy and configure
them in a Kubernetes Environment. Integration with other solutions like Sumo Logic, or the ELK (Elastic-LogStash-Kibana)
stack is also possible.


## Installation Procedure

Perform the following procedure on the installation server. The procedure is the same for the base Web API software or
for an application build on top of it (called Web API plugin, like the NCS PDX plugin).  

1. Extract the files from the `nuance-webapi-<app>-*.*.*-*.tgz` archive, where `<app>` equals `platform` for the base WebApi package,
or an app (plugin) name (e.g. `ncs-pdx-interface-plugin`) for other applications based on WebApi. `*.*.*-*` is the current
version (including the build timestamp.)

2. Load the Web API image provided under `images/` using the Docker load command, tag it, and push the image to the
container registry used by the container runtime in a Kubernetes environment:

    ```docker load -i images/*.*.*-*.tgz```

    ```docker tag nuance-webapi-platform[-<plugin>]:*.*.*-* <container-registry>/nuance-webapi-platform[-<plugin>]:*.*.*-*```
    
    ```docker push <container-registry>/nuance-webapi-platform[-<plugin>]:*.*.*-*```

     Note: `<plugin>` is the plugin identifier, and may not match exactly the `<app>` in package name.

3. Locate the Helm chart under `helm-charts` and store it on the installation server, or deploy it in a Helm chart repository.
   The file is named `webapi-0.*.*-chart.tgz`, and the  name of the chart is `webapi`.

4. Configure the Helm chart values for this installation. The section Configure Helm charts below describes the parameters
that can be set. Some packages also provide sample values under 'values-samples/'.

5. Launch the installation with Helm:

    ``` helm install <release-name> webapi-0.*.*-chart.tgz -f <values-file>.yaml```

6. The documentation is found under the `docs/` directory. Each document packaged in a .tgz archive contains a static
site that can be deployed on a web server. The index.html file can also be directly opened in a browser.

7. Grafana dashboards are provided under the `grafana/` directory. They can be imported in Grafana (refer to Grafana
documentation for the procedure to import a dashboard.) The following dashboards are provided:

        - Web API Commands dashboard: Presents commands and errors metrics.
        
        - Web API Command Engines: Presents engines requests and errors metrics.
        
        - Web API HTTP Endpoints: Presents generic statistics (count, status, duration) for HTTP (REST-like) endpoints.

## Configure Helm charts

To configure your Kubernetes environment, locate the *values.yaml* file extracted from the Helm charts provided
by Nuance. You can use it as a base to create the values files for your environment. A description of the
parameters in the values file is provided below. For more information on the server configuration parameters,
please refer to the Web API server configuration guide.

As in all YAML files, the indentation is important: keep the same spacing as indicated below, making sure to use actual
spaces (space bar) and not tabs.

### Web API values file

> Commonly used entries in Helm values for Web API.
>

```yaml
image:
  repository: registry/nuance-webapi-platform
  tag: 2.14.0-202005141319
  pullPolicy: IfNotPresent
  containerPort: 8443  
  terminationGracePeriodSeconds: 900

replicaCount: 2
maxReplicaCount: 20

hpaTargetAverageCpuUtilization: -1

configuration:
  webapi_json:
    sessionCleanUpSleepTimeMilliseconds: 60000
    enableEdfLogPoster: true
    edfLogPosterPoolSize: 20

  log4j2_xml:
    logger_cobra: warn
    logger_nmsp_client: warn
    json_log: false

resources:
  requests:
    memory: "2000Mi"
    cpu: "1000m"
  limits:
    memory: "2500Mi"
    cpu: "1500m"

prometheus:
  enableMonitoring: false
  scrapeInterval: 15s
  namespace: infra
  serviceMonitorLabels:
    release: infra-stack
```

Here are some commonly used entries in the values file for installing Web API with Helm.
 
| Entry            | Description                                                  |
| ---------------- | ------------------------------------------------------------ |
| image |  |
| &#160;&#160;&#160;&#160;repository | Repository containing the Docker images for Web API |
| &#160;&#160;&#160;&#160;tag        | The tag for the Docker image  |
| &#160;&#160;&#160;&#160;pullPolicy     | Image pull policy. See https://kubernetes.io/docs/concepts/containers/images/#updating-images  |
| &#160;&#160;&#160;&#160;containerPort  | Internal endpoint port for Web API pods.  |
| &#160;&#160;&#160;&#160;terminationGracePeriodSeconds | Time in seconds Web API remains alive after gracefully terminating the pod  |
| replicaCount        | Initial number of replicas  |
| maxReplicaCount     | Max number of replicas when auto-scaling is used  |
| hpaTargetAverageCpuUtilization   | Average CPU utilisation to trigger horizontal pod autoscaling. Recommended is 90. Set to -1 to disable  |
| tenantWebServer | |
| &#160;&#160;&#160;&#160;enabled | Whether the tenant web server is enabled or not. If it is enabled, a sidecar container running nginx will be started to serve the tenant configuration. |
| &#160;&#160;&#160;&#160;image |  |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;repository | Repository for the nginx Docker image. | 
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;tag |  Tag for the nginx Docker image. | 
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;pullPolicy |  Pull policy for the nginx Docker image. | 
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;containerPort |  Port on which the web server listens. | 
| &#160;&#160;&#160;&#160;tenantFileName |  Tenant file name (e.g. companyName_appName.json) | 
| &#160;&#160;&#160;&#160;tenantFileContent | Tenant file content. | 
| configuration |  |
| &#160;&#160;&#160;&#160;webapi_json |  |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;(parameter) | Any of the parameters in the webapi.json server configuration file can be configured here. Refer to server configuration documentation for parameters, descriptions, and allowed values. |
| &#160;&#160;&#160;&#160;log4j2_xml |  |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;logger_cobra | Log level for cobra code (error, warn, info, debug, trace) |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;logger_nmsp_client | Log level for the nmsp client code (error, warn, info, debug, trace)  |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;json_log | Whether to format logs as json (true, false)  |
| ingress |  |
| &#160;&#160;&#160;&#160;enabled |  Whether ingress is used or not |
| &#160;&#160;&#160;&#160;tls |  |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;secretName | Name of secret containing certificate and key for TLS. Must be of type `kubernetes.io/tls`.
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;hosts | List of hosts that use this secret.
| &#160;&#160;&#160;&#160;hostPathMappings |  List of (host, paths) |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;host | Host for the mapping
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;paths | List of URL context paths for the mapping
| resources |  |
| &#160;&#160;&#160;&#160;requests |  |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;memory | Memory request. This is amount of memory that will be allocated to the pod. |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;cpu | CPU request. This is amount of CPU that will be allocated to the pod. |
| &#160;&#160;&#160;&#160;limits |  |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;memory | Memory limit. The kernel will terminate the process with an out of memory error if this limit is exceeded. |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;cpu | CPU limit. CPU usage limit for the Web API container. |
| prometheus |  |
| &#160;&#160;&#160;&#160;enableMonitoring |  Whether Prometheus monitoring is enabled or not |
| &#160;&#160;&#160;&#160;scrapeInterval | Prometheus scrape interval |
| &#160;&#160;&#160;&#160;metricsUserBase64 | Username that Prometheus uses to retrieve the metrics.  Must match the value configured in webapi.json. (Note: On Unix the command "echo -n <username> &#124; base64" can be used to do the encoding.)
| &#160;&#160;&#160;&#160;metricsPasswordBase64 | password that Prometheus uses to retrieve the metrics.  Must match the value configured in webapi.json.(Note: On Unix the command "echo -n <username> &#124; base64" can be used to do the encoding.)
| &#160;&#160;&#160;&#160;namespace |  Namespace in which the prometheus operator is installed. |
| &#160;&#160;&#160;&#160;serviceMonitorLabels | |
| &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;release | Name of the Helm release for the Prometheus operator. |
| alarmTemplate | Velocity template for HTTP alarms. The HTTP alarms parameters must also be configured in the webapi_json parameter (httpAlarmTemplateFile, httpAlarmEndpointUrl, and authentication parameters. Refer to the configuration guide for details.) |


