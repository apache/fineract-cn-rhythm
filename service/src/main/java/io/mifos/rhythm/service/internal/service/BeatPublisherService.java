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

import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.api.util.ApiFactory;
import io.mifos.core.lang.AutoTenantContext;
import io.mifos.core.lang.DateConverter;
import io.mifos.permittedfeignclient.service.ApplicationAccessTokenService;
import io.mifos.rhythm.service.config.RhythmProperties;
import io.mifos.rhythm.spi.v1.client.BeatListener;
import io.mifos.rhythm.spi.v1.domain.BeatPublish;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.mifos.rhythm.service.ServiceConstants.LOGGER_NAME;

/**
 * @author Myrle Krantz
 */
@Service
public class BeatPublisherService {
  private final DiscoveryClient discoveryClient;
  private final ApplicationAccessTokenService applicationAccessTokenService;
  private final ApiFactory apiFactory;
  private final RhythmProperties properties;
  private final Logger logger;

  @Autowired
  public BeatPublisherService(
          @SuppressWarnings("SpringJavaAutowiringInspection") final DiscoveryClient discoveryClient,
          @SuppressWarnings("SpringJavaAutowiringInspection") final ApplicationAccessTokenService applicationAccessTokenService,
          final ApiFactory apiFactory,
          final RhythmProperties properties,
          @Qualifier(LOGGER_NAME) final Logger logger) {
    this.discoveryClient = discoveryClient;
    this.applicationAccessTokenService = applicationAccessTokenService;
    this.apiFactory = apiFactory;
    this.properties = properties;
    this.logger = logger;
  }

  /**
   * Authenticate with identity and publish the beat to the application.  This function performs all the internal
   * interprocess communication in rhythm, and therefore most be mocked in unit and component tests.
   *
   * @param beatIdentifier The identifier of the beat as provided when the beat was created.
   * @param tenantIdentifier The tenant identifier as provided via the tenant header when the beat was created.
   * @param applicationName The name of the application the beat should be sent to.
   * @param timestamp The publication time for the beat.  If rhythm has been down for a while this could be in the past.
   *
   * @return true if the beat was published.  false if the beat was not published, or we just don't know.
   */
  @SuppressWarnings("WeakerAccess") //Access is public for spying in component test.
  public boolean publishBeat(
          final String beatIdentifier,
          final String tenantIdentifier,
          final String applicationName,
          final LocalDateTime timestamp) {
    final BeatPublish beatPublish = new BeatPublish(beatIdentifier, DateConverter.toIsoString(timestamp));
    logger.info("Attempting publish {} with timestamp {} under user {}.", beatPublish, timestamp, properties.getUser());

    final List<ServiceInstance> applicationsByName = discoveryClient.getInstances(applicationName);
    if (applicationsByName.isEmpty())
      return false;

    final ServiceInstance beatListenerService = applicationsByName.get(0);
    final BeatListener beatListener = apiFactory.create(BeatListener.class, beatListenerService.getUri().toString());

    final String accessToken = applicationAccessTokenService.getAccessToken(
            properties.getUser(), getEndointSetIdentifier(applicationName));
    try (final AutoUserContext ignored2 = new AutoUserContext(properties.getUser(), accessToken)) {
      try (final AutoTenantContext ignored = new AutoTenantContext(tenantIdentifier)) {
        beatListener.publishBeat(beatPublish);
        return true;
      }
    }
    catch (final Throwable e) {
      return false;
    }
  }

  private static String getEndointSetIdentifier(final String applicationName) {
    return applicationName.replace("-", "__") + "__khepri";
  }

  public Optional<LocalDateTime> checkBeatForPublish(
          final LocalDateTime now,
          final String beatIdentifier,
          final String tenantIdentifier,
          final String applicationName,
          final Integer alignmentHour,
          final LocalDateTime nextBeat) {
    return checkBeatForPublishHelper(now, alignmentHour, nextBeat, x -> publishBeat(beatIdentifier, tenantIdentifier, applicationName, x));
  }

  //Helper is separated from original function so that it can be unit-tested separately from publishBeat.
  static Optional<LocalDateTime> checkBeatForPublishHelper(
          final LocalDateTime now,
          final Integer alignmentHour,
          final LocalDateTime nextBeat,
          final Predicate<LocalDateTime> publishSucceeded) {
    final long numberOfBeatPublishesNeeded = getNumberOfBeatPublishesNeeded(now, nextBeat);
    if (numberOfBeatPublishesNeeded == 0)
      return Optional.empty();

    final Optional<LocalDateTime> firstFailedBeat = Stream.iterate(nextBeat,
            x -> incrementToAlignment(x, alignmentHour))
            .limit(numberOfBeatPublishesNeeded)
            .filter(x -> !publishSucceeded.test(x))
            .findFirst();

    if (firstFailedBeat.isPresent())
      return firstFailedBeat;
    else
      return Optional.of(incrementToAlignment(now, alignmentHour));
  }

  static long getNumberOfBeatPublishesNeeded(final LocalDateTime now, final @Nonnull LocalDateTime nextBeat) {
    if (nextBeat.isAfter(now))
      return 0;

    return Math.max(1, nextBeat.until(now, ChronoUnit.DAYS));
  }

  static LocalDateTime incrementToAlignment(final LocalDateTime toIncrement, final Integer alignmentHour)
  {
    return toIncrement.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(alignmentHour);
  }
}