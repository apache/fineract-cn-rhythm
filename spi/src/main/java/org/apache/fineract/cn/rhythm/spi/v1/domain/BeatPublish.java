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
package org.apache.fineract.cn.rhythm.spi.v1.domain;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;
import org.apache.fineract.cn.lang.validation.constraints.ValidLocalDateTimeString;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BeatPublish {
  @ValidIdentifier
  private String identifier;

  @NotNull
  @ValidLocalDateTimeString
  private String forTime;

  public BeatPublish() {
  }

  public BeatPublish(String identifier, String forTime) {
    this.identifier = identifier;
    this.forTime = forTime;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getForTime() {
    return forTime;
  }

  public void setForTime(String forTime) {
    this.forTime = forTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BeatPublish beatPublish = (BeatPublish) o;
    return Objects.equals(identifier, beatPublish.identifier) &&
            Objects.equals(forTime, beatPublish.forTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, forTime);
  }

  @Override
  public String toString() {
    return "BeatPublish{" +
            "identifier='" + identifier + '\'' +
            ", forTime='" + forTime + '\'' +
            '}';
  }
}
