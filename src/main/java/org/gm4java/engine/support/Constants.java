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

/**
 * 
 * Class to hold commonly used constants.
 * 
 * @author Kenneth Xu
 * 
 */
final class Constants {

    private Constants() {
    }

    /**
     * Default path to GraphcisMagick executable.
     */
    static final String DEFAULT_GM_PATH = "gm";
    static final String GM_PASS = "OK";
    static final String GM_FAIL = "NG";

    // @formatter:off
    static final String[] GM_COMMAND = {
        DEFAULT_GM_PATH, "batch",
        "-escape", "windows",
        "-feedback", "on",
        "-pass", GM_PASS,
        "-fail", GM_FAIL,
        "-prompt", "off",
        "-echo", "off",
        "-"
    };
    // @formatter:on

    static String[] gmCommand(String gmPath) {
        String[] result = GM_COMMAND.clone();
        result[0] = gmPath;
        return result;
    }
}
