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

import zipkin2.Span;

import org.glowroot.engine.impl.NopTransactionService;
import org.glowroot.engine.util.Throwables;
import org.glowroot.instrumentation.api.AsyncTraceEntry;
import org.glowroot.instrumentation.api.MessageSupplier;
import org.glowroot.instrumentation.api.ThreadContext;
import org.glowroot.instrumentation.api.Timer;
import org.glowroot.instrumentation.api.internal.ReadableMessage;
import org.glowroot.zipkin.util.Global;

public class TraceEntryImpl implements AsyncTraceEntry {

    private final SpanContext spanContext;
    private final MessageSupplier messageSupplier;
    private final long startTimeMicros;

    protected TraceEntryImpl(SpanContext spanContext, MessageSupplier messageSupplier) {
        this.spanContext = spanContext;
        this.messageSupplier = messageSupplier;
        startTimeMicros = Global.currentTimeMicros();
    }

    @Override
    public void end() {
        finish();
    }

    @Override
    public void endWithLocationStackTrace(long threshold, TimeUnit unit) {
        finish();
    }

    @Override
    public void endWithError(Throwable t) {
        finish(t);
    }

    @Override
    public void endWithError(String message) {
        finish();
    }

    @Override
    public void endWithError(String message, Throwable t) {
        finish(t);
    }

    @Override
    public void endWithInfo(Throwable t) {
        finish();
    }

    @Override
    public Timer extend() {
        return NopTransactionService.TIMER;
    }

    @Override
    public Object getMessageSupplier() {
        return messageSupplier;
    }

    @Override
    public void stopSyncTimer() {}

    @Override
    public Timer extendSyncTimer(ThreadContext currThreadContext) {
        return NopTransactionService.TIMER;
    }

    protected void postFinish() {}

    private void finish() {
        Global.report(newBuilder().build());
        postFinish();
    }

    private void finish(Throwable t) {
        Global.report(newBuilder()
                .putTag("error", Throwables.getBestMessage(t))
                .build());
        postFinish();
    }

    private Span.Builder newBuilder() {
        long durationMicros = Global.currentTimeMicros() - startTimeMicros;
        return Span.newBuilder()
                .traceId(spanContext.traceIdString())
                .parentId(spanContext.parentSpanIdString())
                .id(spanContext.spanIdString())
                .name(getSpanName())
                .timestamp(startTimeMicros)
                .duration(Math.max(durationMicros, 1));
    }

    private String getSpanName() {
        return ((ReadableMessage) messageSupplier.get()).getText();
    }
}
