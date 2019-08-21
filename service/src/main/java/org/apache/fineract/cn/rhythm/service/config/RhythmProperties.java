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
package org.apache.fineract.cn.rhythm.service.config;

import java.util.concurrent.TimeUnit;
import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @author Myrle Krantz
 */
@Component
@ConfigurationProperties(prefix="rhythm")
@Validated
public class RhythmProperties {
  @ValidIdentifier
  private String user;


  @Value("${rhythm.password}")
  private String password;

  @Range(min=0, max=3_600_000)
  private Long beatCheckRate = TimeUnit.MINUTES.toMillis(10);

  public RhythmProperties() {
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getUser() {
    return user;
  }

  public Long getBeatCheckRate() {
    return beatCheckRate;
  }

  public void setBeatCheckRate(Long beatCheckRate) {
    this.beatCheckRate = beatCheckRate;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
