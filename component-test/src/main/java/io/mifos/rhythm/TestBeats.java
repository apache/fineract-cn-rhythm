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
import io.mifos.rhythm.api.v1.domain.Application;
import io.mifos.rhythm.api.v1.domain.Beat;
import io.mifos.rhythm.api.v1.events.BeatEvent;
import io.mifos.rhythm.api.v1.events.EventConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Myrle Krantz
 */
public class TestBeats extends AbstractRhythmTest {
  @Test
  public void shouldCreateBeat() throws InterruptedException {
    final Application application = createApplication("funnybusiness-v1");

    final Beat beat = createBeat(application, "bebopthedowop");

    final Beat createdBeat = this.testSubject.getBeat(application.getApplicationName(), beat.getIdentifier());
    Assert.assertEquals(beat, createdBeat);

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(application.getApplicationName());
    Assert.assertTrue(allEntities.contains(beat));
  }

  @Test
  public void shouldDeleteBeat() throws InterruptedException {
    final Application application = createApplication("funnybusiness-v2");

    final Beat beat = createBeat(application, "bebopthedowop");

    testSubject.deleteBeat(application.getApplicationName(), beat.getIdentifier());
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_BEAT, new BeatEvent(application.getApplicationName(), beat.getIdentifier())));

    final List<Beat> allEntities = this.testSubject.getAllBeatsForApplication(application.getApplicationName());
    Assert.assertFalse(allEntities.contains(beat));

    try {
      this.testSubject.getBeat(application.getApplicationName(), beat.getIdentifier());
      Assert.fail("NotFoundException should be thrown.");
    }
    catch (final NotFoundException ignored) { }
  }
}
