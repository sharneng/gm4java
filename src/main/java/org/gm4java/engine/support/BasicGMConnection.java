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

import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * 
 * A implementation of {@link GMConnection} that serves as base of other implementation, and also used by
 * {@link SimpleGMService}.
 * 
 * @author Kenneth Xu
 * 
 */
class BasicGMConnection implements GMConnection {
    private static final List<String> empty = Arrays.asList(new String[0]);
    private static final int NORMAL_BUFFER_SIZE = 4096;
    private static final String EOL = System.getProperty("line.separator");
    private ReaderWriterProcess process;
    private final StringBuffer sb = new StringBuffer();

    public BasicGMConnection(@Nonnull ReaderWriterProcess process) throws GMServiceException {
        if (process == null) throw new NullPointerException("process");
        this.process = process;
    }

    @Override
    public final String execute(@Nonnull String command, @CheckForNull String... arguments) throws IOException,
            GMException, GMServiceException {
        if (command == null) throw new NullPointerException("Argument 'command' must not be null");
        return execute(command, arguments == null || arguments.length == 0 ? empty : Arrays.asList(arguments));
    }

    @Override
    public final String execute(@Nonnull List<String> command) throws IOException, GMException, GMServiceException {
        if (command == null) throw new NullPointerException("Argument 'command' must not be null");
        if (command.size() == 0) throw new IllegalArgumentException("Argument 'command' must not be empty");
        return execute(null, command);
    }

    @Override
    public void close() {
        if (process == null) return;
        process.destroy();
        process = null;
    }

    protected String execute(String command, @Nonnull List<String> arguments) throws IOException, GMException,
            GMServiceException {
        if (process == null) throw new GMServiceException("GMConnection is already closed.");
        sendCommand(command, arguments);
        return readResult();
    }

    private void sendCommand(String command, @Nonnull List<String> arguments) throws GMServiceException {
        Writer toGm = process.getWriter();
        try {
            if (command != null) toGm.write(command);
            for (String s : arguments) {
                final byte quote = '"';
                toGm.write(" ");
                toGm.write(quote);
                int start = 0, index = s.indexOf(quote);
                if (index < 0) {
                    toGm.write(s);
                } else {
                    do {
                        toGm.write(s, start, ++index - start);
                        toGm.write(quote);
                        start = index;
                        index = s.indexOf(quote, start);
                    } while (index >= 0);
                }
                toGm.write(quote);
            }
            toGm.write(EOL);
            toGm.flush();
        } catch (IOException e) {
            throw new GMServiceException(e.getMessage(), e);
        }
    }

    private String readResult() throws IOException, GMServiceException, GMException {
        String line;
        BufferedReader fromGm = process.getReader();
        sb.setLength(0);
        while ((line = readLine(fromGm)) != null) {
            if (line.equals(Constants.GM_PASS)) {
                return getGMOutput();
            }
            if (line.equals(Constants.GM_FAIL)) {
                final String output = getGMOutput();
                if (output.endsWith("].")) throw new IOException(output);
                else throw new GMException(output);
            }
            sb.append(line).append(EOL);
        }
        throw new GMServiceException("Input from GraphicsMagick was closed unexpectedly after receiving: "
                + getGMOutput());
    }

    private String getGMOutput() {
        sb.setLength(sb.length() - EOL.length());
        String output = sb.toString();
        if (sb.length() > NORMAL_BUFFER_SIZE) {
            sb.setLength(0);
            sb.trimToSize();
        }
        return output;
    }

    private String readLine(BufferedReader reader) throws GMServiceException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new GMServiceException(e.getMessage(), e);
        }
    }

}
