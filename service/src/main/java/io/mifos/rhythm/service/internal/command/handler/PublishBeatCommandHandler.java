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
package io.mifos.rhythm.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.lang.ServiceException;
import io.mifos.rhythm.service.config.RhythmProperties;
import io.mifos.rhythm.service.internal.command.CheckPublishBeatCommand;
import io.mifos.rhythm.service.internal.repository.BeatEntity;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class PublishBeatCommandHandler {

  private final BeatRepository publishedBeatRepository;
  private final RhythmProperties properties;
  private final Logger logger;

  public PublishBeatCommandHandler(
          final BeatRepository publishedBeatRepository,
          final RhythmProperties properties,
          final Logger logger) {
    this.publishedBeatRepository = publishedBeatRepository;
    this.properties = properties;
    this.logger = logger;
  }

  @CommandHandler
  @Transactional
  public void process(final CheckPublishBeatCommand checkPublishBeatCommand) {
    final BeatEntity beat
            = this.publishedBeatRepository.findByTenantIdentifierAndApplicationNameAndBeatIdentifier(
            checkPublishBeatCommand.getTenantIdentifier(),
            checkPublishBeatCommand.getApplicationName(),
            checkPublishBeatCommand.getBeatIdentifier())
            .orElseThrow(
                    () -> ServiceException.notFound("Could not publish the beat because it was not found.  Tenant '{}', Application '{}', Beat '{}'",
                            checkPublishBeatCommand.getTenantIdentifier(),
                            checkPublishBeatCommand.getApplicationName(),
                            checkPublishBeatCommand.getBeatIdentifier()));


    getTimesNeedingEvents(beat.getLastPublishedOn(), checkPublishBeatCommand.getPublishedSince(), beat.getAlignmentHour())
            .forEach(x -> {
              //TODO: Actually send the event.
              logger.info("This is where the beat {} should be published with timestamp {} under user {}.", beat, x, properties.getUser());
              beat.setLastPublishedOn(LocalDateTime.now(ZoneId.of("UTC")));
              this.publishedBeatRepository.save(beat);
            });
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