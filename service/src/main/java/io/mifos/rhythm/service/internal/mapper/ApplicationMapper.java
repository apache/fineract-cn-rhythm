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
package io.mifos.rhythm.service.internal.mapper;

import io.mifos.rhythm.api.v1.domain.Application;
import io.mifos.rhythm.service.internal.repository.ApplicationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Myrle Krantz
 */
public interface ApplicationMapper {
  static Application map(final ApplicationEntity entity) {
    final Application ret = new Application();
    ret.setApplicationName(entity.getApplicationName());
    return ret;
  }

  static ApplicationEntity map(final Application instance) {
    final ApplicationEntity ret = new ApplicationEntity();
    ret.setApplicationName(instance.getApplicationName());
    return ret;
  }

  static List<Application> map(final List<ApplicationEntity> entities) {
    final List<Application> ret = new ArrayList<>(entities.size());
    ret.addAll(entities.stream().map(ApplicationMapper::map).collect(Collectors.toList()));
    return ret;
  }
}