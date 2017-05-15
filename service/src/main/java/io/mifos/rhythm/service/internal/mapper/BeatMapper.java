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

import io.mifos.rhythm.api.v1.domain.Beat;
import io.mifos.rhythm.service.internal.repository.BeatEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Myrle Krantz
 */
public interface BeatMapper {
  static Beat map(final BeatEntity entity) {
    final Beat ret = new Beat();
    ret.setIdentifier(entity.getBeatIdentifier());
    ret.setAlignmentHour(entity.getAlignmentHour());
    return ret;
  }

  static List<Beat> map(final List<BeatEntity> entities) {
    final List<Beat> ret = new ArrayList<>(entities.size());
    ret.addAll(entities.stream().map(BeatMapper::map).collect(Collectors.toList()));
    return ret;
  }

  static BeatEntity map(final String tenantIdentifier, final String applicationName, final Beat instance) {
    final BeatEntity ret = new BeatEntity();
    ret.setBeatIdentifier(instance.getIdentifier());
    ret.setTenantIdentifier(tenantIdentifier);
    ret.setApplicationName(applicationName);
    ret.setAlignmentHour(instance.getAlignmentHour());
    return ret;
  }
}
