/*
 * Copyright (c) 2011 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gm4java.engine.support;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.IOException;
import java.util.Scanner;

import javax.annotation.Nonnull;

/**
 * Implementation of GMProcessFactory that knows how to deal with the version of GraphicsMagick you have installed.
 * 
 * It checks this with executing "gm version" once to determine the version and features of the installed
 * GraphicsMagick. Currently, it removes '-safe-mode' option when starting GraphicsMagick 1.3.22 and later versions.
 * 
 * @author Roy Sindre Norangshol
 * @author Kenneth Xu
 */
class GMProcessFactoryImpl implements GMProcessFactory {
    public static final GMProcessFactory.Builder BUILDER = new GMProcessFactory.Builder() {
        private final ReaderWriterProcess.Factory factory = ReaderWriterProcessImpl.FACTORY;

        @Override
        @Nonnull
        public GMProcessFactory buildFactory(final String gmPath) {
            return new GMProcessFactoryImpl(factory, gmPath);
        }
    };

    // @formatter:off
    private static final String[] GM_COMMAND_SAFE_MODE = {
        Constants.DEFAULT_GM_PATH, "batch",
        "-escape", "windows",
        "-feedback", "on",
        "-pass", Constants.GM_PASS,
        "-fail", Constants.GM_FAIL,
        "-prompt", "off",
        "-echo", "off",
        "-safe-mode", "on",
        "-"
    };
    private static final String[] GM_COMMAND = {
        Constants.DEFAULT_GM_PATH, "batch",
        "-escape", "windows",
        "-feedback", "on",
        "-pass", Constants.GM_PASS,
        "-fail", Constants.GM_FAIL,
        "-prompt", "off",
        "-echo", "off",
        "-"
    };
    // @formatter:on

    private static final DefaultArtifactVersion version_1_3_22 = new DefaultArtifactVersion("1.3.22");

    private final ReaderWriterProcess.Factory factory;
    private final String gmPath;
    private DefaultArtifactVersion version;
    private String[] gmCommand;

    GMProcessFactoryImpl(ReaderWriterProcess.Factory factory, String gmPath) {
        this.factory = factory;
        this.gmPath = gmPath;
    }

    @Override
    @Nonnull
    public DefaultArtifactVersion getVersion() {
        try {
            ensureFeatures();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return version;
    }

    @Override
    @Nonnull
    public String getGMPath() {
        return gmPath;
    }

    @Override
    @Nonnull
    public ReaderWriterProcess getProcess() throws IOException {
        ensureFeatures();
        return factory.getProcess(gmCommand);
    }

    static String[] getGMCommand(String gmPath) {
        String[] gmCommand = GM_COMMAND.clone();
        gmCommand[0] = gmPath;
        return gmCommand;
    }

    static String[] getGMCommandSafeMode(String gmPath) {
        String[] gmCommand = GM_COMMAND_SAFE_MODE.clone();
        gmCommand[0] = gmPath;
        return gmCommand;
    }

    private void ensureFeatures() throws IOException {
        if (gmCommand != null) return;
        ReaderWriterProcess proc = factory.getProcess(gmPath, "version");
        // Assuming version number is always in second word with delimiter ' '. $ gm version
        // GraphicsMagick 1.3.23 2015-11-07 Q16 http://www.GraphicsMagick.org/
        // Copyright (C) 2002-2015 GraphicsMagick Group.
        Scanner scanner = new Scanner(proc.getReader());
        if (!scanner.hasNextLine()) {
            throw new IOException(String.format("Could not detect your GraphicsMagick version, is '%s' in PATH?",
                    gmPath));
        }
        String[] firstLineInWords = scanner.nextLine().split(" ");
        version = new DefaultArtifactVersion(firstLineInWords[1]);
        this.gmCommand = version.compareTo(version_1_3_22) >= 0 ? getGMCommand(gmPath) : getGMCommandSafeMode(gmPath);
    }
}
