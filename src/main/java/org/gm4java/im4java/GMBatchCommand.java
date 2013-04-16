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
package org.gm4java.im4java;

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMExecutor;
import org.im4java.core.ImageCommand;
import org.im4java.process.ErrorConsumer;
import org.im4java.process.OutputConsumer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;

import javax.annotation.Nonnull;

/**
 * 
 * This class wraps the GM command in interactive or batch mode.
 * <p>
 * Note: Does not support asynchronous mode; Does not support BufferedImage
 * 
 * @author Kenneth Xu
 * 
 */
public class GMBatchCommand extends ImageCommand {
    private final GMExecutor executor;
    private OutputConsumer outputConsumer;
    private ErrorConsumer errorConsumer;

    /**
     * Construct a new instance of {@link GMBatchCommand} that uses given executor to execute specified command.
     * 
     * @param executor
     *            executor to execute the command
     * @param command
     *            command to be executed.
     */
    public GMBatchCommand(@Nonnull GMExecutor executor, @Nonnull String command) {
        super(command);
        this.executor = executor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutputConsumer(@Nonnull OutputConsumer pOutputConsumer) {
        super.setOutputConsumer(pOutputConsumer);
        outputConsumer = pOutputConsumer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setErrorConsumer(@Nonnull ErrorConsumer pErrorConsumer) {
        super.setErrorConsumer(pErrorConsumer);
        this.errorConsumer = pErrorConsumer;
    }

    /**
     * {@inheritDoc}
     * 
     * This implementation uses {@link GMExecutor} to execute the command.
     */
    @Override
    protected int run(@Nonnull LinkedList<String> pArgs) throws Exception {

        int rc;
        try {
            String result = executor.execute(pArgs);
            if (outputConsumer != null && result != null) outputConsumer.consumeOutput(stringToStream(result));
            rc = 0;
        } catch (GMException e) {
            if (errorConsumer != null) errorConsumer.consumeError(stringToStream(e.getMessage()));
            else throw e;
            rc = 1;
        }
        finished(rc);
        return rc;
    }

    @Nonnull
    private InputStream stringToStream(@Nonnull String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
