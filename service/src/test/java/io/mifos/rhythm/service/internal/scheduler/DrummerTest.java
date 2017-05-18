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

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
public class DrummerTest {

  @Test
  public void incrementToAlignment() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final LocalDateTime tomorrow = Drummer.incrementToAlignment(now, 3);

    Assert.assertEquals(tomorrow.minusDays(1).truncatedTo(ChronoUnit.DAYS), now.truncatedTo(ChronoUnit.DAYS));
    Assert.assertEquals(3, tomorrow.getHour());
  }

  @Test
  public void getTimesNeedingEvents() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final Stream<LocalDateTime> noStartTime = Drummer.getTimesNeedingEvents(null, now.plus(3, ChronoUnit.DAYS), 0);
    Assert.assertEquals(1, noStartTime.count());

    final Stream<LocalDateTime> threeDaysStartTime = Drummer.getTimesNeedingEvents(now, now.plus(3, ChronoUnit.DAYS), 0);
    Assert.assertEquals(3, threeDaysStartTime.count());

    final Stream<LocalDateTime> eventsAlreadyDone = Drummer.getTimesNeedingEvents(now, now.minus(1, ChronoUnit.DAYS), 0);
    Assert.assertEquals(0, eventsAlreadyDone.count());
  }
}