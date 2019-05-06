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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.nullness.qual.Nullable;
import zipkin2.Span;

import org.glowroot.xyzzy.engine.impl.NopTransactionService;
import org.glowroot.xyzzy.engine.util.Throwables;
import org.glowroot.xyzzy.instrumentation.api.AsyncQuerySpan;
import org.glowroot.xyzzy.instrumentation.api.QueryMessageSupplier;
import org.glowroot.xyzzy.instrumentation.api.Timer;
import org.glowroot.zipkin.util.Global;

class QuerySpanImpl implements AsyncQuerySpan {

    private final SpanContext spanContext;
    private final String queryType;
    private final String queryText;
    private final QueryMessageSupplier queryMessageSupplier;
    private final long startTimeMicros;

    QuerySpanImpl(SpanContext context, String queryType, String queryText,
            QueryMessageSupplier queryMessageSupplier) {
        this.spanContext = context;
        this.queryType = queryType;
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
    public Timer extendSyncTimer() {
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
        Span.Builder builder = Span.newBuilder()
                .traceId(spanContext.traceIdString())
                .parentId(spanContext.parentSpanIdString())
                .id(spanContext.spanIdString())
                .name(queryText)
                .putTag("type", queryType);
        Map<String, ?> detail = queryMessageSupplier.get();
        for (Map.Entry<String, ?> entry : detail.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                // TODO do tags accept null values?
                builder.putTag(entry.getKey(), value.toString());
            }
        }
        return builder.timestamp(startTimeMicros)
                .duration(Math.max(durationMicros, 1));
    }
}
