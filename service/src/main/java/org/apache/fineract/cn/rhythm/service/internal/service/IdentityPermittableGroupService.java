/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.rhythm.service.internal.service;

import java.util.Optional;
import org.apache.fineract.cn.rhythm.service.ServiceConstants;
import org.apache.fineract.cn.rhythm.service.internal.repository.ApplicationEntity;
import org.apache.fineract.cn.rhythm.service.internal.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Myrle Krantz
 */
@Service
public class IdentityPermittableGroupService {
  private final ApplicationRepository applicationRepository;
  private final BeatPublisherService beatPublisherService;
  private final Logger logger;

  @Autowired
  public IdentityPermittableGroupService(
      final ApplicationRepository applicationRepository,
      final BeatPublisherService beatPublisherService,
      @Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger) {
    this.applicationRepository = applicationRepository;
    this.beatPublisherService = beatPublisherService;
    this.logger = logger;
  }

  public synchronized boolean checkThatApplicationHasRequestForAccessPermission(
          final String tenantIdentifier,
          final String applicationIdentifier) {
    try {
      logger.info("checkThatApplicationHasRequestForAccessPermission begin");
      return checkThatApplicationHasRequestForAccessPermissionHelper(tenantIdentifier, applicationIdentifier);
    }
    catch (final DataIntegrityViolationException e) {
      return false;
    }
  }

  @SuppressWarnings("WeakerAccess")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean checkThatApplicationHasRequestForAccessPermissionHelper(String tenantIdentifier, String applicationIdentifier) {
    final Optional<ApplicationEntity> findApplication = applicationRepository.findByTenantIdentifierAndApplicationIdentifier(
            tenantIdentifier,
            applicationIdentifier);
    if (findApplication.isPresent())
      return true;
    else {
      final Optional<String> ret = beatPublisherService.requestPermissionForBeats(tenantIdentifier, applicationIdentifier);

      ret.ifPresent(x -> {
        final ApplicationEntity saveApplication = new ApplicationEntity();
        saveApplication.setTenantIdentifier(tenantIdentifier);
        saveApplication.setApplicationIdentifier(applicationIdentifier);
        saveApplication.setConsumerPermittableGroupIdentifier(x);
        applicationRepository.save(saveApplication);
      });

      return ret.isPresent();
    }
  }
}
