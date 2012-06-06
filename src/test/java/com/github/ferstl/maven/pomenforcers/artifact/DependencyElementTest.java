/*
 * Copyright (c) 2012 by The Author(s)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ferstl.maven.pomenforcers.artifact;

import org.junit.Test;

import com.github.ferstl.maven.pomenforcers.artifact.DependencyElement;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class DependencyElementTest {

  @Test
  public void testGetByElementName() {
    DependencyElement.values();
    for (DependencyElement element : DependencyElement.values()) {
      assertThat(element, is(DependencyElement.getByElementName(element.getElementName())));
    }
  }

}