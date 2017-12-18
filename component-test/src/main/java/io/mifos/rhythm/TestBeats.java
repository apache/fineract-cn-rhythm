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
package io.mifos.rhythm;

import io.mifos.core.api.util.NotFoundException;
import io.mifos.rhythm.api.v1.domain.Beat;
import io.mifos.rhythm.api.v1.domain.ClockOffset;
import io.mifos.rhythm.api.v1.events.BeatEvent;
import io.mifos.rhythm.api.v1.events.EventConstants;
import io.mifos.rhythm.service.internal.repository.BeatEntity;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
public class TestBeats extends AbstractRhythmTest {
  @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
  @Autowired
  BeatRepository beatRepository;

  @Test
  public void shouldCreateBeat() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v1";
    final Beat beat = createBeatForThisHour(applicationIdentifier, "bebopthedowop");

    final Beat createdBeat = this.testSubject.getBeat(applicationIdentifier, beat.getIdentifier());
    Assert.assertEquals(beat, createdBeat);

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertTrue(allEntities.contains(beat));
  }

  @Test
  public void shouldDeleteBeat() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v2";

    final Beat beat = createBeatForThisHour(applicationIdentifier, "bebopthedowop");

    testSubject.deleteBeat(applicationIdentifier, beat.getIdentifier());
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_BEAT, new BeatEvent(applicationIdentifier, beat.getIdentifier())));

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertFalse(allEntities.contains(beat));

    try {
      this.testSubject.getBeat(applicationIdentifier, beat.getIdentifier());
      Assert.fail("NotFoundException should be thrown.");
    }
    catch (final NotFoundException ignored) { }
  }

  @Test
  public void shouldDeleteApplication() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v3";
    createBeatForThisHour(applicationIdentifier, "bebopthedowop");

    this.testSubject.deleteApplication(applicationIdentifier);
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_APPLICATION, applicationIdentifier));

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertTrue(allEntities.isEmpty());
  }

  @Test
  public void shouldRetryBeatPublishIfFirstAttemptFails() throws InterruptedException {
    final String tenantIdentifier = tenantDataStoreContext.getTenantName();
    final String applicationIdentifier = "funnybusiness-v4";
    final String beatId = "bebopthedowop";

    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

    final Beat beat = new Beat();
    beat.setIdentifier(beatId);
    beat.setAlignmentHour(now.getHour());

    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, beat.getAlignmentHour());

    Mockito.doReturn(Optional.of("boop")).when(beatPublisherServiceMock).requestPermissionForBeats(Matchers.eq(tenantIdentifier), Matchers.eq(applicationIdentifier));
    Mockito.when(beatPublisherServiceMock.publishBeat(beatId, tenantIdentifier, applicationIdentifier, expectedBeatTimestamp)).thenReturn(false, false, true);

    this.testSubject.createBeat(applicationIdentifier, beat);

    Assert.assertTrue(this.eventRecorder.wait(EventConstants.POST_BEAT, new BeatEvent(applicationIdentifier, beat.getIdentifier())));

    Mockito.verify(beatPublisherServiceMock, Mockito.timeout(10_000).times(3)).publishBeat(beatId, tenantIdentifier, applicationIdentifier, expectedBeatTimestamp);
  }

  @Test
  public void twentyFourBeats() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v5";
    final LocalDateTime today = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS);
    final List<Beat> beats = new ArrayList<>();
    for (int i = 0; i < 24; i ++) {
      final LocalDateTime expectedBeatTimestamp = today.plusHours(i);
      beats.add(createBeat(applicationIdentifier, "bebopthedowop" + i, i, expectedBeatTimestamp));
    }

    beats.forEach(x -> {
      final Beat createdBeat = this.testSubject.getBeat(applicationIdentifier, x.getIdentifier());
      Assert.assertEquals(x, createdBeat);
    });

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);

    beats.forEach(x -> Assert.assertTrue(allEntities.contains(x)));
  }

  @Test
  public void shouldBeatForMissingDays() throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v6";
    final String beatIdentifier = "fiddlebeat";
    createBeatForThisHour(applicationIdentifier, beatIdentifier);

    final int daysAgo = 10;
    final LocalDateTime nextBeat = setBack(applicationIdentifier, beatIdentifier, daysAgo);

    for (int i = daysAgo; i > 0; i--) {
      Mockito.verify(beatPublisherServiceMock, Mockito.timeout(4_000).times(1))
          .publishBeat(
              beatIdentifier,
              tenantDataStoreContext.getTenantName(),
              applicationIdentifier,
              nextBeat.minusDays(daysAgo));
    }
  }

  @Test
  public void clockOffsetShouldEffectBeatTiming() throws InterruptedException {
    final String tenantIdentifier = tenantDataStoreContext.getTenantName();
    final String applicationIdentifier = "funnybusiness-v7";
    final String beatIdentifier = "fiddlebeat0";

    final ClockOffset initialClockOffset = this.testSubject.getClockOffset();
    Assert.assertEquals(Integer.valueOf(0), initialClockOffset.getHours());
    Assert.assertEquals(Integer.valueOf(0), initialClockOffset.getMinutes());
    Assert.assertEquals(Integer.valueOf(0), initialClockOffset.getSeconds());

    final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
    final ClockOffset offsetToNow = new ClockOffset(now.getHour(), now.getMinute(), now.getSecond());
    this.testSubject.setClockOffset(offsetToNow);

    Assert.assertTrue(this.eventRecorder.wait(EventConstants.PUT_CLOCKOFFSET, offsetToNow));

    final ClockOffset changedClockOffset = this.testSubject.getClockOffset();
    Assert.assertEquals(offsetToNow, changedClockOffset);

    final Beat beat = new Beat();
    beat.setIdentifier(beatIdentifier);
    beat.setAlignmentHour(0);

    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, 0, offsetToNow);

    Mockito.doReturn(Optional.of("boop")).when(beatPublisherServiceMock)
        .requestPermissionForBeats(Matchers.eq(tenantIdentifier), Matchers.eq(applicationIdentifier));
    Mockito.when(beatPublisherServiceMock
        .publishBeat(beatIdentifier, tenantIdentifier, applicationIdentifier, expectedBeatTimestamp))
        .thenReturn(true);

    this.testSubject.createBeat(applicationIdentifier, beat);

    Assert.assertTrue(this.eventRecorder.wait(EventConstants.POST_BEAT, new BeatEvent(applicationIdentifier, beat.getIdentifier())));

    Mockito.verify(beatPublisherServiceMock, Mockito.timeout(10_000).times(1)).publishBeat(beatIdentifier, tenantIdentifier, applicationIdentifier, expectedBeatTimestamp);

    this.testSubject.setClockOffset(initialClockOffset);
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.PUT_CLOCKOFFSET, initialClockOffset));
  }

  @Transactional
  LocalDateTime setBack(
      final String applicationIdentifier,
      final String beatIdentifier,
      final int daysAgo) {

    final BeatEntity beatEntity = beatRepository.findByTenantIdentifierAndApplicationIdentifierAndBeatIdentifier(
        tenantDataStoreContext.getTenantName(),
        applicationIdentifier,
        beatIdentifier).orElseThrow(IllegalStateException::new);

    Mockito.reset(beatPublisherServiceMock);
    final LocalDateTime nextBeat = beatEntity.getNextBeat();

    beatEntity.setNextBeat(nextBeat.minusDays(daysAgo));

    beatRepository.save(beatEntity);

    return nextBeat;
  }
}