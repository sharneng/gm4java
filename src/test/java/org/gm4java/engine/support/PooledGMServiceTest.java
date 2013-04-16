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

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMServiceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

/**
 * Test cases for {@link PooledGMService}.
 * 
 * @author Kenneth Xu
 * 
 */
public class PooledGMServiceTest {
    private static final String PATH_TO_GM = "path to gm";
    private static final String CREATE_PROCESS_FAILURE = "Something bad happended";

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private GMConnectionPool pool;
    @Mock
    private PooledGMConnection connection;

    private String gmCommand = "convert something";
    private GMConnectionPoolConfig config;

    private PooledGMService sut;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(pool.borrowObject()).thenReturn(connection);
        sut = new PooledGMService(pool);
    }

    @Test
    public void constructor_chokes_onNullConfig() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("config");
        config = null;

        new PooledGMService(config);
    }

    @Test
    public void constructor_passesConfigToPool() throws Exception {
        config = new GMConnectionPoolConfig();
        final String expectedGMPath = PATH_TO_GM;
        config.gmPath = expectedGMPath;
        sut = new PooledGMService(config);
        ReaderWriterProcess.Factory factory = mock(ReaderWriterProcess.Factory.class);
        ReaderWriterProcess process = mock(ReaderWriterProcess.class);
        sut.setProcessFactory(factory);
        when(factory.getProcess(Matchers.<String[]> anyVararg())).thenReturn(process);

        sut.getConnection();

        TestUtils.verifyFactoryCalledWithGMPath(factory, expectedGMPath);
    }

    @Test
    public void getConnection_chokes_whenBorrowObjectChokes() throws Exception {
        when(pool.borrowObject()).thenThrow(new GMServiceException(CREATE_PROCESS_FAILURE));
        exception.expect(GMServiceException.class);
        exception.expectMessage(CREATE_PROCESS_FAILURE);

        sut.getConnection();
    }

    @Test
    public void getConnection__close_returnsBorrowedConnectionInsteadOfCloseIt() throws Exception {
        GMConnection c = sut.getConnection();

        c.close();

        verify(connection, never()).close();
        verify(pool).returnObject(connection);
    }

    @Test
    public void getConnection__close_canBeCalledMultipleTimes() throws Exception {
        GMConnection p = sut.getConnection();

        p.close();
        p.close();
    }

    @Test
    public void getConnection__execute_chokes_afterClose() throws Exception {
        GMConnection p = sut.getConnection();
        p.close();
        exception.expect(GMServiceException.class);
        exception.expectMessage("closed");

        p.execute(gmCommand);
    }

    @Test
    public void getConnection__executeByList_chokes_afterClose() throws Exception {
        GMConnection p = sut.getConnection();
        p.close();
        exception.expect(GMServiceException.class);
        exception.expectMessage("closed");

        p.execute(Arrays.asList(gmCommand));
    }

    @Test
    public void getConnection__execute_delegatesToBorrowedConnection() throws Exception {
        String expected = "result 9465";
        when(connection.execute(gmCommand)).thenReturn(expected);
        GMConnection p = sut.getConnection();

        String result = p.execute(gmCommand);
        p.close();

        verify(connection).execute(gmCommand);
        assertThat(result, is(expected));
    }

    @Test
    public void getConnection__executeByList_delegatesToBorrowedConnection() throws Exception {
        String expected = "result 9465";
        final List<String> command = Arrays.asList(gmCommand);
        when(connection.execute(command)).thenReturn(expected);
        GMConnection p = sut.getConnection();

        String result = p.execute(command);
        p.close();

        verify(connection).execute(command);
        assertThat(result, is(expected));
    }

    @Test
    public void execute_chokes_whenBorrowObjectChokes() throws Exception {
        when(pool.borrowObject()).thenThrow(new GMServiceException(CREATE_PROCESS_FAILURE));
        exception.expect(GMServiceException.class);
        exception.expectMessage(CREATE_PROCESS_FAILURE);

        sut.execute(gmCommand);
    }

    @Test
    public void executeByList_chokes_whenBorrowObjectChokes() throws Exception {
        when(pool.borrowObject()).thenThrow(new GMServiceException(CREATE_PROCESS_FAILURE));
        exception.expect(GMServiceException.class);
        exception.expectMessage(CREATE_PROCESS_FAILURE);

        sut.execute(Arrays.asList(gmCommand));
    }

    @Test
    public void execute_delegatesToBorrowedConnection() throws Exception {
        String expected = "result 9465";
        when(connection.execute(gmCommand)).thenReturn(expected);
        String result = sut.execute(gmCommand);

        verify(connection).execute(gmCommand);
        assertThat(result, is(expected));
    }

    @Test
    public void executeByList_delegatesToBorrowedConnection() throws Exception {
        String expected = "result 9465";
        final List<String> command = Arrays.asList(gmCommand);
        when(connection.execute(command)).thenReturn(expected);
        String result = sut.execute(command);

        verify(connection).execute(command);
        assertThat(result, is(expected));
    }

    @Test
    public void execute_returnBorrowedConnection() throws Exception {
        sut.execute(gmCommand);

        verify(pool).returnObject(connection);
    }

    @Test
    public void executeByList_returnBorrowedConnection() throws Exception {
        sut.execute(Arrays.asList(gmCommand));

        verify(pool).returnObject(connection);
    }

    @Test
    public void execute_returnBorrowedConnection_onError() throws Exception {
        when(connection.execute(gmCommand)).thenThrow(new GMException(""));
        try {
            sut.execute(gmCommand);
            Assert.fail("shoud get exeception here.");
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMException e) {
        }

        verify(pool).returnObject(connection);
    }

    @Test
    public void executeByList_returnBorrowedConnection_onError() throws Exception {
        final List<String> command = Arrays.asList(gmCommand);
        when(connection.execute(command)).thenThrow(new GMException(""));
        try {
            sut.execute(command);
            Assert.fail("shoud get exeception here.");
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMException e) {
        }

        verify(pool).returnObject(connection);
    }
}
