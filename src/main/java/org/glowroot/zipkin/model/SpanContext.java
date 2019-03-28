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

import org.checkerframework.checker.nullness.qual.Nullable;

import org.glowroot.zipkin.util.HexCodec;

// much copied from brave.propagation.TraceContext
public class SpanContext {

    private final long traceIdHigh;
    private final long traceId;
    private final long parentSpanId;
    private final long spanId;

    public SpanContext(long traceIdHigh, long traceId, long parentSpanId, long spanId) {
        this.traceIdHigh = traceIdHigh;
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
    }

    public long getTraceIdHigh() {
        return traceIdHigh;
    }

    public long getTraceId() {
        return traceId;
    }

    public long getParentSpanId() {
        return parentSpanId;
    }

    public long getSpanId() {
        return spanId;
    }

    volatile @Nullable String traceIdString; // Lazily initialized and cached.

    /** Returns the hex representation of the span's trace ID */
    public String traceIdString() {
        String r = traceIdString;
        if (r == null) {
            if (traceIdHigh != 0) {
                char[] result = new char[32];
                HexCodec.writeHexLong(result, 0, traceIdHigh);
                HexCodec.writeHexLong(result, 16, traceId);
                r = new String(result);
            } else {
                r = HexCodec.toLowerHex(traceId);
            }
            traceIdString = r;
        }
        return r;
    }

    volatile @Nullable String parentSpanIdString; // Lazily initialized and cached.

    /** Returns the hex representation of the span's parent ID */
    public @Nullable String parentSpanIdString() {
        String r = parentSpanIdString;
        if (r == null && parentSpanId != 0L) {
            r = parentSpanIdString = HexCodec.toLowerHex(parentSpanId);
        }
        return r;
    }

    volatile @Nullable String spanIdString; // Lazily initialized and cached.

    /** Returns the hex representation of the span's ID */
    public String spanIdString() {
        String r = spanIdString;
        if (r == null) {
            r = spanIdString = HexCodec.toLowerHex(spanId);
        }
        return r;
    }
}
