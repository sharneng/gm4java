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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Test cases for {@link ReaderWriterProcessImpl}.
 * 
 * @author Kenneth Xu
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessBuilder.class, ReaderWriterProcessImpl.class, LoggerFactory.class })
public class ReaderWriterProcessImplTest {
    private static final String text = "some string";
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    protected OutputStream outputStream;
    @Mock
    protected InputStream inputStream;
    private ProcessBuilder builder;
    @Mock
    private Process process;

    private ReaderWriterProcess sut;

    private static Logger logger;

    @BeforeClass
    public static void init() throws Exception {
        PowerMockito.mockStatic(LoggerFactory.class);
        logger = mock(Logger.class);
        when(LoggerFactory.getLogger(ReaderWriterProcessImpl.class)).thenReturn(logger);
    }

    @Before
    public void setup() throws Exception {
        reset(logger);
        MockitoAnnotations.initMocks(this);
        builder = PowerMockito.mock(ProcessBuilder.class);
        when(builder.command(Matchers.<String[]> anyObject())).thenReturn(builder);
        when(builder.redirectErrorStream(true)).thenReturn(builder);
        when(builder.start()).thenReturn(process);
        PowerMockito.whenNew(ProcessBuilder.class).withNoArguments().thenReturn(builder);
    }

    private InputStream mockInput() throws Exception {
        doCallRealMethod().when(inputStream).read(Matchers.<byte[]> any(), anyInt(), anyInt());
        doCallRealMethod().when(inputStream).read(Matchers.<byte[]> any());
        return inputStream;
    }

    private OutputStream mockOutput() throws Exception {
        doCallRealMethod().when(outputStream).write(Matchers.<byte[]> any(), anyInt(), anyInt());
        doCallRealMethod().when(outputStream).write(Matchers.<byte[]> any());
        return outputStream;
    }

    private ReaderWriterProcess sut() throws Exception {
        return sut(mockInput(), mockOutput());
    }

    private ReaderWriterProcess sut(InputStream in, OutputStream out) throws Exception {
        when(process.getInputStream()).thenReturn(in);
        when(process.getOutputStream()).thenReturn(out);
        sut = ReaderWriterProcessImpl.FACTORY.getProcess("any");
        return sut;
    }

    @Test
    public void getWriter__write_chokes_onStreamError() throws Exception {
        doThrow(new IOException()).when(outputStream).write(anyInt());
        exception.expect(IOException.class);

        Writer writer = sut().getWriter();
        writer.write(text);
        writer.flush();
    }

    @Test
    public void getWriter__write_sendStringToStream() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Writer writer = sut(mockInput(), stream).getWriter();
        writer.write(text);
        writer.flush();

        assertThat(stream.toByteArray(), equalTo(text.getBytes()));
    }

    @Test
    public void getReader_read_chokes_onStreamError() throws Exception {
        doThrow(new IOException()).when(inputStream).read();
        exception.expect(IOException.class);

        BufferedReader reader = sut().getReader();
        reader.readLine();
    }

    @Test
    public void getReader_read_receiveStringFromStream() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());

        BufferedReader reader = sut(stream, mockOutput()).getReader();
        String result = reader.readLine();

        assertThat(result, equalTo(text));
    }

    @Test
    public void destroy_destroysProcess() throws Exception {
        sut().destroy();

        verify(process).destroy();
    }

    @Test
    public void destroy_closesStreams() throws Exception {
        sut().destroy();

        verify(inputStream).close();
        verify(outputStream).close();
    }

    @Test
    public void destroy_destroys_onStreamError() throws Exception {
        doThrow(new AssertionError()).when(outputStream).close();

        try {
            sut().destroy();
            Assert.fail("exepect exception");
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (Throwable e) {
        }

        verify(process).destroy();
    }

    @Test
    public void destroy_destroysAndCloses_onOutputStreamException() throws Exception {
        final RuntimeException ex = new RuntimeException(text);
        doThrow(ex).when(outputStream).close();

        sut().destroy();

        verify(process).destroy();
        verify(inputStream).close();
        verify(logger).debug(text, ex);
    }

    @Test
    public void destroy_destroysAndCloses_onInputStreamException() throws Exception {
        final IOException ex = new IOException(text);
        doThrow(ex).when(inputStream).close();

        sut().destroy();

        verify(process).destroy();
        verify(outputStream).close();
        verify(logger).debug(text, ex);
    }
}
