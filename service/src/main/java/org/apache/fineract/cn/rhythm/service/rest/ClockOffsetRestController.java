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
package org.apache.fineract.cn.rhythm.service.rest;

import static org.apache.fineract.cn.lang.config.TenantHeaderFilter.TENANT_HEADER;

import javax.validation.Valid;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.rhythm.api.v1.domain.ClockOffset;
import org.apache.fineract.cn.rhythm.service.internal.command.ChangeClockOffsetCommand;
import org.apache.fineract.cn.rhythm.service.internal.service.ClockOffsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Myrle Krantz
 */
@RestController
@RequestMapping("/clockoffset")
public class ClockOffsetRestController {
  private final CommandGateway commandGateway;
  private final ClockOffsetService clockOffsetService;

  @Autowired
  public ClockOffsetRestController(
      final CommandGateway commandGateway,
      final ClockOffsetService clockOffsetService) {
    super();
    this.commandGateway = commandGateway;
    this.clockOffsetService = clockOffsetService;
  }

  @Permittable(value = AcceptedTokenType.SYSTEM)
  @RequestMapping(
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<ClockOffset> getClockOffset(@RequestHeader(TENANT_HEADER) final String tenantIdentifier) {
    return ResponseEntity.ok(this.clockOffsetService.findByTenantIdentifier(tenantIdentifier));
  }

  @Permittable(value = AcceptedTokenType.SYSTEM)
  @RequestMapping(
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> setClockOffset(
      @RequestHeader(TENANT_HEADER) final String tenantIdentifier,
      @RequestBody @Valid final ClockOffset instance) throws InterruptedException {
    this.commandGateway.process(new ChangeClockOffsetCommand(tenantIdentifier, instance));
    return ResponseEntity.accepted().build();
  }
}
