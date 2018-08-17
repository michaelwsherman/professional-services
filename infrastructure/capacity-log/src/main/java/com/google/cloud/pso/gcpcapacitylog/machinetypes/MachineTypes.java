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

package com.google.cloud.pso.gcpcapacitylog.machinetypes;

import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.compute.model.MachineType;
import com.google.cloud.bigquery.JobStatistics;
import com.google.cloud.pso.gcpcapacitylog.services.BQHelper;
import com.google.cloud.pso.gcpcapacitylog.services.EmptyRowCollection;
import com.google.cloud.pso.gcpcapacitylog.services.GCEHelper;
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MachineTypes {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * This method scans a org for MachineTypes and uploads an each machine type to table specificed in the input arguments.
   *
   * @param orgNumber the org number. Example: 143823328417
   * @param dataset the name of the dataset where the machine_types should be written. Example: gce_capacity_log
   * @param tableName the table name where the machine_types should be written. Example: machine_types
   * @see MachineTypeRow is the BigQuery datamodel
   */
  public static void writeMachineTypestoBQ(String projectId, String orgNumber, String dataset,
      String tableName)
      throws IOException, GeneralSecurityException, InterruptedException {

    BQHelper.deleteTable(projectId, dataset, tableName);

    List<Project> projects = GCEHelper.getProjectsForOrg(orgNumber);

    for (int i = 0; i < projects.size(); i++) {
      Set<Object> machineTypeRows = new HashSet<>();
      Project project = projects.get(i);

      for (MachineType machineType : GCEHelper.getMachineTypesForProject(project)) {
        machineTypeRows.add(convertToBQRow(project, machineType));
      }
      // Table table = BQHelper.createTable(projectId, dataset, tableName, MachineTypeRow.getBQSchema());
      JobStatistics statistics = null;

      try {
        statistics = BQHelper.insertIntoTable(projectId, dataset, tableName, MachineTypeRow.getBQSchema(), machineTypeRows);
        logger.atInfo().log(statistics.toString());
      } catch (EmptyRowCollection e) {
        logger.atFinest().log("No input data supplied", e);
      }

      logger.atInfo().log(
          "Processed project (" + (i + 1) + "/" + projects.size() + ") " + projects.get(i)
              .getProjectId());
    }
  }

  protected static MachineTypeRow convertToBQRow(Project project, MachineType machineType) {
    return new MachineTypeRow(
        machineType.getIsSharedCpu(),
        machineType.getKind(),
        machineType.getDescription(),
        machineType.getSelfLink(),
        machineType.getMemoryMb(),
        machineType.getMaximumPersistentDisks(),
        machineType.getMaximumPersistentDisksSizeGb(),
        machineType.getZone(),
        machineType.getCreationTimestamp(),
        machineType.getName(),
        project.getProjectId(),
        machineType.getGuestCpus()
    );
  }


}
