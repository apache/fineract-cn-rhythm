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
package org.apache.fineract.cn.rhythm;

import com.google.gson.Gson;
import org.apache.fineract.cn.rhythm.api.v1.domain.Beat;
import org.apache.fineract.cn.rhythm.api.v1.domain.ClockOffset;
import org.apache.fineract.cn.rhythm.api.v1.events.EventConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.apache.fineract.cn.lang.config.TenantHeaderFilter.TENANT_HEADER;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

public class RhythmApiDocumentation extends AbstractRhythmTest {
  @Rule
  public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/doc/generated-snippets/test-rhythm");

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @Before
  public void setUp ( ) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
  }

  @Test
  public void documentCreateBeat ( ) throws Exception {
    final String applicationIdentifier = "funnybusiness-v1";
    final String oldBeatId = "oldBeatIdentifier123";

    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    int alignmentHour = now.getHour();
    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, alignmentHour);

    Mockito.doAnswer(new Returns(true)).when(super.beatPublisherServiceMock).publishBeat(
            Matchers.eq(oldBeatId),
            Matchers.eq(tenantDataStoreContext.getTenantName()),
            Matchers.eq(applicationIdentifier),
            Matchers.eq(expectedBeatTimestamp));

    Beat newBeat = new Beat();
    newBeat.setIdentifier(oldBeatId);
    newBeat.setAlignmentHour(expectedBeatTimestamp.getHour());

    Gson gson = new Gson();
    this.mockMvc.perform(post("/applications/" + applicationIdentifier + "/beats")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header(TENANT_HEADER, tenantDataStoreContext.getTenantName())
            .content(gson.toJson(newBeat)))
            .andExpect(status().isAccepted())
            .andDo(document(
                    "document-create-beat", preprocessRequest(prettyPrint()),
                    requestFields(
                            fieldWithPath("identifier").description("Beat Identifier"),
                            fieldWithPath("alignmentHour").description("Beat Alignment Hour")
                    )
            ));
  }

  @Test
  public void documentGetBeat ( ) throws InterruptedException {
    final String applicationIdentifier = "funnybusiness-v2";
    final String oldBeatId = "oldBeatIdentifier789";

    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    int alignmentHour = now.getHour();
    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, alignmentHour);

    Mockito.doAnswer(new Returns(true)).when(super.beatPublisherServiceMock).publishBeat(
            Matchers.eq(oldBeatId),
            Matchers.eq(tenantDataStoreContext.getTenantName()),
            Matchers.eq(applicationIdentifier),
            Matchers.eq(expectedBeatTimestamp));

    final Beat createdBeat = createBeat(applicationIdentifier, oldBeatId, alignmentHour, expectedBeatTimestamp);

    try {
      this.mockMvc.perform(get("/applications/" + applicationIdentifier + "/beats/" + createdBeat.getIdentifier())
              .accept(MediaType.APPLICATION_JSON_VALUE)
              .contentType(MediaType.APPLICATION_JSON_VALUE)
              .header(TENANT_HEADER, tenantDataStoreContext.getTenantName()))
              .andExpect(status().isOk())
              .andDo(document(
                      "document-get-beat", preprocessRequest(prettyPrint()),
                      responseFields(
                              fieldWithPath("identifier").description("Beat Identifier"),
                              fieldWithPath("alignmentHour").description("Beat Alignment Hour")
                      )
              ));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void documentGetBeatsForApplication ( ) throws InterruptedException {
    final String applicationIdentifier = "interestingbusiness-v1";
    final String oldBeatId = "oldBeatIdentifier789";
    final String newBeatId = "newBeatIdentifier456";

    final LocalDateTime nowOne = LocalDateTime.now(ZoneId.of("UTC"));
    int alignmentHourOne = nowOne.getHour();
    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(nowOne, alignmentHourOne);

    final LocalDateTime nowTwo = LocalDateTime.now(ZoneOffset.ofHours(2));
    int alignmentHourTwo = nowTwo.getHour();
    final LocalDateTime beatTimestamp = getExpectedBeatTimestamp(nowTwo, alignmentHourTwo);

    Mockito.doAnswer(new Returns(true)).when(super.beatPublisherServiceMock).publishBeat(
            Matchers.eq(oldBeatId),
            Matchers.eq(tenantDataStoreContext.getTenantName()),
            Matchers.eq(applicationIdentifier),
            Matchers.eq(expectedBeatTimestamp));

    final Beat firstBeat = createBeat(applicationIdentifier, oldBeatId, alignmentHourOne, expectedBeatTimestamp);
    final Beat secondBeat = createBeat(applicationIdentifier, newBeatId, alignmentHourTwo, beatTimestamp);

    try {
      this.mockMvc.perform(get("/applications/" + applicationIdentifier + "/beats/")
              .accept(MediaType.APPLICATION_JSON_VALUE)
              .contentType(MediaType.APPLICATION_JSON_VALUE)
              .header(TENANT_HEADER, tenantDataStoreContext.getTenantName()))
              .andExpect(status().isOk())
              .andDo(document(
                      "document-get-beats", preprocessRequest(prettyPrint()),
                      responseFields(
                              fieldWithPath("[].identifier").description("First Beat's Identifier"),
                              fieldWithPath("[].alignmentHour").description("First Beat's Alignment Hour"),
                              fieldWithPath("[1].identifier").description("Second Beat's Identifier"),
                              fieldWithPath("[1].alignmentHour").description("Second Beat's Alignment Hour")
                      )
              ));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void documentDeleteBeat ( ) throws InterruptedException {
    final String applicationIdentifier = "interestingbusiness-v2";
    final String oldBeatId = "beatIdentifier789";

    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    int alignmentHour = now.getHour();
    final LocalDateTime expectedBeatTimestamp = getExpectedBeatTimestamp(now, alignmentHour);

    Mockito.doAnswer(new Returns(true)).when(super.beatPublisherServiceMock).publishBeat(
            Matchers.eq(oldBeatId),
            Matchers.eq(tenantDataStoreContext.getTenantName()),
            Matchers.eq(applicationIdentifier),
            Matchers.eq(expectedBeatTimestamp));

    final Beat createdBeat = createBeat(applicationIdentifier, oldBeatId, alignmentHour, expectedBeatTimestamp);

    final List <Beat> allEntities = super.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertTrue(allEntities.contains(createdBeat));

    try {
      this.mockMvc.perform(delete("/applications/" + applicationIdentifier + "/beats/" + createdBeat.getIdentifier())
              .accept(MediaType.ALL_VALUE)
              .contentType(MediaType.ALL_VALUE)
              .header(TENANT_HEADER, tenantDataStoreContext.getTenantName()))
              .andExpect(status().isAccepted())
              .andDo(document("document-delete-beat"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void documentDeleteApplication ( ) throws InterruptedException {
    final String applicationIdentifier = "goodbusiness-v1";
    super.createBeatForThisHour(applicationIdentifier, "beatDefender");

    List <Beat> allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertTrue(allEntities.size() == 1);

    try {
      this.mockMvc.perform(delete("/applications/" + applicationIdentifier)
              .accept(MediaType.ALL_VALUE)
              .contentType(MediaType.ALL_VALUE)
              .header(TENANT_HEADER, tenantDataStoreContext.getTenantName()))
              .andExpect(status().isAccepted())
              .andDo(document("document-delete-application"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.testSubject.deleteApplication(applicationIdentifier);
    Assert.assertTrue(this.eventRecorder.wait(EventConstants.DELETE_APPLICATION, applicationIdentifier));

    allEntities = this.testSubject.getAllBeatsForApplication(applicationIdentifier);
    Assert.assertTrue(allEntities.isEmpty());
  }

  @Test
  public void documentSetClockOffset ( ) throws InterruptedException {
    final LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(6));
    ClockOffset clockOffset = new ClockOffset(now.getHour(), now.getMinute(), now.getSecond());

    Gson gson = new Gson();
    try {
      this.mockMvc.perform(put("/clockoffset")
              .accept(MediaType.ALL_VALUE)
              .contentType(MediaType.APPLICATION_JSON_VALUE)
              .content(gson.toJson(clockOffset))
              .header(TENANT_HEADER, tenantDataStoreContext.getTenantName()))
              .andExpect(status().isAccepted())
              .andDo(document(
                      "document-set-clockoffset", preprocessRequest(prettyPrint()),
                      requestFields(
                              fieldWithPath("hours").type("Integer").description("Clock Offset Hour"),
                              fieldWithPath("minutes").type("Integer").description("Clock Offset Minutes"),
                              fieldWithPath("seconds").type("Integer").description("Clock Offset Seconds")
                      )
              ));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void documentGetClockOffset ( ) {
    try {
      this.mockMvc.perform(get("/clockoffset")
              .accept(MediaType.ALL_VALUE)
              .contentType(MediaType.APPLICATION_JSON_VALUE)
              .header(TENANT_HEADER, tenantDataStoreContext.getTenantName()))
              .andExpect(status().isOk())
              .andDo(document(
                      "document-get-clockoffset", preprocessResponse(prettyPrint()),
                      responseFields(
                              fieldWithPath("hours").description("Clock Offset Hour"),
                              fieldWithPath("minutes").description("Clock Offset Minutes"),
                              fieldWithPath("seconds").description("Clock Offset Seconds")
                      )
              ));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
