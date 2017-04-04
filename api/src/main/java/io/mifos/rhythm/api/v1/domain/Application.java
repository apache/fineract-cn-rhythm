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
package io.mifos.rhythm.api.v1.domain;

import io.mifos.core.lang.validation.constraints.ValidApplicationName;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Application {
  @ValidApplicationName
  private String applicationName;

  public Application() {
    super();
  }

  public Application(final String applicationName) {
    this.applicationName = applicationName;
  }

  public String getApplicationName() {
    return this.applicationName;
  }

  public void setApplicationName(final String applicationName) {
    this.applicationName = applicationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Application application = (Application) o;

    return applicationName != null ? applicationName.equals(application.applicationName) : application.applicationName == null;

  }

  @Override
  public int hashCode() {
    return applicationName != null ? applicationName.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Application{" +
            "applicationName='" + applicationName + '\'' +
            '}';
  }
}
