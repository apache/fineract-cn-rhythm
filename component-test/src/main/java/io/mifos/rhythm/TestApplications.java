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
import io.mifos.rhythm.api.v1.events.EventConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Myrle Krantz
 */
public class TestApplications extends AbstractRhythmTest {
  @Test
  public void shouldCreateApplication() throws InterruptedException {
    final Application application = createApplication("funnybusiness-v1");

    final Application createdApplication = this.testSubject.getApplication(application.getApplicationName());
    Assert.assertEquals(application, createdApplication);

    final List<Application> allEntities = this.testSubject.getAllApplications();
    Assert.assertTrue(allEntities.contains(application));
  }

  @Test
  public void shouldListApplications() {
    final List<Application> allEntities = this.testSubject.getAllApplications();
    Assert.assertNotNull(allEntities);
  }

  @Test
  public void shouldDeleteApplication() throws InterruptedException {
    final Application application = createApplication("funnybusiness-v2");

    this.testSubject.deleteApplication(application.getApplicationName());
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_APPLICATION, application.getApplicationName()));

    final List<Application> allEntities = this.testSubject.getAllApplications();
    Assert.assertFalse(allEntities.contains(application));

    try {
      this.testSubject.getApplication(application.getApplicationName());
      Assert.fail("NotFoundException should be thrown.");
    }
    catch (final NotFoundException ignored) { }
  }
}
