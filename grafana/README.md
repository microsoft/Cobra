# Grafana Dashboards

The dashboard provided here can be to monitor WebAPI operation in Grafana:

- WebAPI Commands dashboard: Presents commands and errors metrics.

- WebAPI Command Engines: Presents engines requests and errors metrics.

- WebAPI HTTP Endpoints: Presents generic statistics (count, status, duration) for HTTP (REST-like) endpoints.

- JVM Kubernetes dashboards: Presents statistics for the JVMs (valid for WebAPI and other containers exposing
the same JVM metrics using the Prometheus Java client.)  (**INTERNAL USE ONLY**: see ewapi-1095. Do not rename
the `jvm-kubernetes.json` dashboard file.)


## Prerequisites

- Access to a Kubernetes cluster

- The Prometheus operator is deployed in the cluster.

- WebAPI is deployed in the Kubernetes cluster with Prometheus metrics enabled.
 
- A Grafana instance is configured to access to use the DB Prometheus pushes data to as a data source.
(Default: if Grafana is enabled in the Prometheus operator it can use the internal Prometheus database as the
datasource)
 
- The Prometheus operator is configured to send data to a datasource that Grafana can use (Default is
internal Prometheus timeseries database.)

- Grafana is exposed: Grafana must be exposes through a NodePort, Ingress, LoadBalancer (or port forwarding can be
used for testing)


## Import Dashboards in Grafana

Use the following procedure to import the dashboards:

1. Go to Dashboard -> Manage in Grafana and click on import button at the top right corner.

2. Click On "Upload JSON file" button & upload an existing Dashboard json file.

3. Select Prometheus datasource configured for the k8s cluster.

4. Click on `Import`. If the panel was already imported, there will be warnings for the panel name
and unique Id, and the button will be labeled `Import (Overwrite)`.


## Updating the Dashboards

Use the following procedure to update a dashboard, after making change in Grafana.

- Click on the Share button among the buttons in the top right corner of the screen.

- Select the `Export` tab

- Enable `Export for sharing externally option`

- Click `Save to file` or `View JSON`, and update the corresponding dashboard json file in this
directory.
