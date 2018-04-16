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
package org.apache.fineract.cn.rhythm.api.v1.domain;

import org.hibernate.validator.constraints.Range;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ClockOffset {
  @Range(min = 0, max = 23)
  private Integer hours;

  @Range(min = 0, max = 59)
  private Integer minutes;

  @Range(min = 0, max = 59)
  private Integer seconds;

  public ClockOffset() {
    this.hours = 0;
    this.minutes = 0;
    this.seconds = 0;
  }

  public ClockOffset(Integer hours, Integer minutes) {
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = 0;
  }

  public ClockOffset(Integer hours, Integer minutes, Integer seconds) {
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
  }

  public Integer getHours() {
    return hours;
  }

  public void setHours(Integer hours) {
    this.hours = hours;
  }

  public Integer getMinutes() {
    return minutes;
  }

  public void setMinutes(Integer minutes) {
    this.minutes = minutes;
  }

  public Integer getSeconds() {
    return seconds;
  }

  public void setSeconds(Integer seconds) {
    this.seconds = seconds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClockOffset that = (ClockOffset) o;
    return Objects.equals(hours, that.hours) &&
        Objects.equals(minutes, that.minutes) &&
        Objects.equals(seconds, that.seconds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hours, minutes, seconds);
  }

  @Override
  public String toString() {
    return "ClockOffset{" +
        "hours=" + hours +
        ", minutes=" + minutes +
        ", seconds=" + seconds +
        '}';
  }
}
