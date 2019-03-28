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
import zipkin2.Span;

import org.glowroot.engine.impl.NopTransactionService;
import org.glowroot.engine.util.Throwables;
import org.glowroot.instrumentation.api.AsyncQueryEntry;
import org.glowroot.instrumentation.api.QueryMessageSupplier;
import org.glowroot.instrumentation.api.ThreadContext;
import org.glowroot.instrumentation.api.Timer;
import org.glowroot.instrumentation.api.internal.ReadableQueryMessage;
import org.glowroot.zipkin.util.Global;

class QueryEntryImpl implements AsyncQueryEntry {

    private final SpanContext spanContext;
    private final String queryText;
    private final QueryMessageSupplier queryMessageSupplier;
    private final long startTimeMicros;

    QueryEntryImpl(SpanContext context, String queryText,
            QueryMessageSupplier queryMessageSupplier) {
        this.spanContext = context;
        this.queryText = queryText;
        this.queryMessageSupplier = queryMessageSupplier;
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
    public @Nullable Object getMessageSupplier() {
        return null;
    }

    @Override
    public void rowNavigationAttempted() {}

    @Override
    public void incrementCurrRow() {}

    @Override
    public void setCurrRow(long row) {}

    @Override
    public void stopSyncTimer() {}

    @Override
    public Timer extendSyncTimer(ThreadContext currThreadContext) {
        return NopTransactionService.TIMER;
    }

    private void finish() {
        Global.report(newBuilder().build());
    }

    private void finish(Throwable t) {
        Global.report(newBuilder()
                .putTag("error", Throwables.getBestMessage(t))
                .build());
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
        ReadableQueryMessage message = (ReadableQueryMessage) queryMessageSupplier.get();
        return message.getPrefix() + queryText + message.getSuffix();
    }
}
