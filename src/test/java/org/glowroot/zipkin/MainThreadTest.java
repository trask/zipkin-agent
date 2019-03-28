/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glowroot.zipkin;

import org.junit.Test;

import org.glowroot.instrumentation.api.TraceEntry;

import static org.assertj.core.api.Assertions.assertThat;

// the tests simulate the method calls that the engine makes, and then validate that the reporter
// collected the expected spans
public class MainThreadTest extends BaseTest {

    @Test
    public void testWithNoOutgoingSpans() {
        // when
        TraceEntry traceEntry = startTransaction("A", "B", "C");
        traceEntry.end();

        // then
        assertThat(reporter.getSpans()).hasSize(1);
    }

    @Test
    public void testWithOneOutgoingSpan() {
        // when
        TraceEntry traceEntry = startTransaction("A", "B", "C");

        TraceEntry serviceCallEntry = startServiceCallEntry("X", "Y", "Z");
        serviceCallEntry.end();

        traceEntry.end();

        // then
        assertThat(reporter.getSpans()).hasSize(2);
    }

    @Test
    public void testWithMultipleOutgoingSpans() {
        // when
        TraceEntry traceEntry = startTransaction("A", "B", "C");

        for (int i = 0; i < 10; i++) {
            TraceEntry serviceCallEntry = startServiceCallEntry("X", "Y" + i, "Z" + i);
            serviceCallEntry.end();
        }

        traceEntry.end();

        // then
        assertThat(reporter.getSpans()).hasSize(11);
    }
}
