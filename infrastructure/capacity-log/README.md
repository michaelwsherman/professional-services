# Capacity Log
## Introduction
The purpose of this application is to provide large GCP organizations with a granular way to monitor GCE global capacity usage within their GCP-org. The system is made to scale to thousands of projects and only requires initial setup. 
The system uses [audit logging](https://cloud.google.com/logging/docs/audit/) to monitor creation an deletion of GCP VMs. 
The end-result is a BigQuery view that can be used for global tracking of GCE utilization.


### Scanning for machine types (machine_types table)
Unfortunately for us the audit log events written into BigQuery only contain the machine type (ex. n1-standard4) but does not include the cores and RAM size of the machine. To remediate this we need to join with another table called **machine_types**. 

The audit logs and the machine_types table allows us to create a view within BigQuery that produces a result that looks approximately like this:

### vm_events (view)

inserted                   | deleted                    | preemptible | instance_id         | project_id       | zone             | cores | memory_mb |
---------------------------|----------------------------|-------------|---------------------|------------------|------------------|-------|-------|
2018-05-06 15:23:32.104UTC | null                       | false       | 3374911842295128859 | myorg-resource3 | us-east1-b       | 1     | 3840  | 
2018-05-14 10:38:18.896UTC | null                       | false       | 1413363994329405941 | myorg-resource1  | us-east1-b       | 1     | 3840  |	 
2018-05-02 14:29:53.525UTC | 2018-05-21 13:05:48.943UTC | false       | 1123220051332461966 | myorg-resource2  | us-central1-a    | 1     | 3840  |	 


### Initial VM inventory (initial_vm_inventory table)
Finally, as the audit log sink only writes creation and deletion events from the moment it was created. We therefore need to do an initial scan of the org for VMs that were created before the sink was created. 

This initial VM inventory will be found in a table called **initial_vm_inventory**. 

## The dependecies
There are three concrete pieces of code within this project. 

- The **vm_events** view which is simply a bigquery view. 
- The **machine_types**, which is a BigQuery table created by a java program wrapped in a docker-container. 
- The **initial_vm_inventory**, which is a BigQuey table created by a java program wrapped in docker container.

The step by step instructions below include instructions on how to build ([maven](https://maven.apache.org/) & [GCB](https://cloud.google.com/container-builder/docs/)) and deploy **machine_types** and **initial_vm_inventory** on top of a [GKE](https://cloud.google.com/kubernetes-engine/)-cluster. 

**initial_vm_inventory** only needs to run once whilst **machine_types** should run on a regular cadence, like every hour. In the instructions **machine_types** is deployed as a cron scheduled pod. 


## Instructions 

### Initial steps and CI
* First thing you need to do is to create a project within your organization. Note down the project id, within this doc we'll refer to our project id as ***my-project-id***.
* Enable billing on the account.
* Create create a build trigger in [Container Builder](https://cloud.google.com/container-builder/docs/running-builds/automate-builds). 
  * Name the trigger anything you want, such as "gcp-capacity-log". 
  * Point the trigger to https://github.com/GoogleCloudPlatform/professional-services/. 
  * Set the file filer to /infrastructure/gcp-capacity-log/. 
  * Set the build configuration to cloudbuild.yaml and set the cloudbuild.taml location to /infrastructure/gcp-capacity-log/capacity/cloudbuild.yaml. 
* When this is done verify that you have an image in your [GCR](https://cloud.google.com/container-registry/),  called **capacity**.

#### Service account & IAM permissioning
Now create a [Custom IAM Role](https://cloud.google.com/iam/docs/creating-custom-roles) called **gcp-capacity-log-role**. 
Give the role the following permissions:
```
compute.instances.get
compute.instances.list
compute.machineTypes.get
compute.machineTypes.list
compute.zones.get
compute.zones.list
resourcemanager.organizations.get
resourcemanager.projects.get
resourcemanager.projects.list
```

Now create a service account, called **gcp-capacity-log-service**. Give that service account the following org-level roles :

```
Viewer
```

Also give the account the following project level roles:
```
gcp_capacity_log_role
BigQuery Data Editor
BigQuery Data Owner
Editor
```
## Installing and deploying
Now lets go through the installation steps. There are a two parameters that you need to have during the installation, these are:

- The id of the project. Here referred to as ```{PROJECT_ID}```. An example project id is gcp-capacity-log.
- The numeric id of the orgnization. Here referred to as ```{ORG_NUMBER}```. An example org number is 143820778417.

### Set project and create dataset
Our first step is to set gcloud to use our poject and create a dataset where we want our sink to write the audit logs. You need to come up with a name for the dataset and write this down, from here on we'll use the name {DATASET_NAME}. Do also choose an appropiate location for the dataset. In this example we use "EU", you might want to use another [location](https://cloud.google.com/bigquery/docs/dataset-locations). 
```
gcloud config set project {PROJECT_ID}
bq mk --location=EU -d {PROJECT_ID}:{DATASET_NAME}
```
### Install audit log sink ###
The next step is to create the audit log sink that will write to you BigQuery dataset, the [command](https://cloud.google.com/sdk/gcloud/reference/beta/logging/sinks/create) below creates this sink.
```
gcloud logging sinks create gcp-capacity-log-sink bigquery.googleapis.com/projects/{PROJECT_ID}/datasets/{DATASET_NAME} --organization={ORG_NUMBER} --log-filter='resource.type="gce_instance" AND protoPayload.serviceName="compute.googleapis.com"' --include-children
```
Do note that this step has a manual step that needs to be completed. Look to your console to see the ouput of the command and follow the instructions. 

### Run containers
You are now ready to execute the containers. 

The initial-vm-inventory inventory only needs to run once. You can execute it with GCE through this command. 

```
gcloud beta compute --project=gcp-capacity-log instances create-with-container initial-vm-inventory --machine-type=n1-standard-16 --service-account=gcp-capacity-log-service@{PROJECT_ID}.iam.gserviceaccount.com --container-image=gcr.io/gcp-capacity-log/initial-vm-inventory:latest --container-env=ORG_NUMBER={ORG_NUMBER},PROJECT_ID={PROJECT_ID},DATASET={DATASET},OPERATION=initial-vm-inventory
```

The machine-types should be run on periodic basis. Below is an example of running it once on GCE through the gcloud command. 

```
gcloud beta compute --project=gcp-capacity-log instances create-with-container machine-types --machine-type=n1-standard-16 --service-account=gcp-capacity-log-service@{PROJECT_ID}.iam.gserviceaccount.com --container-image=gcr.io/gcp-capacity-log/initial-vm-inventory:latest --container-env=ORG_NUMBER={ORG_NUMBER},PROJECT_ID={PROJECT_ID},DATASET={DATASET},OPERATION=machine-types
```

The sample pod definition described [here](scripts/kubernetes) shows how to deploy the containers in a kubernetes cluster.

### SQL View
After running the two containers listed above you should have three tables with data in your dataset:

-  ```gce_capacity_log.cloudaudit_googleapis_com_activity_*```
-  ```gce_capacity_log.machine_types```
-  ```gce_capacity_log.initial_vm_inventory```

If this is the case your can now run and use the view found in ```/infrastructure/capacity-log/vm_events.sql```
The output of this view should be look similar to:

inserted                   | deleted                    | preemptible | instance_id         | project_id       | zone             | cores | memory_mb |
---------------------------|----------------------------|-------------|---------------------|------------------|------------------|-------|-------|
2018-05-06 15:23:32.104UTC | null                       | false       | 3374911842295128859 | myorg-resource3 | us-east1-b       | 1     | 3840  | 
2018-05-14 10:38:18.896UTC | null                       | false       | 1413363994329405941 | myorg-resource1  | us-east1-b       | 1     | 3840  |	 
2018-05-02 14:29:53.525UTC | 2018-05-21 13:05:48.943UTC | false       | 1123220051332461966 | myorg-resource2  | us-central1-a    | 1     | 3840  |	 
