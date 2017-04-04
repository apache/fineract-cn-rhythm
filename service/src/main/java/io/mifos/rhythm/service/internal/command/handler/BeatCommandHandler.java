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
import io.mifos.rhythm.api.v1.events.BeatEvent;
import io.mifos.rhythm.service.internal.command.CreateBeatCommand;
import io.mifos.rhythm.service.internal.command.DeleteBeatCommand;
import io.mifos.rhythm.service.internal.mapper.BeatMapper;
import io.mifos.rhythm.service.internal.repository.BeatEntity;
import io.mifos.rhythm.service.internal.repository.BeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class BeatCommandHandler {

  private final BeatRepository beatRepository;

  @Autowired
  public BeatCommandHandler(final BeatRepository beatRepository) {
    super();
    this.beatRepository = beatRepository;
  }

  @CommandHandler
  @Transactional
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.POST_BEAT)
  public BeatEvent process(final CreateBeatCommand createBeatCommand) {

    final BeatEntity entity = BeatMapper.map(createBeatCommand.getApplicationName(), createBeatCommand.getInstance());
    this.beatRepository.save(entity);

    return new BeatEvent(createBeatCommand.getApplicationName(), createBeatCommand.getInstance().getIdentifier());
  }

  @CommandHandler
  @Transactional
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.DELETE_BEAT)
  public BeatEvent process(final DeleteBeatCommand deleteBeatCommand) {

    final Optional<BeatEntity> toDelete
            = this.beatRepository.findByApplicationNameAndIdentifier(deleteBeatCommand.getApplicationName(), deleteBeatCommand.getIdentifier());
    final BeatEntity toDeleteForReal
            = toDelete.orElseThrow(() -> ServiceException.notFound(
                    "Beat with for the application " + deleteBeatCommand.getApplicationName() +
                            ", and the identifier " + deleteBeatCommand.getApplicationName() + " not found."));

    this.beatRepository.delete(toDeleteForReal);

    return new BeatEvent(deleteBeatCommand.getApplicationName(), deleteBeatCommand.getIdentifier());
  }
}
