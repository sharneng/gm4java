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

import org.gm4java.engine.GMServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 * Test cases for {@link GMConnectionPool}.
 * 
 * @author Kenneth Xu
 * 
 */
public class GMConnectionPoolTest {
    private static final String READER_WRITER_PROCESS_FAILURE = "Something bad happended";

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private Writer writer;
    @Mock
    private BufferedReader reader;
    @Mock
    private ReaderWriterProcess process;
    @Mock
    private GMProcessFactory factory;
    @Mock
    private GMProcessFactory.Builder builder;

    private final String gmCommand = "convert something";
    private GMConnectionPoolConfig config;

    private GMConnectionPool sut;

    @Before
    public void setup() throws Exception {
        config = new GMConnectionPoolConfig();

        MockitoAnnotations.initMocks(this);
        when(reader.readLine()).thenReturn("OK");
        when(process.getWriter()).thenReturn(writer);
        when(process.getReader()).thenReturn(reader);
        final ArgumentCaptor<String> gmPathCaptor = ArgumentCaptor.forClass(String.class);
        when(builder.buildFactory(gmPathCaptor.capture())).thenReturn(factory);
        when(factory.getProcess()).thenReturn(process);
        when(factory.getGMPath()).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return gmPathCaptor.getValue();
            }
        });
        sut = new GMConnectionPool(config);
        sut.setProcessFactoryBuilder(builder);
    }

    @After
    public void teardown() throws Exception {
        sut.close();
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void constructor_chokes_onNullConfig() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("config");

        new GMConnectionPool(null);
    }

    @Test
    public void borrowObject_chokes_whenCreateReaderWriterFails() throws Exception {
        when(factory.getProcess()).thenThrow(new IOException(READER_WRITER_PROCESS_FAILURE));

        exception.expect(GMServiceException.class);
        exception.expectMessage(READER_WRITER_PROCESS_FAILURE);

        sut.borrowObject();
    }

    @Test
    public void borrowObject_propagatesRuntimeException() throws Exception {
        when(factory.getProcess()).thenThrow(new NullPointerException(READER_WRITER_PROCESS_FAILURE));

        exception.expect(NullPointerException.class);
        exception.expectMessage(READER_WRITER_PROCESS_FAILURE);

        sut.borrowObject();
    }

    @Test
    public void borrowObject_doesNotReturnUnhealthyConnection() throws Exception {
        when(reader.readLine()).thenThrow(new IOException(READER_WRITER_PROCESS_FAILURE));
        PooledGMConnection connection = sut.borrowObject();
        try {
            connection.execute(gmCommand);
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMServiceException e) {
        }
        sut.returnObject(connection);

        PooledGMConnection connection2 = sut.borrowObject();

        assertThat(connection2, not(is(connection)));
        connection2.ensureHealthy(); // should not throw exception
    }

    @Test
    public void setEvictAfterNumberOfUse_limitsTheUseOfConnection() throws Exception {
        PooledGMConnection connection = sut.borrowObject();
        final int limit = 5;
        sut.setEvictAfterNumberOfUse(limit);
        // execute it limit+1 times
        for (int i = 0; i <= limit; i++)
            connection.execute(gmCommand);
        sut.returnObject(connection);

        PooledGMConnection connection2 = sut.borrowObject();

        assertThat(connection2, not(is(connection)));
        connection2.ensureHealthy(); // should not throw exception
    }

    @Test
    public void borrowObject_reusesObjectReturnedToPool() throws Exception {
        PooledGMConnection connection = sut.borrowObject();
        sut.returnObject(connection);

        PooledGMConnection connection2 = sut.borrowObject();

        assertThat(connection2, is(connection));
    }

    @Test
    public void setTestOnX_noEffect_whenOff() throws Exception {
        when(reader.readLine()).thenThrow(new IOException(READER_WRITER_PROCESS_FAILURE));
        sut.setTestOnBorrow(false);
        sut.setTestOnReturn(false);

        PooledGMConnection connection = sut.borrowObject();
        sut.returnObject(connection);
        PooledGMConnection connection2 = sut.borrowObject();

        assertThat(connection2, is(connection));
    }

    @Test
    public void setTestOnBorrow_removesTestFailedConnection() throws Exception {
        when(reader.readLine()).thenReturn("OK", "NG", "OK");
        sut.setTestOnBorrow(true);

        PooledGMConnection connection = sut.borrowObject();
        sut.returnObject(connection);
        PooledGMConnection connection2 = sut.borrowObject();

        assertThat(connection2, not(is(connection)));
    }

    @Test
    public void setTestOnReturn_removesTestFailedConnection() throws Exception {
        when(reader.readLine()).thenReturn("NG", "OK");
        sut.setTestOnReturn(true);

        PooledGMConnection connection = sut.borrowObject();
        sut.returnObject(connection);
        PooledGMConnection connection2 = sut.borrowObject();

        assertThat(connection2, not(is(connection)));
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void setGMPath_chokes_onNullPath() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("gmPath");

        sut.setGMPath(null);
    }

    @Test
    public void setGMPath_changesCommandSendtoGM() throws Exception {
        final String gmPath = "random string";
        sut.setGMPath(gmPath);

        sut.borrowObject();

        assertThat(sut.getGMPath(), is(gmPath));
        verify(builder).buildFactory(gmPath);
    }

    @Test
    public void close_destories_allProcesses() throws Exception {
        final int processCount = 3;
        PooledGMConnection[] pooled = new PooledGMConnection[processCount];
        final ReaderWriterProcess[] processes = new ReaderWriterProcess[processCount];
        for (int i = 0; i < processCount; i++) {
            processes[i] = mock(ReaderWriterProcess.class);
        }
        when(factory.getProcess()).then(new Answer<ReaderWriterProcess>() {
            int processIndex = 0;

            @Override
            public ReaderWriterProcess answer(InvocationOnMock invocation) throws Throwable {
                if (processes[processIndex] == null) throw new IOException(READER_WRITER_PROCESS_FAILURE);
                return processes[processIndex++];
            }

        });
        for (int i = 0; i < processCount; i++)
            pooled[i] = sut.borrowObject();
        for (int i = 0; i < processCount; i++)
            sut.returnObject(pooled[i]);

        sut.close();

        for (int i = 0; i < processCount; i++)
            verify(processes[i]).destroy();
    }
}
