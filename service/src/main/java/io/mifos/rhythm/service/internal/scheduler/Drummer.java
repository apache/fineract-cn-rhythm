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

import java.time.LocalDateTime;
import java.time.ZoneId;
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
      //Get beats from the last two hours in case restart/start happens close to hour begin.
      final Stream<BeatEntity> beats = beatRepository.findByNextBeatBefore(now);
      beats.forEach((beat) -> {
        logger.info("Checking if beat {} needs publishing.", beat);
        final Optional<LocalDateTime> nextBeat = beatPublisherService.checkBeatForPublish(now, beat.getBeatIdentifier(), beat.getTenantIdentifier(), beat.getApplicationName(), beat.getAlignmentHour(), beat.getNextBeat());
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
}