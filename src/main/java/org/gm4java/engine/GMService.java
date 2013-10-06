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
package org.gm4java.engine;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Defines the GraphicsMagick service interface.
 * <p>
 * Implementations of this interface must ensure all the methods defined in this interface are thread safe.
 * 
 * @author Kenneth Xu
 * 
 */
public interface GMService extends GMExecutor {
    /**
     * Executes the GraphisMagick command and return the result.
     * <p>
     * This is a convenient method to {@link #getConnection() get the connection},
     * {@link GMConnection#execute(String, String...) execute} the command once and {@link GMConnection#close() close}
     * it. It is functionally equivalent to code below, but actual implementation may optimize this for better
     * efficiency.
     * 
     * <pre>
     * final GMConnection connection = gmService.getConnection();
     * try {
     *     return connection.execute(command, argument1, argument2, ...);
     * } finally {
     *     connection.close();
     * }
     * </pre>
     * <p>
     * This method is thread safe.
     * 
     * @param command
     *            the command to be executed
     * @param arguments
     *            arguments of the command.
     * @return the output from GraphicsMagick as the result of executing the command
     * @throws NullPointerException
     *             when command is null
     * @throws IOException
     *             when GM encounter IO error executing the command
     * @throws GMException
     *             when GraphicsMagick returns non-IO error executing the command
     * @throws GMServiceException
     *             when there is error communicating with the underlying GraphicsMagick process
     */
    @Override
    String execute(@Nonnull String command, String... arguments) throws IOException, GMException, GMServiceException;

    /**
     * Executes the command using the underlying GraphicsMagick process. GraphicsMagick command and its arguments are
     * passed in as a list of strings.
     * <p>
     * This is a convenient method to {@link #getConnection() get the connection}, {@link GMConnection#execute(List)
     * execute} the command once and {@link GMConnection#close() close} it. It is functionally equivalent to code below,
     * but actual implementation may optimize this for better efficiency.
     * 
     * <pre>
     * final GMConnection connection = gmService.getConnection();
     * try {
     *     return connection.execute(command);
     * } finally {
     *     connection.close();
     * }
     * </pre>
     * <p>
     * This method is thread safe.
     * 
     * @param command
     *            the command and arguments to be executed
     * @return the output from GraphicsMagick as the result of executing the command
     * @throws NullPointerException
     *             when command is null
     * @throws IllegalArgumentException
     *             when command is an empty list.
     * @throws IOException
     *             when GM encounter IO error executing the command
     * @throws GMException
     *             when GraphicsMagick returns non-IO error executing the command
     * @throws GMServiceException
     *             when there is error communicating with the underlying GraphicsMagick process
     * @see #execute(String, String...)
     */
    @Override
    String execute(@Nonnull List<String> command) throws IOException, GMException, GMServiceException;

    /**
     * Gets an instance of {@link GMConnection}. Depends on the implementation, the instance can be newly created or
     * from a pool.
     * <p>
     * This method is thread safe.
     * 
     * @return an instance of {@linkplain GMConnection}
     * @throws GMServiceException
     *             when communicate error occurs between the physical GraphicsMagick process.
     */
    @Nonnull
    GMConnection getConnection() throws GMServiceException;
}
