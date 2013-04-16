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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.gm4java.engine.GMException;
import org.gm4java.engine.GMService;
import org.im4java.core.CommandException;
import org.im4java.core.IMOperation;
import org.im4java.process.ArrayListOutputConsumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Test cases for {@link GMBatchCommand}.
 * 
 * @author Kenneth Xu
 * 
 */
public class GMBatchCommandTest {
    private static final String TARGET_IMAGE = "myimage_small.jpg";
    private static final String SOURCE_IMAGE = "myimage.jpg";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private GMService service;

    private GMBatchCommand sut;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void run_chokes_whenServiceChokes() throws Exception {
        // create command
        final String command = "bad";
        sut = new GMBatchCommand(service, command);
        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(SOURCE_IMAGE);
        op.resize(800, 600);
        op.addImage(TARGET_IMAGE);
        final String message = "bad command";
        when(service.execute(anyListOf(String.class))).thenThrow(new GMException(message));
        exception.expect(CommandException.class);
        exception.expectMessage(message);

        // execute the operation
        sut.run(op);
    }

    @Test
    public void run_chokes_whenErrorConsumerIsNull() throws Exception {
        // create command
        final String command = "bad";
        sut = new GMBatchCommand(service, command);
        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(SOURCE_IMAGE);
        op.resize(800, 600);
        op.addImage(TARGET_IMAGE);
        final String message = "bad command";
        when(service.execute(anyListOf(String.class))).thenThrow(new GMException(message));
        exception.expect(CommandException.class);
        exception.expectMessage(message);
        sut.setErrorConsumer(null);

        // execute the operation
        sut.run(op);
    }

    @Test
    public void run_sendsCommandToService() throws Exception {
        // create command
        final String command = "convert";
        sut = new GMBatchCommand(service, command);
        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(SOURCE_IMAGE);
        op.resize(800, 600);
        op.addImage(TARGET_IMAGE);

        // execute the operation
        sut.run(op);

        verify(service).execute(Arrays.asList(command, SOURCE_IMAGE, "-resize", "800x600", TARGET_IMAGE));
    }

    @Test
    public void run_works_whenOutputConsumerIsNull() throws Exception {
        // create command
        final String command = "convert";
        sut = new GMBatchCommand(service, command);
        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(SOURCE_IMAGE);
        op.resize(800, 600);
        op.addImage(TARGET_IMAGE);
        sut.setOutputConsumer(null);

        // execute the operation
        sut.run(op);

        verify(service).execute(Arrays.asList(command, SOURCE_IMAGE, "-resize", "800x600", TARGET_IMAGE));
    }

    @Test
    public void run_returnsResultBack() throws Exception {
        final String command = "identify";
        sut = new GMBatchCommand(service, command);
        IMOperation op = new IMOperation();
        op.ping();
        final String format = "%m\n%W\n%H\n%g\n%z";
        op.format(format);
        op.addImage();
        ArrayListOutputConsumer output = new ArrayListOutputConsumer();
        sut.setOutputConsumer(output);
        when(service.execute(anyListOf(String.class))).thenReturn("JPEG\n800\n600\n800x600\n0\n");

        sut.run(op, SOURCE_IMAGE);

        verify(service).execute(Arrays.asList(command, "-ping", "-format", format, SOURCE_IMAGE));
        // ... and parse result
        ArrayList<String> cmdOutput = output.getOutput();
        Iterator<String> iter = cmdOutput.iterator();
        assertThat(iter.next(), is("JPEG"));
        assertThat(iter.next(), is("800"));
        assertThat(iter.next(), is("600"));
        assertThat(iter.next(), is("800x600"));
        assertThat(iter.next(), is("0"));
    }
}
