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

import java.util.concurrent.TimeUnit;

import org.glowroot.xyzzy.engine.annotation.spi.XyzzyServiceSPI;

class XyzzyServiceImpl implements XyzzyServiceSPI {

    @Override
    public void setTransactionType(String transactionType) {}

    @Override
    public void setTransactionName(String transactionName) {}

    @Override
    public void setTransactionUser(String user) {}

    @Override
    public void addTransactionAttribute(String name, String value) {}

    @Override
    public void setTransactionSlowThreshold(long threshold, TimeUnit unit) {}
}
