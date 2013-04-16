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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.annotation.Nonnull;

/**
 * 
 * Implementation of {@link ReaderWriterProcess}.
 * 
 * @author Kenneth Xu
 * 
 */
class ReaderWriterProcessImpl implements ReaderWriterProcess {
    public static final Factory FACTORY = new Factory() {
        @Override
        @Nonnull
        public ReaderWriterProcess getProcess(@Nonnull String... command) throws IOException {
            return new ReaderWriterProcessImpl(command);
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderWriterProcessImpl.class);
    private final Process process;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final BufferedReader reader;
    private final Writer writer;

    public ReaderWriterProcessImpl(@Nonnull String... command) throws IOException {
        if (command == null) throw new NullPointerException("command");
        process = new ProcessBuilder().command(command).redirectErrorStream(true).start();
        outputStream = process.getOutputStream();
        inputStream = process.getInputStream();
        writer = new OutputStreamWriter(outputStream);
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public Writer getWriter() {
        return writer;
    }

    @Override
    public BufferedReader getReader() {
        return reader;
    }

    @Override
    public void destroy() {
        try {
            quietlyClose(outputStream);
            quietlyClose(inputStream);
        } finally {
            process.destroy();
        }
    }

    private void quietlyClose(Closeable c) {
        try {
            c.close();
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    protected void finalize() throws Throwable {
        destroy();
    }
}
