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

import javax.sql.DataSource;
import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.CommandLogLevel;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.apache.fineract.cn.postgresql.domain.FlywayFactoryBean;
import org.apache.fineract.cn.rhythm.api.v1.events.EventConstants;
import org.apache.fineract.cn.rhythm.service.internal.command.InitializeServiceCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@Aggregate
public class InitializeCommandHandler {

  private final DataSource dataSource;
  private final FlywayFactoryBean flywayFactoryBean;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  public InitializeCommandHandler(final DataSource dataSource,
                                  final FlywayFactoryBean flywayFactoryBean) {
    super();
    this.dataSource = dataSource;
    this.flywayFactoryBean = flywayFactoryBean;
  }

  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @Transactional
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.INITIALIZE)
  public String initialize(final InitializeServiceCommand initializeServiceCommand) {
    this.flywayFactoryBean.create(this.dataSource).migrate();
    return EventConstants.INITIALIZE;
  }
}
