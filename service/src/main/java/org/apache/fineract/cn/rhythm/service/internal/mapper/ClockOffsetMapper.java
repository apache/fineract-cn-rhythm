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
package org.apache.fineract.cn.rhythm.service.internal.mapper;

import java.util.Optional;
import org.apache.fineract.cn.rhythm.api.v1.domain.ClockOffset;
import org.apache.fineract.cn.rhythm.service.internal.repository.ClockOffsetEntity;

/**
 * @author Myrle Krantz
 */
public interface ClockOffsetMapper {
  static ClockOffset map(final ClockOffsetEntity entity) {
    final ClockOffset ret = new ClockOffset();
    ret.setHours(entity.getHours());
    ret.setMinutes(entity.getMinutes());
    ret.setSeconds(entity.getSeconds());
    return ret;
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  static ClockOffsetEntity map(
      final String tenantIdentifier,
      final ClockOffset instance,
      final Optional<ClockOffsetEntity> existingClockOffset) {
    final ClockOffsetEntity ret = new ClockOffsetEntity();
    existingClockOffset.ifPresent(x -> ret.setId(x.getId()));
    ret.setTenantIdentifier(tenantIdentifier);
    ret.setHours(instance.getHours());
    ret.setMinutes(instance.getMinutes());
    ret.setSeconds(instance.getSeconds());
    return ret;
  }
}
