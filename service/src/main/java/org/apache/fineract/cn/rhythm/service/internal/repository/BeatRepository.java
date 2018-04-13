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
package org.apache.fineract.cn.rhythm.service.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@Repository
public interface BeatRepository extends JpaRepository<BeatEntity, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  void deleteByTenantIdentifierAndApplicationIdentifier
      (String tenantIdentifier, String applicationIdentifier);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  void deleteByTenantIdentifierAndApplicationIdentifierAndBeatIdentifier
      (String tenantIdentifier,
       String applicationIdentifier,
       String beatIdentifier);
  List<BeatEntity> findByTenantIdentifierAndApplicationIdentifier
          (String tenantIdentifier, String applicationIdentifier);
  Optional<BeatEntity> findByTenantIdentifierAndApplicationIdentifierAndBeatIdentifier
          (String tenantIdentifier, String applicationIdentifier, String beatIdentifier);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Stream<BeatEntity> findByNextBeatBefore(LocalDateTime currentTime);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Stream<BeatEntity> findByTenantIdentifier(String tenantIdentifier);
}
