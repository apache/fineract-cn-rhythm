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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
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

  @Autowired
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
      beats.forEach((beat) -> {
        final Optional<LocalDateTime> nextBeat = checkBeatForPublish(beat, now);
        nextBeat.ifPresent(x -> {
          beat.setNextBeat(x);
          beatRepository.save(beat);
        });
      });

    }
    catch (final InvalidDataAccessResourceUsageException e) {
      logger.info("InvalidDataAccessResourceUsageException in check for scheduled beats, probably " +
              "because initialize hasn't been called yet. {}", e);
    }
  }

  static private int minus1(int alignmentHour) {
    return alignmentHour != 0 ? alignmentHour-1 : 23;
  }

  private Optional<LocalDateTime> checkBeatForPublish(final BeatEntity beat, final LocalDateTime now) {
    try (final AutoTenantContext ignored = new AutoTenantContext((beat.getTenantIdentifier()))) {

      logger.info("Checking if beat {} needs publishing.", beat);

      return getTimesNeedingEvents(beat.getNextBeat(), now, beat.getAlignmentHour())
              .filter(x ->
                      x.isAfter(now)
                      || !beatPublisherService.publishBeat(beat.getApplicationName(), beat.getBeatIdentifier(), x))
              .findFirst();
    }
  }

  static Stream<LocalDateTime> getTimesNeedingEvents(
          final @Nonnull LocalDateTime nextBeat,
          final LocalDateTime now,
          final Integer alignmentHour) {
    if (nextBeat.isAfter(now)) {
      return Stream.of(nextBeat);
    }

    final long days = nextBeat.until(now, ChronoUnit.DAYS) + 2;
    return Stream.iterate(nextBeat,
            x -> incrementToAlignment(x, alignmentHour))
            .limit(days);
  }

  static LocalDateTime incrementToAlignment(final LocalDateTime toIncrement, final Integer alignmentHour)
  {
    return toIncrement.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(alignmentHour);
  }
}