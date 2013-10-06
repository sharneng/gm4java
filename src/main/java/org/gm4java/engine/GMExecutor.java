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
 * An interface to execute command using the underlying GraphicsMagick Process.
 * 
 * @author Kenneth Xu
 * 
 */
public interface GMExecutor {
    /**
     * Executes the command using the underlying GraphicsMagick process.
     * <p>
     * This method can be used in different ways explained in the following sections using an example of drawing text NO
     * IMAGE on a background defined by in.png.
     * <p>
     * One way is to prepare entire command line with proper space separation of GraphicsMagick arguments and pass it as
     * one single string to command parameter. You don't need to use arguments parameter at all. Using this way, you
     * need to make sure special characters are escaped and arguments with space are quoted. e.g.,
     * 
     * <pre>
     * <code>
     * execute("convert in.png -draw \"text 50 100 \"\"NO IMAGE\"\"\" out.png");
     * </code>
     * </pre>
     * 
     * A better way is to pass the command and arguments separately. The same example can now be written as below.
     * Notice that you don't need to add the quotes and escape the quotes anymore.
     * 
     * <pre>
     * <code>
     * execute("convert", "in.png", "-draw", "text 50 100 \"NO IMAGE\"", "out.png");
     * </code>
     * </pre>
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
     * @see #execute(List)
     */
    String execute(@Nonnull String command, String... arguments) throws IOException, GMException, GMServiceException;

    /**
     * Executes the command using the underlying GraphicsMagick process. GraphicsMagick command and its arguments are
     * passed in as a list of strings.
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
    String execute(@Nonnull List<String> command) throws IOException, GMException, GMServiceException;
}
