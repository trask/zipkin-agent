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

import java.util.concurrent.Future;

import org.junit.Test;

import org.glowroot.engine.bytecode.api.ThreadContextPlus;
import org.glowroot.instrumentation.api.AuxThreadContext;
import org.glowroot.instrumentation.api.TraceEntry;
import org.glowroot.zipkin.util.Global;

import static org.assertj.core.api.Assertions.assertThat;

// the tests simulate the method calls that the engine makes, and then validate that the reporter
// collected the expected spans
public class AuxThreadTest extends BaseTest {

    @Test
    public void testWithOneAuxThread() throws Exception {
        // when
        TraceEntry traceEntry = startTransaction("A", "B", "C");

        ThreadContextPlus threadContext = Global.getThreadContextHolder().get();
        final AuxThreadContext auxThreadContext = threadContext.createAuxThreadContext();

        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                TraceEntry auxTraceEntry = auxThreadContext.start();
                TraceEntry serviceCallEntry = startServiceCallEntry("X", "Y", "Z");
                serviceCallEntry.end();
                auxTraceEntry.end();
            }
        });
        future.get();

        traceEntry.end();

        // then
        assertThat(reporter.getSpans()).hasSize(2);
    }
}
