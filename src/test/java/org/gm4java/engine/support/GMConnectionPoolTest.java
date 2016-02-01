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

import org.apache.commons.pool.impl.GenericObjectPool;
import org.gm4java.engine.GMServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    private static final int MOCK_PROCESS_ARRAY_SIZE = 3;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private Writer writer;
    @Mock
    private BufferedReader reader;
    @Mock
    private ReaderWriterProcess process;
    @Mock
    private ReaderWriterProcess.Factory factory;
    @Mock
    private CommandSelector commandSelector;

    private String gmCommand = "convert something";
    private ReaderWriterProcess[] processes;
    private int processIndex;
    private GMConnectionPoolConfig config;
    private String[] capturedCommand;

    private GMConnectionPool sut;

    @Before
    public void setup() throws Exception {
        config = new GMConnectionPoolConfig();
        processIndex = 0;
        processes = new ReaderWriterProcess[MOCK_PROCESS_ARRAY_SIZE];

        MockitoAnnotations.initMocks(this);
        when(reader.readLine()).thenReturn("OK");
        when(process.getWriter()).thenReturn(writer);
        when(process.getReader()).thenReturn(reader);
        for (int i = 0; i < MOCK_PROCESS_ARRAY_SIZE; i++) {
            processes[i] = mock(ReaderWriterProcess.class);
        }
        when(factory.getProcess(Matchers.<String[]> anyVararg())).thenReturn(process);
        when(commandSelector.gmCommand()).thenReturn(Constants.GM_COMMAND.clone());
        sut = new GMConnectionPool(config, commandSelector);
        sut.setProcessFactory(factory);
    }

    @After
    public void teardown() throws Exception {
        sut.close();
    }

    private class MockFactory implements ReaderWriterProcess.Factory {
        @Override
        public ReaderWriterProcess getProcess(String... command) throws IOException {
            capturedCommand = command;
            if (processes[processIndex] == null) throw new IOException(READER_WRITER_PROCESS_FAILURE);
            return processes[processIndex++];
        }
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void constructor_chokes_onNullConfig() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("config");

        new GMConnectionPool(null, commandSelector);
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void constructor_chokes_onNullCommandSelector() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("commandselector");
        config = mock(GMConnectionPoolConfig.class);
        when(config.getConfig()).thenReturn(mock(GenericObjectPool.Config.class));

        new GMConnectionPool(config, null);
    }

    @Test
    public void borrowObject_chokes_whenCreateReaderWriterFails() throws Exception {
        when(factory.getProcess(Matchers.<String[]> anyVararg())).thenThrow(
                new IOException(READER_WRITER_PROCESS_FAILURE));

        exception.expect(GMServiceException.class);
        exception.expectMessage(READER_WRITER_PROCESS_FAILURE);

        sut.borrowObject();
    }

    @Test
    public void borrowObject_propagatesRuntimeException() throws Exception {
        when(factory.getProcess((String[]) anyVararg())).thenThrow(
                new NullPointerException(READER_WRITER_PROCESS_FAILURE));

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
        sut.setProcessFactory(new MockFactory());
        final String gmPath = "random string";
        sut.setGMPath(gmPath);

        sut.borrowObject();

        assertThat(sut.getGMPath(), is(gmPath));
        assertThat(capturedCommand[0], is(gmPath));
    }

    @Test
    public void close_destories_allProcesses() throws Exception {
        PooledGMConnection[] pooled = new PooledGMConnection[MOCK_PROCESS_ARRAY_SIZE];
        sut.setProcessFactory(new MockFactory());
        for (int i = 0; i < MOCK_PROCESS_ARRAY_SIZE; i++)
            pooled[i] = sut.borrowObject();
        for (int i = 0; i < MOCK_PROCESS_ARRAY_SIZE; i++)
            sut.returnObject(pooled[i]);

        sut.close();

        for (int i = 0; i < MOCK_PROCESS_ARRAY_SIZE; i++)
            verify(processes[i]).destroy();
    }
}
