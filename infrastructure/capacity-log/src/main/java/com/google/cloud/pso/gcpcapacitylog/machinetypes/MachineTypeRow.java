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

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class MachineTypeRow {

  @SerializedName("shared_cpu")
  Boolean isSharedCpu;

  @SerializedName("cpus")
  Integer guestCpus;

  @SerializedName("kind")
  String kind;

  @SerializedName("description")
  String description;

  @SerializedName("self_link")
  String selfLink;

  @SerializedName("memory_mb")
  Integer memoryMb;

  @SerializedName("max_persistent_disks")
  Integer maximumPersistentDisks;

  @SerializedName("max_persistent_disk_size_gb")
  Long maximumPersistentDisksSizeGb;

  @SerializedName("zone")
  String zone;

  @SerializedName("creation_timestamp")
  String creationTimestamp;

  @SerializedName("name")
  String name;

  @SerializedName("project_id")
  String projectID;


  public MachineTypeRow(Boolean isSharedCpu, String kind, String description, String selfLink,
      Integer memoryMb, Integer maximumPersistentDisks, Long maximumPersistentDisksSizeGb,
      String zone, String creationTimestamp, String name, String projectID, Integer guestCpus) {

    this.isSharedCpu = isSharedCpu;
    this.guestCpus = guestCpus;
    this.kind = kind;
    this.description = description;
    this.selfLink = selfLink;
    this.memoryMb = memoryMb;
    this.maximumPersistentDisks = maximumPersistentDisks;
    this.maximumPersistentDisksSizeGb = maximumPersistentDisksSizeGb;
    this.zone = zone;
    this.creationTimestamp = creationTimestamp;
    this.name = name;
    this.projectID = projectID;
  }

  // For tests
  protected MachineTypeRow() {}

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MachineTypeRow that = (MachineTypeRow) o;
    return Objects.equals(isSharedCpu, that.isSharedCpu) &&
        Objects.equals(guestCpus, that.guestCpus) &&
        Objects.equals(kind, that.kind) &&
        Objects.equals(description, that.description) &&
        Objects.equals(selfLink, that.selfLink) &&
        Objects.equals(memoryMb, that.memoryMb) &&
        Objects.equals(maximumPersistentDisks, that.maximumPersistentDisks) &&
        Objects.equals(maximumPersistentDisksSizeGb, that.maximumPersistentDisksSizeGb) &&
        Objects.equals(zone, that.zone) &&
        Objects.equals(creationTimestamp, that.creationTimestamp) &&
        Objects.equals(name, that.name) &&
        Objects.equals(projectID, that.projectID);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(isSharedCpu, guestCpus, kind, description, selfLink, memoryMb, maximumPersistentDisks,
            maximumPersistentDisksSizeGb, zone, creationTimestamp, name, projectID);
  }
  
  public static Schema getBQSchema() {
     Field f1 = Field.of("shared_cpu", LegacySQLTypeName.BOOLEAN);
	 Field f2 = Field.of("cpus", LegacySQLTypeName.INTEGER);
	 Field f3 = Field.of("kind", LegacySQLTypeName.STRING);
	 Field f4 = Field.of("description", LegacySQLTypeName.STRING);
	 Field f5 = Field.of("self_link", LegacySQLTypeName.STRING);
	 Field f6 = Field.of("memory_mb", LegacySQLTypeName.INTEGER);
	 Field f7 = Field.of("max_persistent_disks", LegacySQLTypeName.INTEGER);
	 Field f8 = Field.of("max_persistent_disk_size_gb", LegacySQLTypeName.INTEGER);
	 Field f9 = Field.of("zone", LegacySQLTypeName.STRING);
	 Field f10 = Field.of("creation_timestamp", LegacySQLTypeName.TIMESTAMP);
	 Field f11 = Field.of("name", LegacySQLTypeName.STRING);
	 Field f12 =  Field.of("project_id", LegacySQLTypeName.STRING);
	 return Schema.of(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12);
	
	
  }
}
