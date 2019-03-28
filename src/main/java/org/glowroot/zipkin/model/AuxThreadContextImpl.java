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
package org.glowroot.zipkin.model;

import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.glowroot.engine.bytecode.api.ThreadContextPlus;
import org.glowroot.engine.bytecode.api.ThreadContextThreadLocal;
import org.glowroot.engine.impl.NopTransactionService;
import org.glowroot.instrumentation.api.AuxThreadContext;
import org.glowroot.instrumentation.api.ThreadContext.ServletRequestInfo;
import org.glowroot.instrumentation.api.Timer;
import org.glowroot.instrumentation.api.TraceEntry;
import org.glowroot.zipkin.util.Global;

public class AuxThreadContextImpl implements AuxThreadContext {

    private final SpanContext parentSpanContext;
    private final @Nullable ServletRequestInfo servletRequestInfo;

    public AuxThreadContextImpl(SpanContext parentSpanContext,
            @Nullable ServletRequestInfo servletRequestInfo) {
        this.parentSpanContext = parentSpanContext;
        this.servletRequestInfo = servletRequestInfo;
    }

    @Override
    public TraceEntry start() {
        return start(false);
    }

    @Override
    public TraceEntry startAndMarkAsyncTransactionComplete() {
        return start(true);
    }

    private TraceEntry start(boolean completeAsyncTransaction) {
        ThreadContextThreadLocal.Holder threadContextHolder = Global.getThreadContextHolder();
        ThreadContextPlus threadContext = threadContextHolder.get();
        if (threadContext != null) {
            if (completeAsyncTransaction) {
                threadContext.setTransactionAsyncComplete();
            }
            return NopTransactionService.TRACE_ENTRY;
        }
        SpanContext spanContext = new SpanContext(parentSpanContext.getTraceIdHigh(),
                parentSpanContext.getTraceId(), parentSpanContext.getSpanId(), Global.nextId());
        threadContext =
                new ThreadContextImpl(threadContextHolder, spanContext, servletRequestInfo, 0, 0);
        threadContextHolder.set(threadContext);
        if (completeAsyncTransaction) {
            threadContext.setTransactionAsyncComplete();
        }
        return new AuxRootEntryImpl(threadContextHolder);
    }

    private static class AuxRootEntryImpl implements TraceEntry {

        private final ThreadContextThreadLocal.Holder threadContextHolder;

        private AuxRootEntryImpl(ThreadContextThreadLocal.Holder threadContextHolder) {
            this.threadContextHolder = threadContextHolder;
        }

        @Override
        public void end() {
            threadContextHolder.set(null);
        }

        @Override
        public void endWithLocationStackTrace(long threshold, TimeUnit unit) {
            threadContextHolder.set(null);
        }

        @Override
        public void endWithError(Throwable t) {
            threadContextHolder.set(null);
        }

        @Override
        public void endWithError(String message) {
            threadContextHolder.set(null);
        }

        @Override
        public void endWithError(String message, Throwable t) {
            threadContextHolder.set(null);
        }

        @Override
        public void endWithInfo(Throwable t) {
            threadContextHolder.set(null);
        }

        @Override
        public Timer extend() {
            return NopTransactionService.TIMER;
        }

        @Override
        public @Nullable Object getMessageSupplier() {
            return null;
        }
    }
}
