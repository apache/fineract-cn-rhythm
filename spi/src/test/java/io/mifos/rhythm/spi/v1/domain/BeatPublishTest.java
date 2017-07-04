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
package io.mifos.rhythm.spi.v1.domain;

import io.mifos.core.lang.DateConverter;
import io.mifos.core.test.domain.ValidationTest;
import io.mifos.core.test.domain.ValidationTestCase;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.runners.Parameterized;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Myrle Krantz
 */
public class BeatPublishTest extends ValidationTest<BeatPublish> {

  public BeatPublishTest(final ValidationTestCase<BeatPublish> testCase) {
    super(testCase);
  }

  @Override
  protected BeatPublish createValidTestSubject() {
    return new BeatPublish("identifier", DateConverter.toIsoString(LocalDateTime.now()));
  }

  @Parameterized.Parameters
  public static Collection testCases() {
    final Collection<ValidationTestCase> ret = new ArrayList<>();
    ret.add(new ValidationTestCase<BeatPublish>("basicCase")
        .adjustment(x -> {})
        .valid(true));
    ret.add(new ValidationTestCase<BeatPublish>("null time")
        .adjustment(x -> x.setForTime(null))
        .valid(false));
    ret.add(new ValidationTestCase<BeatPublish>("non time")
        .adjustment(x -> x.setForTime("this is not a time"))
        .valid(false));
    ret.add(new ValidationTestCase<BeatPublish>("null identifier")
        .adjustment(x -> x.setIdentifier(null))
        .valid(false));
    ret.add(new ValidationTestCase<BeatPublish>("too long identifier")
        .adjustment(x -> x.setIdentifier(RandomStringUtils.random(33)))
        .valid(false));
    return ret;
  }
}
