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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMServiceException;
import org.gm4java.engine.support.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;


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
    @Mock
    private CommandSelector commandSelector;

    private final String gmCommand = "convert something";
    private GMConnectionPoolConfig config;

    private PooledGMService sut;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(pool.borrowObject()).thenReturn(connection);
        sut = new PooledGMService(pool);
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void constructor_chokes_onNullConfig() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("config");
        config = null;

        new PooledGMService(config, commandSelector);
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void constructor_chokes_onNullCommandSelector() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("commandselector");
        config = mock(GMConnectionPoolConfig.class);
        when(config.getConfig()).thenReturn(mock(GenericObjectPool.Config.class));
        commandSelector = null;

        new PooledGMService(config, commandSelector);
    }

    @Test
    public void constructor_passesConfigToPool() throws Exception {
        config = new GMConnectionPoolConfig();
        final String expectedGMPath = PATH_TO_GM;
        config.setGMPath(expectedGMPath);

        when(commandSelector.isVersionEqualOrGratherThan_1_3_22(anyString())).thenReturn(true);
        when(commandSelector.gmCommand()).thenCallRealMethod();
        when(commandSelector.setGmPath(anyString())).thenCallRealMethod();

        sut = new PooledGMService(config, commandSelector);
        ReaderWriterProcess.Factory factory = mock(ReaderWriterProcess.Factory.class);
        ReaderWriterProcess process = mock(ReaderWriterProcess.class);
        sut.setProcessFactory(factory);
        when(factory.getProcess(Matchers.<String[]> anyVararg())).thenReturn(process);

        sut.getConnection();

        TestUtils.verifyFactoryCalledWithGMPath(factory, expectedGMPath);

        verify(commandSelector).isVersionEqualOrGratherThan_1_3_22(eq(expectedGMPath));
        verify(commandSelector).gmCommand();
        verify(commandSelector).setGmPath(expectedGMPath);
        verifyNoMoreInteractions(commandSelector);
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
            fail("shoud get exeception here.");
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
            fail("shoud get exeception here.");
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMException e) {
        }

        verify(pool).returnObject(connection);
    }

}
