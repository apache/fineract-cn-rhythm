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
package org.apache.fineract.cn.rhythm.service.internal.command.handler;

import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.CommandLogLevel;
import org.apache.fineract.cn.rhythm.api.v1.domain.ClockOffset;
import org.apache.fineract.cn.rhythm.api.v1.events.BeatEvent;
import org.apache.fineract.cn.rhythm.api.v1.events.EventConstants;
import org.apache.fineract.cn.rhythm.service.ServiceConstants;
import org.apache.fineract.cn.rhythm.service.internal.command.CreateBeatCommand;
import org.apache.fineract.cn.rhythm.service.internal.command.DeleteBeatCommand;
import org.apache.fineract.cn.rhythm.service.internal.mapper.BeatMapper;
import org.apache.fineract.cn.rhythm.service.internal.repository.BeatEntity;
import org.apache.fineract.cn.rhythm.service.internal.repository.BeatRepository;
import org.apache.fineract.cn.rhythm.service.internal.service.ClockOffsetService;
import org.apache.fineract.cn.rhythm.service.internal.service.IdentityPermittableGroupService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class BeatCommandHandler {
  private final IdentityPermittableGroupService identityPermittableGroupService;
  private final BeatRepository beatRepository;
  private final ClockOffsetService clockOffsetService;
  private final EventHelper eventHelper;
  private final Logger logger;

  @Autowired
  public BeatCommandHandler(
      final IdentityPermittableGroupService identityPermittableGroupService,
      final BeatRepository beatRepository,
      final ClockOffsetService clockOffsetService,
      final EventHelper eventHelper,
      @Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger) {
    super();
    this.identityPermittableGroupService = identityPermittableGroupService;
    this.beatRepository = beatRepository;
    this.clockOffsetService = clockOffsetService;
    this.eventHelper = eventHelper;
    this.logger = logger;
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.NONE)
  public void process(final CreateBeatCommand createBeatCommand) {
    processCreateBeatCommand(createBeatCommand);

    final BeatEvent event
            = new BeatEvent(createBeatCommand.getApplicationIdentifier(), createBeatCommand.getInstance().getIdentifier());
    logger.info("Sending event {}", event);
    eventHelper.sendEvent(EventConstants.POST_BEAT, createBeatCommand.getTenantIdentifier(), event);
  }

  //I want the transaction to close before I send a beat or log it being sent.  So I need a separate function for the
  //stuff that should happen in the transaction.
  @SuppressWarnings("WeakerAccess")
  @Transactional
  public void processCreateBeatCommand(final CreateBeatCommand createBeatCommand) {
    final boolean applicationHasRequestForAccessPermission = identityPermittableGroupService.checkThatApplicationHasRequestForAccessPermission(
        createBeatCommand.getTenantIdentifier(), createBeatCommand.getApplicationIdentifier());
    if (!applicationHasRequestForAccessPermission) {
      logger.info("Rhythm needs permission to publish beats to application, but couldn't request that permission for tenant '{}' and application '{}'.",
          createBeatCommand.getTenantIdentifier(), createBeatCommand.getApplicationIdentifier());
    }
    final ClockOffset clockOffset = clockOffsetService.findByTenantIdentifier(createBeatCommand.getTenantIdentifier());

    final BeatEntity entity = BeatMapper.map(
        createBeatCommand.getTenantIdentifier(),
        createBeatCommand.getApplicationIdentifier(),
        createBeatCommand.getInstance(),
        clockOffset);
    this.beatRepository.save(entity);
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.NONE)
  @Transactional
  public void process(final DeleteBeatCommand deleteBeatCommand) {
    this.beatRepository.deleteByTenantIdentifierAndApplicationIdentifierAndBeatIdentifier(
            deleteBeatCommand.getTenantIdentifier(),
            deleteBeatCommand.getApplicationIdentifier(),
            deleteBeatCommand.getIdentifier());

    eventHelper.sendEvent(EventConstants.DELETE_BEAT, deleteBeatCommand.getTenantIdentifier(),
            new BeatEvent(deleteBeatCommand.getApplicationIdentifier(), deleteBeatCommand.getIdentifier()));
  }
}
