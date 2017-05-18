-- noinspection SqlNoDataSourceInspectionForFile
--
-- Copyright 2017 The Mifos Initiative.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

# noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE khepri_beats (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_identifier        VARCHAR(64) NOT NULL,
  application_name         VARCHAR(64) NOT NULL,
  beat_identifier          VARCHAR(32) NOT NULL,
  alignment_hour           INT         NOT NULL,
  last_published_for      TIMESTAMP(3) NULL,
  CONSTRAINT khepri_beats_uq UNIQUE (tenant_identifier, application_name, beat_identifier),
  CONSTRAINT khepri_beats_pk PRIMARY KEY (id)
);
