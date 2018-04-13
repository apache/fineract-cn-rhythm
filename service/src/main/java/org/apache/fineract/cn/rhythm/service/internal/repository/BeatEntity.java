/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.rhythm.service.internal.repository;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.fineract.cn.mariadb.util.LocalDateTimeConverter;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity
@Table(name = "khepri_beats")
public class BeatEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "beat_identifier", nullable = false)
  private String beatIdentifier;

  @Column(name = "tenant_identifier", nullable = false)
  private String tenantIdentifier;

  @Column(name = "application_identifier", nullable = false)
  private String applicationIdentifier;

  @Column(name = "alignment_hour", nullable = false)
  private Integer alignmentHour;

  @Column(name = "next_beat")
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime nextBeat;

  public BeatEntity() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getBeatIdentifier() {
    return beatIdentifier;
  }

  public void setBeatIdentifier(String beatIdentifier) {
    this.beatIdentifier = beatIdentifier;
  }

  public String getTenantIdentifier() {
    return tenantIdentifier;
  }

  public void setTenantIdentifier(String tenantIdentifier) {
    this.tenantIdentifier = tenantIdentifier;
  }

  public String getApplicationIdentifier() {
    return applicationIdentifier;
  }

  public void setApplicationIdentifier(String applicationIdentifier) {
    this.applicationIdentifier = applicationIdentifier;
  }

  public Integer getAlignmentHour() {
    return alignmentHour;
  }

  public void setAlignmentHour(Integer alignmentHour) {
    this.alignmentHour = alignmentHour;
  }

  public LocalDateTime getNextBeat() {
    return nextBeat;
  }

  public void setNextBeat(LocalDateTime nextBeat) {
    this.nextBeat = nextBeat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BeatEntity that = (BeatEntity) o;
    return Objects.equals(beatIdentifier, that.beatIdentifier) &&
            Objects.equals(tenantIdentifier, that.tenantIdentifier) &&
            Objects.equals(applicationIdentifier, that.applicationIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(beatIdentifier, tenantIdentifier, applicationIdentifier);
  }

  @Override
  public String toString() {
    return "BeatEntity{" +
            "id=" + id +
            ", beatIdentifier='" + beatIdentifier + '\'' +
            ", tenantIdentifier='" + tenantIdentifier + '\'' +
            ", applicationIdentifier='" + applicationIdentifier + '\'' +
            ", alignmentHour=" + alignmentHour +
            ", nextBeat=" + nextBeat +
            '}';
  }
}
