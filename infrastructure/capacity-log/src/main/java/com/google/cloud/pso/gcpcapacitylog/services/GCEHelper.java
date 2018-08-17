/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.pso.gcpcapacitylog.services;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.ListProjectsResponse;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.Compute.Instances;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;
import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.MachineTypeAggregatedList;
import com.google.api.services.compute.model.MachineTypesScopedList;
import com.google.api.services.compute.model.Zone;
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class GCEHelper {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static List<Project> getProjectsForOrg(String orgNumber)
      throws IOException, GeneralSecurityException {

    CloudResourceManager cloudResourceManagerService = CloudResourceManagerService.getInstance();
    CloudResourceManager.Projects.List request = cloudResourceManagerService.projects().list()
        .setFilter("parent.type=organization AND parent.id=" + orgNumber);
    ListProjectsResponse response;
    List returnValue = new ArrayList();
    do {
      response = request.execute();
      if (response.getProjects() == null) {
        continue;
      }
      for (Project project : response.getProjects()) {
        returnValue.add(project);
      }
      request.setPageToken(response.getNextPageToken());
    } while (response.getNextPageToken() != null);

    return returnValue;
  }

  public static List<Instance> getInstancesForProject(Project project)
      throws IOException, GeneralSecurityException {

    List<Instance> returnValue = new ArrayList();
    Compute compute = ComputeService.getInstance();
    List<Zone> zones = compute.zones().list(project.getProjectId()).execute().getItems();

    for (Zone zone : zones) {
      Instances.List request = compute.instances().list(project.getProjectId(), zone.getName());
      InstanceList response;
      do {
        response = request.execute();
        if (response.getItems() != null) {
          returnValue.addAll(response.getItems());
        }
        request.setPageToken(response.getNextPageToken());
      } while (response.getNextPageToken() != null);
    }
    return returnValue;
  }

  public static List<MachineType> getMachineTypesForProject(Project project)
      throws GeneralSecurityException, IOException {

    Compute compute = ComputeService.getInstance();
    List<MachineType> returnvalue = new ArrayList<>();

    MachineTypeAggregatedList results = null;
    try {
      results = compute.machineTypes().aggregatedList(project.getProjectId()).execute();
    } catch (GoogleJsonResponseException e) {
      if (e.getStatusCode() == 403) {
        logger.atInfo().log("GCE API not activated for project: " + project.getProjectId() + ". Ignoring project.");
      } else {
        throw e;
      }
    }

    if (results != null && results.getItems().values() != null) {
      for (MachineTypesScopedList machineType : results.getItems().values()) {
        if (machineType.getMachineTypes() != null) {
          returnvalue.addAll(machineType.getMachineTypes());
        }
      }
    }

    return returnvalue;
  }
}
