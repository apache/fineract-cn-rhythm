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
package org.apache.fineract.cn.rhythm.service.internal.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.fineract.cn.rhythm.api.v1.domain.ClockOffset;
import org.apache.fineract.cn.rhythm.service.ServiceConstants;
import org.apache.fineract.cn.rhythm.service.internal.mapper.BeatMapper;
import org.apache.fineract.cn.rhythm.service.internal.repository.BeatEntity;
import org.apache.fineract.cn.rhythm.service.internal.repository.BeatRepository;
import org.apache.fineract.cn.rhythm.service.internal.repository.ClockOffsetEntity;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Myrle Krantz
 */
@Component
public class Drummer {
  private final IdentityPermittableGroupService identityPermittableGroupService;
  private final BeatPublisherService beatPublisherService;
  private final BeatRepository beatRepository;
  private final ClockOffsetService clockOffsetService;
  private final Logger logger;

  @Autowired
  public Drummer(
      final IdentityPermittableGroupService identityPermittableGroupService,
      final BeatPublisherService beatPublisherService,
      final BeatRepository beatRepository,
      final ClockOffsetService clockOffsetService,
      @Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger) {
    this.identityPermittableGroupService = identityPermittableGroupService;
    this.beatPublisherService = beatPublisherService;
    this.beatRepository = beatRepository;
    this.clockOffsetService = clockOffsetService;
    this.logger = logger;
  }

  @Scheduled(initialDelayString = "${rhythm.beatCheckRate}", fixedRateString = "${rhythm.beatCheckRate}")
  @Transactional
  public synchronized void checkForBeatsNeeded() {
    logger.info("checkForBeatsNeeded begin.");
    //In it's current form this function cannot be run in multiple instances of the same service.  We need to get
    //locking on selected entries corrected here, before this will work.
    try {
      final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
      //Get beats from the last two hours in case restart/start happens close to hour begin.
      final Stream<BeatEntity> beats = beatRepository.findByNextBeatBefore(now);
      beats.forEach((beat) -> {
        final boolean applicationHasRequestForAccessPermission
                = identityPermittableGroupService.checkThatApplicationHasRequestForAccessPermission(
                beat.getTenantIdentifier(), beat.getApplicationIdentifier());
        if (!applicationHasRequestForAccessPermission) {
          logger.info("Not checking if beat {} needs publishing, because application access needed to publish is not available.", beat);
        }
        else {
          logger.info("Checking if beat {} needs publishing.", beat);
          final LocalDateTime nextBeat = checkBeatForPublish(
                  now,
                  beat.getBeatIdentifier(),
                  beat.getTenantIdentifier(),
                  beat.getApplicationIdentifier(),
                  beat.getAlignmentHour(),
                  beat.getNextBeat());
          if (!nextBeat.equals(beat.getNextBeat())) {
            beat.setNextBeat(nextBeat);
            beatRepository.save(beat);
          }
          logger.info("Beat updated to {}.", beat);
        }
      });

    }
    catch (final InvalidDataAccessResourceUsageException e) {
      logger.info("InvalidDataAccessResourceUsageException in check for scheduled beats, probably " +
              "because initialize hasn't been called yet. {}", e);
    }
    logger.info("checkForBeatsNeeded end.");
  }

  @Transactional
  public synchronized void realignAllBeatsForTenant(
      final String tenantIdentifier,
      final ClockOffsetEntity oldClockOffset,
      final ClockOffsetEntity newClockOffset)
  {
    final Stream<BeatEntity> beatsToAdjust = beatRepository.findByTenantIdentifier(tenantIdentifier);
    beatsToAdjust.forEach(x -> {
      //Need to subtract old clock offset, because for large clock offsets and large alignments,
      //time can "skip" into the next day through realignment.
      final LocalDateTime oldBeatNextBeat = x.getNextBeat()
          .minusHours(oldClockOffset.getHours())
          .minusMinutes(oldClockOffset.getMinutes())
          .minusSeconds(oldClockOffset.getSeconds());
      x.setNextBeat(BeatMapper.alignDateTime(
          oldBeatNextBeat,
          x.getAlignmentHour(),
          newClockOffset));
      beatRepository.save(x);
    });
  }

  private LocalDateTime checkBeatForPublish(
      final LocalDateTime now,
      final String beatIdentifier,
      final String tenantIdentifier,
      final String applicationIdentifier,
      final Integer alignmentHour,
      final LocalDateTime nextBeat) {
    final ClockOffset clockOffset = clockOffsetService.findByTenantIdentifier(tenantIdentifier);
    return checkBeatForPublishHelper(now, alignmentHour, nextBeat, clockOffset,
            x -> beatPublisherService.publishBeat(beatIdentifier, tenantIdentifier, applicationIdentifier, x));
  }

  //Helper is separated from original function so that it can be unit-tested separately from publishBeat.
  static LocalDateTime checkBeatForPublishHelper(
          final LocalDateTime now,
          final Integer alignmentHour,
          final LocalDateTime nextBeat,
          final ClockOffset clockOffset,
          final Predicate<LocalDateTime> publishSucceeded) {
    LocalDateTime beatToPublish = nextBeat;
    for (;
         !beatToPublish.isAfter(now);
         beatToPublish = incrementToAlignment(beatToPublish, alignmentHour, clockOffset))
    {
      if (!publishSucceeded.test(beatToPublish))
        break;
    }

    return beatToPublish;
  }

  static LocalDateTime incrementToAlignment(
      final LocalDateTime toIncrement,
      final Integer alignmentHour,
      final ClockOffset clockOffset)
  {
    return BeatMapper.alignDateTime(toIncrement.plusDays(1), alignmentHour, clockOffset);
  }
}