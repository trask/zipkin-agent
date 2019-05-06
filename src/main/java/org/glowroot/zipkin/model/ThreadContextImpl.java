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

import org.glowroot.xyzzy.engine.bytecode.api.ThreadContextPlus;
import org.glowroot.xyzzy.engine.bytecode.api.ThreadContextThreadLocal;
import org.glowroot.xyzzy.engine.impl.NopTransactionService;
import org.glowroot.xyzzy.instrumentation.api.AsyncQuerySpan;
import org.glowroot.xyzzy.instrumentation.api.AsyncSpan;
import org.glowroot.xyzzy.instrumentation.api.AuxThreadContext;
import org.glowroot.xyzzy.instrumentation.api.MessageSupplier;
import org.glowroot.xyzzy.instrumentation.api.QueryMessageSupplier;
import org.glowroot.xyzzy.instrumentation.api.QuerySpan;
import org.glowroot.xyzzy.instrumentation.api.Span;
import org.glowroot.xyzzy.instrumentation.api.Timer;
import org.glowroot.xyzzy.instrumentation.api.TimerName;

public class ThreadContextImpl implements ThreadContextPlus {

    private final ThreadContextThreadLocal.Holder threadContextHolder;

    private final SpanContext spanContext;

    private @Nullable ServletRequestInfo servletRequestInfo;

    private int currentNestingGroupId;
    private int currentSuppressionKeyId;

    public ThreadContextImpl(ThreadContextThreadLocal.Holder threadContextHolder,
            SpanContext context, @Nullable ServletRequestInfo servletRequestInfo,
            int rootNestingGroupId, int rootSuppressionKeyId) {
        this.threadContextHolder = threadContextHolder;
        this.spanContext = context;
        this.servletRequestInfo = servletRequestInfo;
        currentNestingGroupId = rootNestingGroupId;
        currentSuppressionKeyId = rootSuppressionKeyId;
    }

    ThreadContextThreadLocal.Holder getThreadContextHolder() {
        return threadContextHolder;
    }

    @Override
    public boolean isInTransaction() {
        return true;
    }

    @Override
    public Span startIncomingSpan(String transactionType, String transactionName,
            MessageSupplier messageSupplier, TimerName timerName) {
        return NopTransactionService.LOCAL_SPAN;
    }

    @Override
    public Span startIncomingSpan(String transactionType, String transactionName,
            MessageSupplier messageSupplier, TimerName timerName,
            AlreadyInTransactionBehavior alreadyInTransactionBehavior) {
        return NopTransactionService.LOCAL_SPAN;
    }

    @Override
    public Span startLocalSpan(MessageSupplier messageSupplier, TimerName timerName) {
        return NopTransactionService.LOCAL_SPAN;
    }

    @Override
    public QuerySpan startQuerySpan(String queryType, String queryText,
            QueryMessageSupplier queryMessageSupplier, TimerName timerName) {
        return new QuerySpanImpl(spanContext, queryType, queryText, queryMessageSupplier);
    }

    @Override
    public QuerySpan startQuerySpan(String queryType, String queryText, long queryExecutionCount,
            QueryMessageSupplier queryMessageSupplier, TimerName timerName) {
        // TODO pass along queryExecutionCount
        return new QuerySpanImpl(spanContext, queryType, queryText, queryMessageSupplier);
    }

    @Override
    public AsyncQuerySpan startAsyncQuerySpan(String queryType, String queryText,
            QueryMessageSupplier queryMessageSupplier, TimerName timerName) {
        return new QuerySpanImpl(spanContext, queryType, queryText, queryMessageSupplier);
    }

    @Override
    public Span startOutgoingSpan(String type, String text,
            MessageSupplier messageSupplier, TimerName timerName) {
        return new SpanImpl(spanContext, type, messageSupplier);
    }

    @Override
    public AsyncSpan startAsyncOutgoingSpan(String type, String text,
            MessageSupplier messageSupplier, TimerName timerName) {
        return new SpanImpl(spanContext, type, messageSupplier);
    }

    @Override
    public Timer startTimer(TimerName timerName) {
        return NopTransactionService.TIMER;
    }

    @Override
    public AuxThreadContext createAuxThreadContext() {
        return new AuxThreadContextImpl(spanContext, servletRequestInfo);
    }

    @Override
    public void setTransactionAsync() {}

    @Override
    public void setTransactionAsyncComplete() {}

    @Override
    public void setTransactionType(String transactionType, int priority) {}

    @Override
    public void setTransactionName(String transactionName, int priority) {}

    @Override
    public void setTransactionUser(String user, int priority) {}

    @Override
    public void addTransactionAttribute(String name, String value) {}

    @Override
    public void setTransactionSlowThreshold(long threshold, TimeUnit unit, int priority) {}

    @Override
    public void setTransactionError(Throwable t) {}

    @Override
    public void setTransactionError(String message) {}

    @Override
    public void setTransactionError(String message, Throwable t) {}

    @Override
    public void addErrorEntry(Throwable t) {}

    @Override
    public void addErrorEntry(String message) {}

    @Override
    public void addErrorEntry(String message, Throwable t) {}

    @Override
    public void trackResourceAcquired(Object resource, boolean withLocationStackTrace) {}

    @Override
    public void trackResourceReleased(Object resource) {}

    @Override
    public @Nullable ServletRequestInfo getServletRequestInfo() {
        return servletRequestInfo;
    }

    @Override
    public void setServletRequestInfo(@Nullable ServletRequestInfo servletRequestInfo) {
        this.servletRequestInfo = servletRequestInfo;
    }

    @Override
    public int getCurrentNestingGroupId() {
        return currentNestingGroupId;
    }

    @Override
    public void setCurrentNestingGroupId(int nestingGroupId) {
        this.currentNestingGroupId = nestingGroupId;
    }

    @Override
    public int getCurrentSuppressionKeyId() {
        return currentSuppressionKeyId;
    }

    @Override
    public void setCurrentSuppressionKeyId(int suppressionKeyId) {
        this.currentSuppressionKeyId = suppressionKeyId;
    }
}
