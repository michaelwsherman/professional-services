# Professional Services
## Infrastructure
A repository of Infrastructure solutions.

 * [dns-sync](#dns-sync)
 * [labelmaker](#labelmaker)
 * [cloudconnect](#cloudconnect)
 * [capacity-log](#capacity-log)
  

### [dns-sync](dns-sync/)
  Sync a Cloud DNS zone with GCE resources. Instances and load balancers are added to the cloud DNS zone as they start from compute_engine_activity log events sent from a pub/sub push subscription. Can sync multiple projects to a single Cloud DNS zone.

### [labelmaker](labelmaker/)
  _labelmaker.py_ is a tool that reads key:value pairs from a json file and labels the running instance and all attached drives accordingly. It is designed to run on boot in a startup-script or userdata. Labels show up in billing exports to BigQuery, and allows organizations to run complex cost analysis over their cloud spend.

### [cloudconnect](cloudconnect/)
  CloudConnect is a package that automates the setup of dual VPN tunnels between AWS and GCP. While this connection is **NOT** meant for high throughput, low latency connections (1Gbps+), it can certainly support basic management and configuration traffic. Currently it supports the creation of both _static-routes_ and _bgp_ connections.

### [capacity-log](capacity-log/)
_capacity-log_ is an application that provides large GCP organizations with a granular way to monitor global GCE footprint. The system is made to scale to thousands of projects and only requires initial setup. 
The system uses [audit logging](https://cloud.google.com/logging/docs/audit/) to monitor creation an deletion of GCP VMs. 
The end-result is a BigQuery view that can be used for global visualization of GCE utilization.