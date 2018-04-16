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

import org.apache.fineract.cn.rhythm.api.v1.domain.Beat;
import org.apache.fineract.cn.rhythm.api.v1.domain.ClockOffset;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.fineract.cn.rhythm.service.internal.repository.BeatEntity;
import org.apache.fineract.cn.rhythm.service.internal.repository.ClockOffsetEntity;

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

  static BeatEntity map(
      final String tenantIdentifier,
      final String applicationIdentifier,
      final Beat instance,
      final ClockOffset clockOffset) {
    final BeatEntity ret = new BeatEntity();
    ret.setBeatIdentifier(instance.getIdentifier());
    ret.setTenantIdentifier(tenantIdentifier);
    ret.setApplicationIdentifier(applicationIdentifier);
    ret.setAlignmentHour(instance.getAlignmentHour());
    //First beat is today.  If it's in the past, it will be published nearly immediately.
    ret.setNextBeat(alignDateTime(
        LocalDateTime.now(Clock.systemUTC()),
        instance.getAlignmentHour(),
        clockOffset));
    return ret;
  }

  static LocalDateTime alignDateTime(
      final LocalDateTime localDateTime,
      final int alignmentHour,
      final ClockOffset clockOffset) {
    return localDateTime.truncatedTo(ChronoUnit.DAYS)
        .plusHours(alignmentHour + clockOffset.getHours())
        .plusMinutes(clockOffset.getMinutes())
        .plusSeconds(clockOffset.getSeconds());
  }

  static LocalDateTime alignDateTime(
      final LocalDateTime localDateTime,
      final int alignmentHour,
      final ClockOffsetEntity clockOffset) {
    return localDateTime.truncatedTo(ChronoUnit.DAYS)
        .plusHours(alignmentHour + clockOffset.getHours())
        .plusMinutes(clockOffset.getMinutes())
        .plusSeconds(clockOffset.getSeconds());
  }
}
