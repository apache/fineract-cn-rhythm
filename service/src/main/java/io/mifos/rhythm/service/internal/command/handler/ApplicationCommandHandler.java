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
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.ServiceException;
import io.mifos.rhythm.api.v1.events.EventConstants;
import io.mifos.rhythm.service.internal.command.CreateApplicationCommand;
import io.mifos.rhythm.service.internal.command.DeleteApplicationCommand;
import io.mifos.rhythm.service.internal.mapper.ApplicationMapper;
import io.mifos.rhythm.service.internal.repository.ApplicationEntity;
import io.mifos.rhythm.service.internal.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class ApplicationCommandHandler {

  private final ApplicationRepository applicationRepository;

  @Autowired
  public ApplicationCommandHandler(final ApplicationRepository applicationRepository) {
    super();
    this.applicationRepository = applicationRepository;
  }

  @CommandHandler
  @Transactional
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.POST_APPLICATION)
  public String process(final CreateApplicationCommand createApplicationCommand) {

    final ApplicationEntity entity = ApplicationMapper.map(createApplicationCommand.getInstance());
    this.applicationRepository.save(entity);

    return createApplicationCommand.getInstance().getApplicationName();
  }

  @CommandHandler
  @Transactional
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.DELETE_APPLICATION)
  public String process(final DeleteApplicationCommand deleteApplicationCommand) {

    final Optional<ApplicationEntity> toDelete
            = this.applicationRepository.findByApplicationName(deleteApplicationCommand.getApplicationName());
    final ApplicationEntity toDeleteForReal
            = toDelete.orElseThrow(() -> ServiceException.notFound("Application with the name " + deleteApplicationCommand.getApplicationName() + " not found."));

    this.applicationRepository.delete(toDeleteForReal);

    return deleteApplicationCommand.getApplicationName();
  }
}
