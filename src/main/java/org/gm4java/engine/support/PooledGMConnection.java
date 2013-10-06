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

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMServiceException;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link org.gm4java.engine.GMConnection} that is used by {@link PooledGMService}.
 * 
 * @author Kenneth Xu
 * 
 */
class PooledGMConnection extends BasicGMConnection {
    private final GMConnectionPool pool;
    private int count;
    private Throwable exception;

    PooledGMConnection(@Nonnull GMConnectionPool pool) throws GMServiceException {
        super(notNull(pool).createProcess());
        this.pool = pool;
    }

    @Override
    protected String execute(String command, @Nonnull List<String> arguments) throws IOException, GMException,
            GMServiceException {
        count++;
        try {
            return super.execute(command, arguments);
        } catch (IOException e) {
            throw e;
        } catch (GMException e) {
            throw e;
        } catch (RuntimeException e) {
            exception = e;
            throw e;
        } catch (GMServiceException e) {
            exception = e;
            throw e;
        } catch (Error e) {
            exception = e;
            throw e;
        }
    }

    void ensureHealthy() throws GMServiceException {
        if (exception != null) throw new GMServiceException(exception.getMessage(), exception);
        int limit = pool.getEvictAfterNumberOfUse();
        if (limit > 0 && count > limit) {
            throw new GMServiceException(String.format(
                    "Instance is stale, executed %d commands which exceeded the %d limit.", count, limit));
        }
    }

    private static GMConnectionPool notNull(GMConnectionPool pool) {
        if (pool == null) throw new NullPointerException("pool");
        return pool;
    }
}
