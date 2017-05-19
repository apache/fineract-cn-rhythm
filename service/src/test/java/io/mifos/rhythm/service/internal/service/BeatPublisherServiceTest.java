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
package io.mifos.rhythm.service.internal.service;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
public class BeatPublisherServiceTest {

  @Test
  public void incrementToAlignment() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final LocalDateTime tomorrow = BeatPublisherService.incrementToAlignment(now, 3);

    Assert.assertEquals(tomorrow.minusDays(1).truncatedTo(ChronoUnit.DAYS), now.truncatedTo(ChronoUnit.DAYS));
    Assert.assertEquals(3, tomorrow.getHour());
  }

  @Test
  public void getNumberOfBeatPublishesNeeded() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final long eventsNeeded3 = BeatPublisherService.getNumberOfBeatPublishesNeeded(now.minus(3, ChronoUnit.DAYS), now);
    Assert.assertEquals(3, eventsNeeded3);

    final long eventsNeededPast = BeatPublisherService.getNumberOfBeatPublishesNeeded(now.plus(1, ChronoUnit.DAYS), now);
    Assert.assertEquals(0, eventsNeededPast);

    final long eventsNeededNow = BeatPublisherService.getNumberOfBeatPublishesNeeded(now.minus(2, ChronoUnit.MINUTES), now);
    Assert.assertEquals(1, eventsNeededNow);
  }
}