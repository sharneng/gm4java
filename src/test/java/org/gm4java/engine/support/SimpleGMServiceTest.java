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

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMServiceException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Arrays;

/**
 * Test cases for {@link SimpleGMService}.
 * 
 * @author Kenneth Xu
 * 
 */
public class SimpleGMServiceTest extends AbstractGMConnectionTest {
    private static final String CREATE_PROCESS_FAILURE = "Something bad happended";
    @Mock
    private ReaderWriterProcess.Factory factory;

    private SimpleGMService sut;
    private GMConnection connection;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        when(factory.getProcess(Matchers.<String[]> anyVararg())).thenReturn(process);
        sut = new SimpleGMService();
        sut.setProcessFactory(factory);
    }

    @Test
    public void getConnection_chokes_whenFactoryFailes() throws Exception {
        when(factory.getProcess(Matchers.<String> anyVararg())).thenThrow(new IOException(CREATE_PROCESS_FAILURE));
        exception.expect(GMServiceException.class);
        exception.expectMessage(CREATE_PROCESS_FAILURE);

        sut.getConnection();
    }

    @Test
    public void execute_delegatesToConnection() throws Exception {
        final String command = "some command";
        final String expected = "some result";
        when(process.getWriter()).thenReturn(mockWriter);
        when(reader.readLine()).thenReturn(expected, "OK");

        String result = sut.execute(command);

        verify(mockWriter).write(command);
        assertThat(result, is(expected));
    }

    @Test
    public void executeByList_delegatesToConnection() throws Exception {
        final String command = "some command";
        final String expected = "some result";
        when(process.getWriter()).thenReturn(mockWriter);
        when(reader.readLine()).thenReturn(expected, "OK");

        String result = sut.execute(Arrays.asList(command));

        verify(mockWriter).write(command);
        assertThat(result, is(expected));
    }

    @Test
    public void execute_chokes_onGMError() throws Exception {
        when(reader.readLine()).thenReturn("NG");
        exception.expect(GMException.class);

        sut.execute("any");
    }

    @Test
    public void executeByList_chokes_onGMError() throws Exception {
        when(reader.readLine()).thenReturn("NG");
        exception.expect(GMException.class);

        sut.execute(Arrays.asList("any"));
    }

    @Test
    public void execute_destroysProcess_onSuccess() throws Exception {
        when(reader.readLine()).thenReturn("OK");

        sut.execute("any");

        verify(process).destroy();
    }

    @Test
    public void executeByList_destroysProcess_onSuccess() throws Exception {
        when(reader.readLine()).thenReturn("OK");

        sut.execute(Arrays.asList("any"));

        verify(process).destroy();
    }

    @Test
    public void execute_destroysProcess_onFailure() throws Exception {
        when(reader.readLine()).thenReturn("NG");

        try {
            sut.execute("any");
            Assert.fail("GMException should have been thrown.");
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMException e) {
        }

        verify(process).destroy();
    }

    @Test
    public void executeByList_destroysProcess_onFailure() throws Exception {
        when(reader.readLine()).thenReturn("NG");

        try {
            sut.execute(Arrays.asList("any"));
            Assert.fail("GMException should have been thrown.");
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMException e) {
        }

        verify(process).destroy();
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void setGMPath_chokes_onNullPath() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("gmPath");

        sut.setGMPath(null);
    }

    @Test
    public void setGMPath_changesPathPassedToFactory() throws Exception {
        final String expectedPath = "some path";
        sut.setGMPath(expectedPath);

        sut.getConnection();

        assertThat(sut.getGMPath(), is(expectedPath));
        TestUtils.verifyFactoryCalledWithGMPath(factory, expectedPath);
    }

    @Override
    protected GMConnection sut() throws Exception {
        if (connection == null) connection = sut.getConnection();
        return connection;
    }
}
