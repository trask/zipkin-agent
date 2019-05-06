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

import java.io.File;
import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import org.glowroot.xyzzy.engine.init.EngineModule;
import org.glowroot.xyzzy.engine.init.MainEntryPointUtil;
import org.glowroot.zipkin.util.Global;

public class MainEntryPoint {

    private static boolean USE_CONSOLE_REPORTER = true;

    private MainEntryPoint() {}

    public static void premain(Instrumentation instrumentation, File agentJarFile) {
        // DO NOT USE ANY GUAVA CLASSES before initLogging() because they trigger loading of jul
        // (and thus org.glowroot.xyzzy.engine.jul.Logger and thus glowroot's shaded slf4j)
        Logger startupLogger;
        try {
            startupLogger = MainEntryPointUtil.initLogging("org.glowroot.zipkin", instrumentation);
        } catch (Throwable t) {
            // log error but don't re-throw which would prevent monitored app from starting
            // also, don't use logger since it failed to initialize
            System.err.println("Agent failed to start: " + t.getMessage());
            t.printStackTrace();
            return;
        }
        try {
            start(instrumentation, agentJarFile);
        } catch (Throwable t) {
            // log error but don't re-throw which would prevent monitored app from starting
            startupLogger.error("Agent failed to start: {}", t.getMessage(), t);
        }
    }

    private static void start(Instrumentation instrumentation, File agentJarFile) throws Exception {

        // FIXME simple way to avoid conflict when multiple java processes being monitored
        File tmpDir = new File(agentJarFile.getParentFile(), "tmp");

        AgentImpl agent = new AgentImpl();

        EngineModule.createWithSomeDefaults(instrumentation, tmpDir,
                Global.getThreadContextThreadLocal(), new XyzzyServiceImpl(), agent,
                agentJarFile);

        if (USE_CONSOLE_REPORTER) {
            Global.setReporter(Reporter.CONSOLE);
        } else {
            Sender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v2/spans");
            Global.setReporter(AsyncReporter.create(sender));
        }
    }
}
