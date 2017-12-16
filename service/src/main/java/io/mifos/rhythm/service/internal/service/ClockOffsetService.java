/*
 * Copyright 2017 Kuelap, Inc.
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
package io.mifos.rhythm.service.internal.service;

import io.mifos.rhythm.api.v1.domain.ClockOffset;
import io.mifos.rhythm.service.internal.mapper.ClockOffsetMapper;
import io.mifos.rhythm.service.internal.repository.ClockOffsetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Myrle Krantz
 */
@Service
public class ClockOffsetService {
  final private ClockOffsetRepository clockOffsetRepository;

  @Autowired
  public ClockOffsetService(final ClockOffsetRepository clockOffsetRepository) {
    this.clockOffsetRepository = clockOffsetRepository;
  }

  public ClockOffset findByTenant(final String tenantIdentifier) {
    return clockOffsetRepository.findByTenantIdentifier(tenantIdentifier)
        .map(ClockOffsetMapper::map)
        .orElseGet(ClockOffset::new);
  }
}