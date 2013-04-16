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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

/**
 * An abstraction for a connected process with reader and writer.
 * <p>
 * This is to isolate parts that are hard to test.
 * 
 * @author Ken
 * 
 */
interface ReaderWriterProcess {
    /**
     * A factory interface for better test isolation.
     */
    interface Factory {
        // The factory method is thread safe.
        @Nonnull
        ReaderWriterProcess getProcess(@Nonnull String... command) throws IOException;
    }

    @Nonnull
    Writer getWriter();

    @Nonnull
    BufferedReader getReader();

    void destroy();
}
