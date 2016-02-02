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
import java.nio.charset.Charset;
import java.util.Scanner;


/**
 * Command selector simple choose the
 * correct command according to which GraphicsMagick version you have installed.
 *
 * It checks this with executing "gm version" once.
 */
public class CommandSelector {
    private final Shell shell;
    private String gmPath;
    private Boolean isVersionEqualOrGreaterThan_1_3_22;

    public CommandSelector(String gmPath) {
        this(gmPath, new Shell());
    }

    protected CommandSelector(String gmPath, Shell shell) {
        this.gmPath = gmPath;
        this.shell = shell;
    }

    /**
     * Retrieve gm base command set correctly according to implementation
     *
     * As of version 1.3.22 , the keyword '-safe-mode' is removed from GraphicsMagick.
     * We cache the response from "gm version" to check which version you have installed.
     *
     * http://hg.graphicsmagick.org/hg/GraphicsMagick/diff/0c27524abb7d/magick/command.c#l1.328
     * https://github.com/sharneng/gm4java/issues/9
     *
     * @return graphics magick command
     */
    public String[] gmCommand() {
        String[] result = (isVersionEqualOrGratherThan_1_3_22(gmPath)? Constants.GM_COMMAND_V1_3_22.clone() : Constants.GM_COMMAND.clone());
        result[0] = gmPath;
        return result;
    }

    /**
     * Assuming verison number is always in second word with delimiter ' '.
     *  $ gm version
     *    GraphicsMagick 1.3.23 2015-11-07 Q16 http://www.GraphicsMagick.org/
     *    Copyright (C) 2002-2015 GraphicsMagick Group.
     *
     * Visible only for testing!
     *
     * @param gmPath gm binary
     */
    protected boolean isVersionEqualOrGratherThan_1_3_22(String gmPath) {
        if (isVersionEqualOrGreaterThan_1_3_22 != null) {
            return isVersionEqualOrGreaterThan_1_3_22;
        }
        try {
            Process proc = shell.exec(new String[]{gmPath, "version"});
            Scanner scanner = new Scanner(proc.getInputStream(), Charset.defaultCharset().name());
            if (!scanner.hasNextLine()) {
                throw new RuntimeException(String.format("Could not detect your GraphicsMagick version, is '%s' in PATH?", gmPath));
            }
            String firstLineInWords[] = scanner.nextLine().split(" ");

            DefaultArtifactVersion version = new DefaultArtifactVersion("1.3.22");

            isVersionEqualOrGreaterThan_1_3_22 =  (new DefaultArtifactVersion(firstLineInWords[1]).compareTo(version) >= 0);
            return isVersionEqualOrGreaterThan_1_3_22;
        } catch (IOException ioe) {
            throw new RuntimeException(String.format("Could not detect your GraphicsMagick version, is '%s' in PATH?", gmPath), ioe);
        }
    }

    /**
     * Set gmPath
     *
     * @param path gm executable
     * @return Chainable COmmandSlector
     */
    public CommandSelector setGmPath(String path) {
        this.gmPath = path;
        return this;
    }
}
