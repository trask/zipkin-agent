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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;

import org.glowroot.engine.impl.ImmutableTimerNameImpl;
import org.glowroot.instrumentation.api.MessageSupplier;
import org.glowroot.instrumentation.api.TimerName;
import org.glowroot.instrumentation.api.TraceEntry;
import org.glowroot.zipkin.util.Global;

public abstract class BaseTest {

    private static final TimerName DUMMY_TIMER_NAME = ImmutableTimerNameImpl.of("dummy", false);

    protected AgentImpl agent;
    protected MockReporter reporter;
    protected ExecutorService executor;

    @Before
    public void beforeEach() {
        agent = new AgentImpl();
        reporter = new MockReporter();
        Global.setReporter(reporter);
        executor = Executors.newCachedThreadPool();
    }

    @After
    public void afterEach() {
        executor.shutdown();
    }

    protected TraceEntry startTransaction(String transactionType, String transactionName,
            String message) {
        return agent.startTransaction(transactionType, transactionName,
                MessageSupplier.create(message), DUMMY_TIMER_NAME, Global.getThreadContextHolder(),
                0, 0);
    }

    protected TraceEntry startServiceCallEntry(String type, String text, String message) {
        return Global.getThreadContextHolder().get().startServiceCallEntry(type, text,
                MessageSupplier.create(message), DUMMY_TIMER_NAME);
    }
}
