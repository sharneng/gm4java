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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class GMProcessFactoryImplTest {

    @Mock
    private ReaderWriterProcess.Factory factory;
    @Mock
    private ReaderWriterProcess process;

    private String version = "1.3.22";
    private final String releaseDate = "2015-11-07";
    private final String quality = "Q16";
    private final String fakeGMOutputFormat = "GraphicsMagick %s %s %s http://www.GraphicsMagick.org/\n"
            + "Copyright (C) 2002-%s GraphicsMagick Group.";
    private final String gmPath = "/somewhere/gm";

    private GMProcessFactoryImpl sut;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(factory.getProcess(Matchers.<String[]> anyVararg())).thenReturn(mock(ReaderWriterProcess.class));
        when(factory.getProcess(Matchers.anyString(), eq("version"))).thenReturn(process);
        this.sut = new GMProcessFactoryImpl(factory, gmPath);
    }

    @Test
    public void builder_returnsFactory() throws Exception {
        GMProcessFactory sut = GMProcessFactoryImpl.BUILDER.buildFactory(gmPath);
        assertThat(sut.getGMPath(), equalTo(gmPath));
    }

    @Test(expected = RuntimeException.class)
    public void getVersion_chokes_onEmptyResponseFromGM() throws Exception {
        when(process.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        sut.getVersion();
    }

    @Test(expected = RuntimeException.class)
    public void getVersion_chokes_whenFailedToStartGMProcess() throws Exception {
        when(factory.getProcess(Matchers.<String[]> anyVararg())).thenThrow(new IOException());
        sut.getVersion();
    }

    @Test
    public void getVersion_callsGMProcessAndExtractVersion() throws Exception {
        when(process.getReader()).thenReturn(new BufferedReader(new StringReader(fakeGMOutput())));

        DefaultArtifactVersion result = sut.getVersion();
        assertThat(result, notNullValue());
        assertThat(result.toString(), equalTo(version));

        verify(factory).getProcess(gmPath, "version");
    }

    @Test
    public void getVersion_callsGMProcessOnlyOnce() throws Exception {
        version = "1.1.1";
        when(process.getReader()).thenReturn(new BufferedReader(new StringReader(fakeGMOutput())));

        DefaultArtifactVersion result = sut.getVersion(); // 1st call
        assertThat(sut.getVersion(), is(result)); // 2nd call

        assertThat(result, notNullValue());
        assertThat(result.toString(), equalTo(this.version));

        verify(factory, times(1)).getProcess(gmPath, "version");
    }

    @Test
    public void getGMPath_returnsPathPassedToConstructor() {
        String result = sut.getGMPath();

        assertThat(result, equalTo(gmPath));

        final String expectedPath = "new path";
        sut = new GMProcessFactoryImpl(factory, expectedPath);

        result = sut.getGMPath();
        assertThat(result, equalTo(expectedPath));
    }

    @Test
    public void getProcess_doesNotUseSafeMode_onVersion_1_3_22() throws Exception {
        getProcessVersionTest("1.3.22", GMProcessFactoryImpl.getGMCommand(gmPath));
    }

    @Test
    public void getProcess_doesNotUseSafeMode_onVersion_1_10_1() throws Exception {
        getProcessVersionTest("1.10.1", GMProcessFactoryImpl.getGMCommand(gmPath));
    }

    @Test
    public void getProcess_doesNotUseSafeMode_onVersion_2_0_0() throws Exception {
        getProcessVersionTest("2.0.0", GMProcessFactoryImpl.getGMCommand(gmPath));
    }

    @Test
    public void getProcess_usesSafeMode_onVersion_1_3_21() throws Exception {
        getProcessVersionTest("1.3.21", GMProcessFactoryImpl.getGMCommandSafeMode(gmPath));
    }

    @Test
    public void getProcess_usesSafeMode_onVersion_1_3_3() throws Exception {
        getProcessVersionTest("1.3.3", GMProcessFactoryImpl.getGMCommandSafeMode(gmPath));
    }

    @Test
    public void getProcess_usesSafeMode_onVersion_1_0_0() throws Exception {
        getProcessVersionTest("1.0.0", GMProcessFactoryImpl.getGMCommandSafeMode(gmPath));
    }

    private void getProcessVersionTest(String version, String[] gmCommand) throws Exception {
        this.version = version;
        when(process.getReader()).thenReturn(new BufferedReader(new StringReader(fakeGMOutput())));

        ReaderWriterProcess result = sut.getProcess();
        assertThat(result, notNullValue());

        verify(factory).getProcess(gmPath, "version");
        verify(factory).getProcess(gmCommand);
        verifyNoMoreInteractions(factory);
    }

    private String fakeGMOutput() {
        return String.format(fakeGMOutputFormat, version, releaseDate, quality, releaseDate.split("-")[0]);
    }
}
