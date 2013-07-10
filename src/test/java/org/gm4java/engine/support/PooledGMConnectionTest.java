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

//import static org.hamcrest.MatcherAssert.*;
//import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMServiceException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;

/**
 * Test cases for {@link PooledGMConnection}.
 * 
 * @author Kenneth Xu
 * 
 */
public class PooledGMConnectionTest extends AbstractGMConnectionTest {
    private static final String READER_WRITER_PROCESS_FAILURE = "Something bad happended";

    @Mock
    private ReaderWriterProcess.Factory factory;
    @Mock
    private GMConnectionPool pool;

    private PooledGMConnection sut;

    private String gmCommand = "convert something";

    @Before
    public void setup() throws Exception {
        super.setup();
        pool.setProcessFactory(factory);
        when(pool.createProcess()).thenReturn(process);
        sut = new PooledGMConnection(pool);
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void constructor_chokes_onNullPath() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("pool");
        new PooledGMConnection(null);
    }

    @Test
    public void constructor_chokes_whenCreateReaderWriterFails() throws Exception {
        when(pool.createProcess()).thenThrow(new GMServiceException(READER_WRITER_PROCESS_FAILURE));

        exception.expect(GMServiceException.class);
        exception.expectMessage(READER_WRITER_PROCESS_FAILURE);

        new PooledGMConnection(pool);
    }

    @Test
    public void ensureHealthy_happyDay_whenWithinLimit() throws Exception {
        final int limit = 10;
        when(pool.getEvictAfterNumberOfUse()).thenReturn(limit);
        ensureHealthy_happyDay(limit);
        // no exception should have thrown
    }

    @Test
    public void ensureHealthy_happyDay_onNoLimit() throws Exception {
        when(pool.getEvictAfterNumberOfUse()).thenReturn(0);
        ensureHealthy_happyDay(10);
        // no exception should have thrown
    }

    private void ensureHealthy_happyDay(int count) throws Exception {
        when(reader.readLine()).thenReturn("OK");
        for (int i = 0; i < count; i++)
            sut.execute(gmCommand);

        sut.ensureHealthy();
    }

    @Test
    public void ensureHealthy_chokes_whenOutOfLimit() throws Exception {
        final int limit = 10;
        when(pool.getEvictAfterNumberOfUse()).thenReturn(limit);
        exception.expect(GMServiceException.class);
        ensureHealthy_happyDay(limit + 1);
    }

    @Test
    public void ensureHealthy_chokes_whenServiceExceptionWasThrown() throws Exception {
        when(reader.readLine()).thenThrow(new IOException());
        try {
            sut.execute(gmCommand);
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMServiceException e) {
        }

        exception.expect(GMServiceException.class);
        sut.ensureHealthy();
    }

    @Test
    public void ensureHealthy_happyDay_whenGMExceptionWasThrown() throws Exception {
        when(reader.readLine()).thenReturn("NG");
        try {
            sut.execute(gmCommand);
            // SUPPRESS CHECKSTYLE EmptyBlock BECAUSE test
        } catch (GMException e) {
        }

        sut.ensureHealthy();
    }

    @Override
    protected GMConnection sut() {
        return sut;
    }
}
