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
import org.gm4java.engine.support.ReaderWriterProcess.Factory;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link GMService} that creates a new GraphicsMagick process every time {@link #getConnection()} is
 * called.
 * 
 * @author Kenneth Xu
 * 
 */
public class SimpleGMService implements GMService {
    /**
     * Default path to GraphcisMagick executable is simply "gm".
     */
    public static final String DEFAULT_GM_PATH = Constants.DEFAULT_GM_PATH;

    private ReaderWriterProcess.Factory factory = ReaderWriterProcessImpl.FACTORY;
    private final String[] gmCommand;

    public SimpleGMService() {
        this.gmCommand = new CommandSelector(DEFAULT_GM_PATH).gmCommand();
    }

    /**
     * Gets the path to GraphicsMagick executable set by {@link #setGMPath(String)} or {@link #DEFAULT_GM_PATH} if it
     * was not explicitly set.
     * 
     * @return the path to GraphicsMagick executable
     */
    @Nonnull
    public String getGMPath() {
        return gmCommand[0];
    }

    /**
     * Sets the path to GraphicsMagick executable.
     * 
     * @param gmPath
     *            the path to GraphicsMagick executable
     */
    public void setGMPath(@Nonnull String gmPath) {
        if (gmPath == null) throw new NullPointerException("gmPath");
        gmCommand[0] = gmPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(@Nonnull String command, String... arguments) throws GMException, GMServiceException,
            IOException {
        final GMConnection connection = getConnection();
        try {
            return connection.execute(command, arguments);
        } finally {
            connection.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(List<String> command) throws GMException, GMServiceException, IOException {
        final GMConnection connection = getConnection();
        try {
            return connection.execute(command);
        } finally {
            connection.close();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * This implementation creates a new instance.
     */
    @Override
    @Nonnull
    public GMConnection getConnection() throws GMServiceException {
        try {
            return new BasicGMConnection(factory.getProcess(gmCommand));
        } catch (IOException e) {
            throw new GMServiceException(e.getMessage(), e);
        }
    }

    void setProcessFactory(Factory factory) {
        this.factory = factory;
    }
}
