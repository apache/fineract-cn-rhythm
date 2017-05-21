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
package io.mifos.rhythm.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.lang.ServiceException;
import io.mifos.rhythm.api.v1.events.BeatEvent;
import io.mifos.rhythm.api.v1.events.EventConstants;
import io.mifos.rhythm.service.ServiceConstants;
import io.mifos.rhythm.service.internal.command.CreateBeatCommand;
import io.mifos.rhythm.service.internal.command.DeleteBeatCommand;
import io.mifos.rhythm.service.internal.mapper.BeatMapper;
import io.mifos.rhythm.service.internal.repository.BeatEntity;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import io.mifos.rhythm.service.internal.service.IdentityPermittableGroupService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class BeatCommandHandler {
  private final IdentityPermittableGroupService identityPermittableGroupService;
  private final BeatRepository beatRepository;
  private final EventHelper eventHelper;
  private final Logger logger;

  @Autowired
  public BeatCommandHandler(
          final IdentityPermittableGroupService identityPermittableGroupService,
          final BeatRepository beatRepository,
          final EventHelper eventHelper,
          @Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger) {
    super();
    this.identityPermittableGroupService = identityPermittableGroupService;
    this.beatRepository = beatRepository;
    this.eventHelper = eventHelper;
    this.logger = logger;
  }

  @CommandHandler
  @Transactional
  public void process(final CreateBeatCommand createBeatCommand) {
    final boolean applicationHasRequestForAccessPermission = identityPermittableGroupService.checkThatApplicationHasRequestForAccessPermission(
            createBeatCommand.getTenantIdentifier(), createBeatCommand.getApplicationName());
    if (!applicationHasRequestForAccessPermission) {
      logger.warn("Rhythm needs permission to publish beats to application, but couldn't request that permission for tenant '{}' and application '{}'.",
              createBeatCommand.getApplicationName(), createBeatCommand.getTenantIdentifier());
    }

    final BeatEntity entity = BeatMapper.map(
            createBeatCommand.getTenantIdentifier(),
            createBeatCommand.getApplicationName(),
            createBeatCommand.getInstance());
    this.beatRepository.save(entity);

    eventHelper.sendEvent(EventConstants.POST_BEAT, createBeatCommand.getTenantIdentifier(),
            new BeatEvent(createBeatCommand.getApplicationName(), createBeatCommand.getInstance().getIdentifier()));
  }

  @CommandHandler
  @Transactional
  public void process(final DeleteBeatCommand deleteBeatCommand) {
    final Optional<BeatEntity> toDelete = this.beatRepository.findByTenantIdentifierAndApplicationNameAndBeatIdentifier(
            deleteBeatCommand.getTenantIdentifier(),
            deleteBeatCommand.getApplicationName(),
            deleteBeatCommand.getIdentifier());
    final BeatEntity toDeleteForReal
            = toDelete.orElseThrow(() -> ServiceException.notFound(
                    "Beat with for the application " + deleteBeatCommand.getApplicationName() +
                            ", and the identifier " + deleteBeatCommand.getIdentifier() + " not found."));

    this.beatRepository.delete(toDeleteForReal);

    eventHelper.sendEvent(EventConstants.DELETE_BEAT, deleteBeatCommand.getTenantIdentifier(),
            new BeatEvent(deleteBeatCommand.getApplicationName(), deleteBeatCommand.getIdentifier()));
  }
}
