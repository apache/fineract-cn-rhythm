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

import io.mifos.core.api.util.ApiFactory;
import io.mifos.core.lang.DateConverter;
import io.mifos.rhythm.service.config.RhythmProperties;
import io.mifos.rhythm.spi.v1.client.BeatListener;
import io.mifos.rhythm.spi.v1.domain.BeatPublish;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Myrle Krantz
 */
@Service
public class BeatPublisherService {
  private final DiscoveryClient discoveryClient;
  private final ApiFactory apiFactory;
  private final RhythmProperties properties;
  private final Logger logger;

  @Autowired
  public BeatPublisherService(
          final DiscoveryClient discoveryClient,
          final ApiFactory apiFactory,
          final RhythmProperties properties,
          final Logger logger) {
    this.discoveryClient = discoveryClient;
    this.apiFactory = apiFactory;
    this.properties = properties;
    this.logger = logger;
  }

  public boolean publishBeat(final String applicationName, final String beatIdentifier, final LocalDateTime timestamp) {
    final BeatPublish beatPublish = new BeatPublish(beatIdentifier, DateConverter.toIsoString(timestamp));
    logger.info("Attempting publish {} with timestamp {} under user {}.", beatPublish, timestamp, properties.getUser());

    final List<ServiceInstance> applicationsByName = discoveryClient.getInstances(applicationName);
    if (applicationsByName.isEmpty())
      return false;

    final ServiceInstance beatListenerService = applicationsByName.get(0);
    final BeatListener beatListener = apiFactory.create(BeatListener.class, beatListenerService.getUri().toString());

    try {
      beatListener.publishBeat(beatPublish);
      return true;
    }
    catch (final Throwable e) {
      return false;
    }
  }
}