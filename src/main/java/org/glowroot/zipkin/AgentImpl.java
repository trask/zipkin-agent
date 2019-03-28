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

import org.glowroot.engine.bytecode.api.ThreadContextThreadLocal;
import org.glowroot.engine.weaving.AgentSPI;
import org.glowroot.instrumentation.api.MessageSupplier;
import org.glowroot.instrumentation.api.TimerName;
import org.glowroot.instrumentation.api.TraceEntry;
import org.glowroot.zipkin.model.SpanContext;
import org.glowroot.zipkin.model.ThreadContextImpl;
import org.glowroot.zipkin.model.TraceEntryImpl;
import org.glowroot.zipkin.util.Global;

class AgentImpl implements AgentSPI {

    AgentImpl() {}

    // in addition to returning TraceEntry, this method needs to put the newly created
    // ThreadContextPlus into the threadContextHolder that is passed in
    @Override
    public TraceEntry startTransaction(String transactionType, String transactionName,
            MessageSupplier messageSupplier, TimerName timerName,
            ThreadContextThreadLocal.Holder threadContextHolder, int rootNestingGroupId,
            int rootSuppressionKeyId) {

        long id = Global.nextId();
        SpanContext spanContext = new SpanContext(0, id, id, id);
        ThreadContextImpl threadContext = new ThreadContextImpl(threadContextHolder, spanContext,
                null, rootNestingGroupId, rootSuppressionKeyId);
        threadContextHolder.set(threadContext);

        return new RootTraceEntryImpl(spanContext, messageSupplier, threadContextHolder);
    }

    private static class RootTraceEntryImpl extends TraceEntryImpl {

        private final ThreadContextThreadLocal.Holder threadContextHolder;

        public RootTraceEntryImpl(SpanContext spanContext, MessageSupplier messageSupplier,
                ThreadContextThreadLocal.Holder threadContextHolder) {
            super(spanContext, messageSupplier);
            this.threadContextHolder = threadContextHolder;
        }

        @Override
        protected void postFinish() {
            threadContextHolder.set(null);
        }
    }
}
