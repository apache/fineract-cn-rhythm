/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.rhythm.service.internal.repository;

import javax.persistence.*;
import java.util.Objects;

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

  @Column(name = "identifier")
  private String identifier;

  @Column(name = "tenant_identifier")
  private String tenantIdentifier;

  @Column(name = "application_name")
  private String applicationName;

  @Column(name = "alignment_hour")
  private Integer alignmentHour;

  public BeatEntity() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getTenantIdentifier() {
    return tenantIdentifier;
  }

  public void setTenantIdentifier(String tenantIdentifier) {
    this.tenantIdentifier = tenantIdentifier;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public Integer getAlignmentHour() {
    return alignmentHour;
  }

  public void setAlignmentHour(Integer alignmentHour) {
    this.alignmentHour = alignmentHour;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof BeatEntity)) return false;
    BeatEntity that = (BeatEntity) o;
    return Objects.equals(getIdentifier(), that.getIdentifier()) &&
            Objects.equals(getApplicationName(), that.getApplicationName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIdentifier(), getApplicationName());
  }
}
