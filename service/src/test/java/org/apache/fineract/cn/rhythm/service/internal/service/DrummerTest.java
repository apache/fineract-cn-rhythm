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
package org.apache.fineract.cn.rhythm.service.internal.service;

import org.apache.fineract.cn.rhythm.api.v1.domain.ClockOffset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Myrle Krantz
 */
@RunWith(Parameterized.class)
public class DrummerTest {
  static class TestCase {
    final String description;
    LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
    LocalDateTime nextBeat = now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
    int alignmentHour = 0;
    ClockOffset clockOffset = new ClockOffset();
    LocalDateTime expectedIncrementedBeat = nextBeat.plusDays(1);
    LocalDateTime expectedNextBeatAfterPublish = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
    int expectedBeatPublishCount = 2;

    TestCase(final String description) {
      this.description = description;
    }

    TestCase now(final LocalDateTime now) {
      this.now = now;
      return this;
    }

    TestCase nextBeat(final LocalDateTime newVal) {
      this.nextBeat = newVal;
      return this;
    }

    TestCase alignmentHour(final int newVal) {
      this.alignmentHour = newVal;
      return this;
    }

    TestCase clockOffset(final ClockOffset newVal) {
      this.clockOffset = newVal;
      return this;
    }

    TestCase expectedIncrementedBeat(final LocalDateTime newVal) {
      this.expectedIncrementedBeat = newVal;
      return this;
    }

    TestCase expectedNextBeatAfterPublish(final LocalDateTime newVal) {
      this.expectedNextBeatAfterPublish = newVal;
      return this;
    }

    TestCase expectedBeatPublishCount(final int newVal) {
      this.expectedBeatPublishCount = newVal;
      return this;
    }
  }

  @Parameterized.Parameters
  public static Collection testCases() {
    final Collection<TestCase> ret = new ArrayList<>();
    final TestCase basicCase = new TestCase("basicCase");
    ret.add(basicCase);
    ret.add(new TestCase("3daysBack")
        .nextBeat(basicCase.now.minusDays(3))
        .expectedIncrementedBeat(basicCase.now.minusDays(2).truncatedTo(ChronoUnit.DAYS))
        .expectedBeatPublishCount(4)); //Four because "now" gets published too.
    ret.add(new TestCase("inFuture")
        .nextBeat(basicCase.now.plusDays(1))
        .expectedIncrementedBeat(basicCase.now.plusDays(2).truncatedTo(ChronoUnit.DAYS))
        .expectedNextBeatAfterPublish(basicCase.now.plusDays(1))
        .expectedBeatPublishCount(0));
    ret.add(new TestCase("nonZeroClockOffset")
        .now(LocalDateTime.of(2017, 12, 18, 15, 5, 2))
        .nextBeat(LocalDateTime.of(2017, 12, 17, 15, 0, 0))
        .clockOffset(new ClockOffset(15, 5, 2))
        .expectedIncrementedBeat(LocalDateTime.of(2017, 12, 18, 15, 5, 2))
        .expectedNextBeatAfterPublish(LocalDateTime.of(2017, 12, 19, 15, 5, 2))
        .expectedBeatPublishCount(2));
    ret.add(new TestCase("nonZeroAlignmentHour")
        .now(LocalDateTime.of(2017, 12, 18, 15, 5, 2))
        .nextBeat(LocalDateTime.of(2017, 12, 17, 15, 0, 0))
        .alignmentHour(4)
        .expectedIncrementedBeat(LocalDateTime.of(2017, 12, 18, 4, 0, 0))
        .expectedNextBeatAfterPublish(LocalDateTime.of(2017, 12, 19, 4, 0, 0))
        .expectedBeatPublishCount(2));
    ret.add(new TestCase("clockOffsetAndAlignmentHour")
        .now(LocalDateTime.of(2017, 12, 18, 15, 5, 2))
        .nextBeat(LocalDateTime.of(2017, 12, 17, 15, 0, 0))
        .alignmentHour(5)
        .clockOffset(new ClockOffset(15, 5, 2))
        .expectedIncrementedBeat(LocalDateTime.of(2017, 12, 18, 20, 5, 2))
        .expectedNextBeatAfterPublish(LocalDateTime.of(2017, 12, 18, 20, 5, 2))
        .expectedBeatPublishCount(1));
    return ret;
  }

  private TestCase testCase;

  public DrummerTest(final TestCase testCase) {
    this.testCase = testCase;
  }

  @Test
  public void incrementToAlignment() {
    final LocalDateTime incrementedBeat = Drummer.incrementToAlignment(
        testCase.nextBeat,
        testCase.alignmentHour,
        testCase.clockOffset);

    Assert.assertEquals(
        "expectedIncrementedBeat",
        testCase.expectedIncrementedBeat,
        incrementedBeat);
  }

  @Test
  public void checkBeatForPublishHelper()
  {
    final Set<LocalDateTime> calledForTimes = new HashSet<>();
    final LocalDateTime nextBeatAfterPublish = Drummer.checkBeatForPublishHelper(
        testCase.now,
        testCase.alignmentHour,
        testCase.nextBeat,
        testCase.clockOffset,
        x -> {
          calledForTimes.add(x);
          return true;
        });
    Assert.assertEquals(
        "expectedNextBeatAfterPublish",
        testCase.expectedNextBeatAfterPublish,
        nextBeatAfterPublish);
    Assert.assertEquals(
        "expectedBeatPublishCount",
        testCase.expectedBeatPublishCount,
        calledForTimes.size());

  }

  @Test
  public void checkBeatForPublishHelperFirstFails() {
    @SuppressWarnings("unchecked")
    final Predicate<LocalDateTime> produceBeatsMock = Mockito.mock(Predicate.class);
    Mockito.when(produceBeatsMock.test(testCase.nextBeat)).thenReturn(false);
    final LocalDateTime nextBeatAfterPublish = Drummer.checkBeatForPublishHelper(
        testCase.now,
        testCase.alignmentHour,
        testCase.nextBeat,
        testCase.clockOffset,
        produceBeatsMock);
    Assert.assertEquals("nextBeat", testCase.nextBeat, nextBeatAfterPublish);
  }

  @Test
  public void checkBeatForPublishSecondFails() {
    if (testCase.expectedBeatPublishCount < 2)
      return;

    final LocalDateTime secondBeat = Drummer.incrementToAlignment(
        testCase.nextBeat,
        testCase.alignmentHour,
        testCase.clockOffset);
    @SuppressWarnings("unchecked") final Predicate<LocalDateTime> produceBeatsMock = Mockito.mock(Predicate.class);
    Mockito.when(produceBeatsMock.test(testCase.nextBeat)).thenReturn(true);
    Mockito.when(produceBeatsMock.test(secondBeat)).thenReturn(false);
    final LocalDateTime nextBeatAfterPublish = Drummer.checkBeatForPublishHelper(
        testCase.now,
        testCase.alignmentHour,
        testCase.nextBeat,
        testCase.clockOffset,
        produceBeatsMock);
    Assert.assertEquals(secondBeat, nextBeatAfterPublish);
  }
}