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
package io.mifos.rhythm.api.v1.events;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BeatEvent {
  private String applicationName;
  private String beatIdentifier;

  public BeatEvent() {
  }

  public BeatEvent(String applicationName, String beatIdentifier) {
    this.applicationName = applicationName;
    this.beatIdentifier = beatIdentifier;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getBeatIdentifier() {
    return beatIdentifier;
  }

  public void setBeatIdentifier(String beatIdentifier) {
    this.beatIdentifier = beatIdentifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BeatEvent beatEvent = (BeatEvent) o;
    return Objects.equals(applicationName, beatEvent.applicationName) &&
            Objects.equals(beatIdentifier, beatEvent.beatIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationName, beatIdentifier);
  }

  @Override
  public String toString() {
    return "BeatEvent{" +
            "applicationName='" + applicationName + '\'' +
            ", beatIdentifier='" + beatIdentifier + '\'' +
            '}';
  }
}
