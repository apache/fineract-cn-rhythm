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
package io.mifos.rhythm.service.internal.scheduler;

import io.mifos.core.lang.AutoTenantContext;
import io.mifos.rhythm.service.ServiceConstants;
import io.mifos.rhythm.service.internal.repository.BeatEntity;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import io.mifos.rhythm.service.internal.service.BeatPublisherService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Component
public class Drummer {
  private final BeatPublisherService beatPublisherService;
  private final BeatRepository beatRepository;
  private final Logger logger;

  public Drummer(
          final BeatPublisherService beatPublisherService,
          final BeatRepository beatRepository,
          @Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger) {
    this.beatPublisherService = beatPublisherService;
    this.beatRepository = beatRepository;
    this.logger = logger;
  }

  @Scheduled(initialDelayString = "${rhythm.beatCheckRate}", fixedRateString = "${rhythm.beatCheckRate}")
  @Transactional
  public void checkForBeatsNeeded() {
    try {
      final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
      int alignmentHour = now.getHour();
      //Get beats from the last two hours in case restart/start happens close to hour begin.
      final Stream<BeatEntity> beats =
              Stream.concat(beatRepository.findByAlignmentHour(alignmentHour),
                      beatRepository.findByAlignmentHour(minus1(alignmentHour)));
      beats.forEach((beat) -> checkBeatForPublish(beat, now));
    }
    catch (final InvalidDataAccessResourceUsageException e) {
      logger.info("InvalidDataAccessResourceUsageException in check for scheduled beats, probably " +
              "because initialize hasn't been called yet. {}", e);
    }
  }

  static private int minus1(int alignmentHour) {
    return alignmentHour != 0 ? alignmentHour-1 : 23;
  }

  public void checkBeatForPublish(final BeatEntity beat, final LocalDateTime now) {
    try (final AutoTenantContext ignored = new AutoTenantContext((beat.getTenantIdentifier()))) {
      final LocalDateTime topOfToday = now.truncatedTo(ChronoUnit.DAYS);
      final LocalDateTime mostRecentEventRequired = topOfToday.plusHours(beat.getAlignmentHour());

      logger.info("Checking if beat {} needs publishing.", beat);

      final Optional<LocalDateTime> firstLocalDatetimeNotPublished
              = getTimesNeedingEvents(beat.getLastPublishedFor(), mostRecentEventRequired, beat.getAlignmentHour())
              .filter(x -> {
                if (beatPublisherService.publishBeat(beat.getApplicationName(), beat.getBeatIdentifier(), x)) {
                  beat.setLastPublishedFor(x);
                  beatRepository.save(beat);
                  return false;
                }
                return true;
              })
              .findFirst();

      firstLocalDatetimeNotPublished.ifPresent(x ->
              logger.warn("Failed to publish the beat {} for all time stamps after and including {} ", beat, x));
    }
  }

  static Stream<LocalDateTime> getTimesNeedingEvents(
          final @Nullable LocalDateTime lastEventTime,
          final LocalDateTime mostRecentEventRequired,
          final Integer alignmentHour) {
    if (lastEventTime == null) {
      return Stream.of(mostRecentEventRequired);
    }

    if (lastEventTime.compareTo(mostRecentEventRequired) >= 0) {
      return Stream.empty();
    }

    final long days = lastEventTime.until(mostRecentEventRequired, ChronoUnit.DAYS);
    return Stream.iterate(incrementToAlignment(lastEventTime, alignmentHour),
            (lastPublishDate) -> incrementToAlignment(lastPublishDate, alignmentHour))
            .limit(days);
  }

  static LocalDateTime incrementToAlignment(final LocalDateTime toIncrement, final Integer alignmentHour)
  {
    return toIncrement.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(alignmentHour);
  }
}