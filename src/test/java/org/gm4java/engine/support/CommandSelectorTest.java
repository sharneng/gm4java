package org.gm4java.engine.support;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class CommandSelectorTest {

    private final String GM_BASE_COMMAND = "gm";
    private Shell shell;
    private CommandSelector commandSelector;

    @Before
    public void setUp() throws Exception {
        shell = mock(Shell.class);
        this.commandSelector = new CommandSelector(GM_BASE_COMMAND, shell);
    }

    @Test
    public void test_contains_no_safe_word_argument() throws IOException {


        Process process = mock(Process.class);

        String fakeInput = "GraphicsMagick 1.3.22 2015-11-07 Q16 http://www.GraphicsMagick.org/\n" +
                "Copyright (C) 2002-2015 GraphicsMagick Group.";

        InputStream inputStream = new ByteArrayInputStream(fakeInput.getBytes("UTF-8"));

        when(shell.exec(new String[] {GM_BASE_COMMAND, "version"})
        ).thenReturn(process);

        when(process.getInputStream()).thenReturn(inputStream);


        String[] command = commandSelector.gmCommand();

        verify(shell).exec(
                eq(new String[] {GM_BASE_COMMAND, "version"})
        );
        verify(process).getInputStream();

        assertThat(
                command,
                arrayContaining(Constants.GM_COMMAND_V1_3_22)
        );

        verifyNoMoreInteractions(shell, process);
    }

    @Test
    public void test_contains_safe_word_argument_for_legacy_versions() throws IOException {
        Process process = mock(Process.class);

        String fakeInput = "GraphicsMagick 1.3.21 20??-??-?? ??? http://www.GraphicsMagick.org/\n" +
                "Copyright (C) 2002-20?? GraphicsMagick Group.";

        InputStream inputStream = new ByteArrayInputStream(fakeInput.getBytes("UTF-8"));

        when(shell.exec(isA(String[].class))
        ).thenReturn(process);

        when(process.getInputStream()).thenReturn(inputStream);

        String[] command = commandSelector.gmCommand();

        verify(shell).exec(
                eq(new String[] {GM_BASE_COMMAND, "version"})
        );
        verify(process).getInputStream();

        assertThat(
                command,
                arrayContaining(Constants.GM_COMMAND)
        );

        verifyNoMoreInteractions(shell, process);
    }

    @Test
    public void test_checks_version_only_once() throws IOException {


        Process process = mock(Process.class);

        String fakeInput = "GraphicsMagick 1.3.22 2015-11-07 Q16 http://www.GraphicsMagick.org/\n" +
                "Copyright (C) 2002-2015 GraphicsMagick Group.";

        InputStream inputStream = new ByteArrayInputStream(fakeInput.getBytes("UTF-8"));

        when(shell.exec(new String[] {GM_BASE_COMMAND, "version"})
        ).thenReturn(process);

        when(process.getInputStream()).thenReturn(inputStream);


        commandSelector.gmCommand();
        String[] command = commandSelector.gmCommand();

        verify(shell, times(1)).exec(
                eq(new String[] {GM_BASE_COMMAND, "version"})
        );
        verify(process).getInputStream();

        assertThat(
                command,
                arrayContaining(Constants.GM_COMMAND_V1_3_22)
        );

        verifyNoMoreInteractions(shell, process);
    }

    @Test(expected = RuntimeException.class)
    public void test_empty_command_input_throws_exception() throws IOException {
        Process process = mock(Process.class);

        String fakeInput = "";

        InputStream inputStream = new ByteArrayInputStream(fakeInput.getBytes("UTF-8"));

        when(shell.exec(new String[] {GM_BASE_COMMAND, "version"})
        ).thenReturn(process);

        when(process.getInputStream()).thenReturn(inputStream);


        String[] command = commandSelector.gmCommand();

        verify(shell, times(1)).exec(
                eq(new String[] {GM_BASE_COMMAND, "version"})
        );
        verify(process).getInputStream();

        assertThat(
                command,
                arrayContaining(Constants.GM_COMMAND_V1_3_22)
        );

        verifyNoMoreInteractions(shell, process);
    }

    @Test(expected = RuntimeException.class)
    public void test_ioexception() throws IOException {
        Process process = mock(Process.class);

        when(shell.exec(new String[] {GM_BASE_COMMAND, "version"})
        ).thenThrow(new IOException("foo"));

        commandSelector.gmCommand();

        verify(shell, times(1)).exec(
                eq(new String[] {GM_BASE_COMMAND, "version"})
        );
        verify(process).getInputStream();

        verifyNoMoreInteractions(shell, process);
    }
}
