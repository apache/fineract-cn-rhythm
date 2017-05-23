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
import io.mifos.rhythm.api.v1.events.BeatEvent;
import io.mifos.rhythm.api.v1.events.EventConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
public class TestBeats extends AbstractRhythmTest {

  @Test
  public void shouldCreateBeat() throws InterruptedException {
    final String appName = "funnybusiness-v1";
    final Beat beat = createBeat(appName, "bebopthedowop");

    final Beat createdBeat = this.testSubject.getBeat(appName, beat.getIdentifier());
    Assert.assertEquals(beat, createdBeat);

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(appName);
    Assert.assertTrue(allEntities.contains(beat));
  }

  @Test
  public void shouldDeleteBeat() throws InterruptedException {
    final String appName = "funnybusiness-v2";

    final Beat beat = createBeat(appName, "bebopthedowop");

    testSubject.deleteBeat(appName, beat.getIdentifier());
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_BEAT, new BeatEvent(appName, beat.getIdentifier())));

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(appName);
    Assert.assertFalse(allEntities.contains(beat));

    try {
      this.testSubject.getBeat(appName, beat.getIdentifier());
      Assert.fail("NotFoundException should be thrown.");
    }
    catch (final NotFoundException ignored) { }
  }

  @Test
  public void shouldDeleteApplication() throws InterruptedException {
    final String appName = "funnybusiness-v3";
    createBeat(appName, "bebopthedowop");

    this.testSubject.deleteApplication(appName);
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_APPLICATION, appName));

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(appName);
    Assert.assertTrue(allEntities.isEmpty());
  }

  @Test
  public void shouldRetryBeatPublishIfFirstAttemptFails() throws InterruptedException {
    final String tenantIdentifier = tenantDataStoreContext.getTenantName();
    final String appName = "funnybusiness-v4";
    final String beatId = "bebopthedowop";

    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

    final Beat beat = new Beat();
    beat.setIdentifier(beatId);
    beat.setAlignmentHour(now.getHour());

    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, beat.getAlignmentHour());

    Mockito.doReturn(Optional.of("boop")).when(beatPublisherServiceSpy).requestPermissionForBeats(Matchers.eq(tenantIdentifier), Matchers.eq(appName));
    Mockito.when(beatPublisherServiceSpy.publishBeat(beatId, tenantIdentifier, appName, expectedBeatTimestamp)).thenReturn(false, false, true);

    this.testSubject.createBeat(appName, beat);

    Assert.assertTrue(this.eventRecorder.wait(EventConstants.POST_BEAT, new BeatEvent(appName, beat.getIdentifier())));

    Mockito.verify(beatPublisherServiceSpy, Mockito.timeout(10_000).times(3)).publishBeat(beatId, tenantIdentifier, appName, expectedBeatTimestamp);
  }
}