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

import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.rhythm.service.internal.command.CheckPublishBeatCommand;
import io.mifos.rhythm.service.internal.repository.BeatEntity;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Component
public class Drummer {
  private final BeatRepository beatRepository;
  private final CommandGateway commandGateway;

  public Drummer(
          final BeatRepository beatRepository,
          final CommandGateway commandGateway) {
    this.beatRepository = beatRepository;
    this.commandGateway = commandGateway;
  }

  //@Scheduled(fixedRate = 300_000) //TimeUnit.MINUTES.toMillis(5)
  @Scheduled(fixedRate = 500)
  public void checkForBeatsNeeded() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    int alignmentHour = now.getHour();
    final Stream<BeatEntity> beats = beatRepository.findByAlignmentHour(alignmentHour);
    beats.forEach((beat) -> publishBeat(beat, now));
  }

  private void publishBeat(final BeatEntity beat, final LocalDateTime now) {
    final LocalDateTime topOfToday = now.truncatedTo(ChronoUnit.DAYS);
    final LocalDateTime publishedSince = topOfToday.plusHours(beat.getAlignmentHour());
    commandGateway.process(
            new CheckPublishBeatCommand(beat.getTenantIdentifier(), beat.getApplicationName(), beat.getBeatIdentifier(), publishedSince));
  }
}