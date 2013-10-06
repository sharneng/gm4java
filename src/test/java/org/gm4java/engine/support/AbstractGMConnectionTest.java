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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.apache.commons.lang3.StringUtils;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMServiceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * Common test cases for implementation of {@link GMConnection}.
 * 
 * @author Kenneth Xu
 * 
 */
public abstract class AbstractGMConnectionTest {
    private static final String gmCommand = "convert something";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    protected ReaderWriterProcess process;
    @Mock
    protected Writer mockWriter;
    protected CharArrayWriter writer;
    @Mock
    protected BufferedReader reader;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        writer = new CharArrayWriter();
        when(process.getWriter()).thenReturn(writer);
        when(process.getReader()).thenReturn(reader);
    }

    protected abstract GMConnection sut() throws Exception;

    @Test
    public void execute_chokes_whenErrorSendingCommandToProcess() throws Exception {
        exception.expect(GMServiceException.class);
        when(process.getWriter()).thenReturn(mockWriter);
        doThrow(new IOException()).when(mockWriter).write(gmCommand);
        sut().execute(gmCommand);
    }

    @Test
    public void executeByList_chokes_whenErrorSendingCommandToProcess() throws Exception {
        exception.expect(GMServiceException.class);
        when(process.getWriter()).thenReturn(mockWriter);
        doThrow(new IOException()).when(mockWriter).write(gmCommand);
        sut().execute(Arrays.asList(gmCommand));
    }

    @Test
    public void execute_chokes_whenErrorReadingResultFromProcess() throws Exception {
        exception.expect(GMServiceException.class);
        doThrow(new IOException()).when(reader).readLine();
        sut().execute(gmCommand);
    }

    @Test
    public void executeByList_chokes_whenErrorReadingResultFromProcess() throws Exception {
        exception.expect(GMServiceException.class);
        doThrow(new IOException()).when(reader).readLine();
        sut().execute(Arrays.asList(gmCommand));
    }

    @Test
    public void execute_chokes_whenCommunicationInterrupts() throws Exception {
        exception.expect(GMServiceException.class);
        sut().execute(gmCommand);
    }

    @Test
    public void executeByList_chokes_whenCommunicationInterrupts() throws Exception {
        exception.expect(GMServiceException.class);
        sut().execute(Arrays.asList(gmCommand));
    }

    @Test
    public void execute_chokes_onCommunicationProblem() throws Exception {
        when(reader.readLine()).thenThrow(new IOException());
        exception.expect(GMServiceException.class);
        sut().execute(gmCommand);
    }

    @Test
    public void executeByList_chokes_onCommunicationProblem() throws Exception {
        when(reader.readLine()).thenThrow(new IOException());
        exception.expect(GMServiceException.class);
        sut().execute(Arrays.asList(gmCommand));
    }

    @Test
    public void execute_throwsIOException_whenGMreturnsIOError() throws Exception {
        final String line1 = "error line 1";
        final String line2 = "convert: Unable to open file (a.jpg) [No such file or directory].";
        when(reader.readLine()).thenReturn(line1, line2, "NG");

        exception.expect(IOException.class);
        exception.expectMessage(line1);
        exception.expectMessage(line2);

        sut().execute(gmCommand);
    }

    @Test
    public void execute_throwsGMException_whenGMreturnsError() throws Exception {
        final String line1 = "error line 1";
        final String line2 = "line 2";
        when(reader.readLine()).thenReturn(line1, line2, "NG");

        exception.expect(GMException.class);
        exception.expectMessage(line1);
        exception.expectMessage(line2);

        sut().execute(gmCommand);
    }

    @Test
    public void executeByList_throwsGMException_whenGMreturnsError() throws Exception {
        final String line1 = "error line 1";
        final String line2 = "line 2";
        when(reader.readLine()).thenReturn(line1, line2, "NG");

        exception.expect(GMException.class);
        exception.expectMessage(line1);
        exception.expectMessage(line2);

        sut().execute(Arrays.asList(gmCommand));
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void execute_chokes_onNullCommand() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("command");
        exception.expectMessage("null");
        sut().execute((String) null);
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void executeByList_chokes_onNullCommand() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("command");
        exception.expectMessage("null");
        sut().execute((List<String>) null);
    }

    @Test
    public void execute_chokes_onEmptyListCommand() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("command");
        exception.expectMessage("empty");
        sut().execute(Arrays.asList(new String[0]));
    }

    @Test
    public void execute_sendsCommandToProcess() throws Exception {
        when(reader.readLine()).thenReturn("OK");
        String result = sut().execute(gmCommand);
        assertThat(result, is(""));
        String s = writer.toString();
        assertThat(s, equalTo(gmCommand + TestUtils.EOL));
    }

    @Test
    public void executeByList_sendsCommandToProcess() throws Exception {
        when(reader.readLine()).thenReturn("OK");
        String result = sut().execute(Arrays.asList(gmCommand));
        assertThat(result, is(""));
        String s = writer.toString();
        assertThat(s, equalTo(" \"" + gmCommand + '"' + TestUtils.EOL));
    }

    @Test
    public void executeNullArguments_sendsCommandToProcess() throws Exception {
        when(reader.readLine()).thenReturn("OK");
        String result = sut().execute(gmCommand, (String[]) null);
        assertThat(result, is(""));
        String s = writer.toString();
        assertThat(s, equalTo(gmCommand + TestUtils.EOL));
    }

    @Test
    public void executeWithArguments_sendsArgumentsToProcess() throws Exception {
        when(reader.readLine()).thenReturn("OK");
        String result = sut().execute(gmCommand, "arg1", "arg2");
        assertThat(result, is(""));
        String s = writer.toString();
        assertThat(s, equalTo(gmCommand + " \"arg1\" \"arg2\"" + TestUtils.EOL));
    }

    @Test
    public void executeWithArguments_escapesQuoteInArgument() throws Exception {
        when(reader.readLine()).thenReturn("OK");
        String result = sut().execute(gmCommand, "arg with \"quotes\"");
        assertThat(result, is(""));
        String s = writer.toString();
        assertThat(s, equalTo(gmCommand + " \"arg with \"\"quotes\"\"\"" + TestUtils.EOL));
    }

    @Test
    public void execute_returns_whenGMReturns() throws Exception {
        final String line1 = "error line 1";
        final String line2 = "line 2";
        when(reader.readLine()).thenReturn(line1, line2, "OK");

        String result = sut().execute(gmCommand);

        assertThat(result, is(line1 + TestUtils.EOL + line2));
    }

    @Test
    public void execute_canHandle_largeGMResult() throws Exception {
        String large = StringUtils.repeat('a', 10000);
        when(reader.readLine()).thenReturn(large, "OK");

        String result = sut().execute(gmCommand);

        assertThat(result, is(large));
    }

    @Test
    public void execute_chokes_afterClose() throws Exception {
        when(reader.readLine()).thenReturn("OK");
        sut().close();
        exception.expect(GMServiceException.class);
        exception.expectMessage("closed");

        sut().execute(gmCommand);
    }

    @Test
    public void executeByList_chokes_afterClose() throws Exception {
        when(reader.readLine()).thenReturn("OK");
        sut().close();
        exception.expect(GMServiceException.class);
        exception.expectMessage("closed");

        sut().execute(Arrays.asList(gmCommand));
    }

    @Test
    public void close_destories_Process() throws Exception {

        sut().close();

        verify(process).destroy();
    }

    @Test
    public void close_canBeCalledMultipleTimes() throws Exception {
        sut().close();
        sut().close();
    }
}
