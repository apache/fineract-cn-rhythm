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
package io.mifos.rhythm.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.CommandLogLevel;
import io.mifos.rhythm.api.v1.events.EventConstants;
import io.mifos.rhythm.service.ServiceConstants;
import io.mifos.rhythm.service.internal.command.ChangeClockOffsetCommand;
import io.mifos.rhythm.service.internal.mapper.ClockOffsetMapper;
import io.mifos.rhythm.service.internal.repository.ClockOffsetEntity;
import io.mifos.rhythm.service.internal.repository.ClockOffsetRepository;
import io.mifos.rhythm.service.internal.service.Drummer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class ClockOffsetCommandHandler {
  private final ClockOffsetRepository clockOffsetRepository;
  private final Drummer drummer;
  private final EventHelper eventHelper;
  private final Logger logger;

  @Autowired
  public ClockOffsetCommandHandler(
      final ClockOffsetRepository clockOffsetRepository,
      final Drummer drummer,
      final EventHelper eventHelper,
      @Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger) {
    super();
    this.clockOffsetRepository = clockOffsetRepository;
    this.drummer = drummer;
    this.eventHelper = eventHelper;
    this.logger = logger;
  }

  @Transactional
  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.NONE)
  public void process(final ChangeClockOffsetCommand changeClockOffsetCommand) {

    final Optional<ClockOffsetEntity> oldClockOffsetEntity =
        clockOffsetRepository.findByTenantIdentifier(changeClockOffsetCommand.getTenantIdentifier());

    final ClockOffsetEntity newOffsetEntity = ClockOffsetMapper.map(
        changeClockOffsetCommand.getTenantIdentifier(),
        changeClockOffsetCommand.getInstance(),
        oldClockOffsetEntity);

    clockOffsetRepository.save(newOffsetEntity);

    drummer.realignAllBeatsForTenant(
        changeClockOffsetCommand.getTenantIdentifier(),
        oldClockOffsetEntity.orElseGet(ClockOffsetEntity::new),
        newOffsetEntity);

    logger.info("Sending change clock offset event.");
    eventHelper.sendEvent(
        EventConstants.PUT_CLOCKOFFSET,
        changeClockOffsetCommand.getTenantIdentifier(),
        changeClockOffsetCommand.getInstance());
  }
}