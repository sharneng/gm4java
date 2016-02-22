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
import org.gm4java.engine.GMService;
import org.gm4java.engine.GMServiceException;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link GMService} that manages and uses a pool of GraphicsMagick instances. This implementation uses
 * {@link org.apache.commons.pool.impl.GenericObjectPool} from <a href="http://commons.apache.org/pool/">Apache Commons
 * Pool</a> project.
 * 
 * @author Kenneth Xu
 * 
 */
public class PooledGMService implements GMService {
    private final GMConnectionPool pool;

    /**
     * Construct a new instance of {@linkplain PooledGMService} with given pool configuration.
     * 
     * @param config
     *            configuration of the GraphicsMagick process pool
     */
    public PooledGMService(@Nonnull GMConnectionPoolConfig config) {
        pool = new GMConnectionPool(config, new CommandSelector(config.getGMPath()));
    }
    
    /**
     * Construct a new instance of {@linkplain PooledGMService} with given pool configuration.
     * 
     * @param config
     *            configuration of the GraphicsMagick process pool
     * @param gmPath gm binary
     */
    public PooledGMService(@Nonnull GMConnectionPoolConfig config, CommandSelector commandSelector) {
        pool = new GMConnectionPool(config, commandSelector);
    }

    PooledGMService(GMConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(@Nonnull String command, String... arguments) throws IOException, GMException,
            GMServiceException {
        PooledGMConnection connection = pool.borrowObject();
        try {
            return connection.execute(command, arguments);
        } finally {
            pool.returnObject(connection);
        }
    }

    @Override
    public String execute(List<String> command) throws IOException, GMException, GMServiceException {
        PooledGMConnection connection = pool.borrowObject();
        try {
            return connection.execute(command);
        } finally {
            pool.returnObject(connection);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an instance from pool.
     */
    @Override
    @Nonnull
    public GMConnection getConnection() throws GMServiceException {
        return new ConnectionWrapper(pool);
    }

    void setProcessFactory(ReaderWriterProcess.Factory factory) {
        pool.setProcessFactory(factory);
    }

    private static final class ConnectionWrapper implements GMConnection {
        @Nonnull
        private final GMConnectionPool pool;
        private PooledGMConnection real;

        private ConnectionWrapper(GMConnectionPool pool) throws GMServiceException {
            this.pool = pool;
            real = pool.borrowObject();
        }

        @Override
        public String execute(@Nonnull String command, String... arguments) throws IOException, GMException,
                GMServiceException {
            assertConnectionNotClosed();
            return real.execute(command, arguments);
        }

        @Override
        public String execute(List<String> command) throws IOException, GMException, GMServiceException {
            assertConnectionNotClosed();
            return real.execute(command);
        }

        @Override
        public void close() throws GMServiceException {
            if (real == null) return;
            pool.returnObject(real);
            real = null;
        }

        private void assertConnectionNotClosed() throws GMServiceException {
            if (real == null) throw new GMServiceException("GMConnection is already closed.");
        }
    }
}
