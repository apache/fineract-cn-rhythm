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

import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import io.mifos.core.test.fixture.mariadb.MariaDBInitializer;
import io.mifos.core.test.listener.EnableEventRecording;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.rhythm.api.v1.client.RhythmManager;
import io.mifos.rhythm.api.v1.domain.Beat;
import io.mifos.rhythm.api.v1.events.BeatEvent;
import io.mifos.rhythm.api.v1.events.EventConstants;
import io.mifos.rhythm.service.RhythmConfiguration;
import io.mifos.rhythm.service.internal.service.BeatPublisherService;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = {AbstractRhythmTest.TestConfiguration.class},
        properties = {"rhythm.user=homer", "rhythm.beatCheckRate=1000"}
)
public class AbstractRhythmTest {

  private static final String APP_NAME = "rhythm-v1";

  @Configuration
  @EnableEventRecording
  @EnableFeignClients(basePackages = {"io.mifos.rhythm.api.v1.client"})
  @RibbonClient(name = APP_NAME)
  @Import({RhythmConfiguration.class})
  @ComponentScan("io.mifos.rhythm.listener")
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean()
    public Logger logger() {
      return LoggerFactory.getLogger("test-logger");
    }
  }
  private static final String TEST_USER = "homer";

  private final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static MariaDBInitializer mariaDBInitializer = new MariaDBInitializer();
  private final static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer, mariaDBInitializer);

  @ClassRule
  public static TestRule orderClassRules = RuleChain
          .outerRule(testEnvironment)
          .around(cassandraInitializer)
          .around(mariaDBInitializer)
          .around(tenantDataStoreContext);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
          = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment, this::waitForInitialize);

  private AutoUserContext userContext;

  @Autowired
  RhythmManager testSubject;

  @Autowired
  EventRecorder eventRecorder;

  @MockBean
  BeatPublisherService beatPublisherService;

  @Before
  public void prepTest() {
    userContext = tenantApplicationSecurityEnvironment.createAutoUserContext(TEST_USER);
  }

  @After
  public void cleanTest() {
    userContext.close();
    eventRecorder.clear();
  }

  public boolean waitForInitialize() {
    try {
      return this.eventRecorder.wait(EventConstants.INITIALIZE, EventConstants.INITIALIZE);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  Beat createBeat(final String applicationName, final String beatIdentifier) throws InterruptedException {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

    final Beat beat = new Beat();
    beat.setIdentifier(beatIdentifier);
    beat.setAlignmentHour(now.getHour());

    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, beat.getAlignmentHour());
    Mockito.when(beatPublisherService.publishBeat(Matchers.eq(applicationName), Matchers.eq(beatIdentifier),
                    AdditionalMatchers.or(Matchers.eq(expectedBeatTimestamp), Matchers.eq(getNextTimeStamp(expectedBeatTimestamp)))))
            .thenReturn(true);

    this.testSubject.createBeat(applicationName, beat);

    Assert.assertTrue(this.eventRecorder.wait(EventConstants.POST_BEAT, new BeatEvent(applicationName, beat.getIdentifier())));

    TimeUnit.SECONDS.sleep(2);

    Mockito.verify(beatPublisherService, Mockito.times(1)).publishBeat(applicationName, beatIdentifier, expectedBeatTimestamp);

    return beat;
  }

  LocalDateTime getExpectedBeatTimestamp(final LocalDateTime fromTime, final Integer alignmentHour) {
    final LocalDateTime midnight = fromTime.truncatedTo(ChronoUnit.DAYS);
    return midnight.plusHours(alignmentHour);
  }

  private LocalDateTime getNextTimeStamp(final LocalDateTime fromTime) {
    return fromTime.plusDays(1);
  }
}
