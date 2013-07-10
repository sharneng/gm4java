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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.gm4java.engine.GMConnection;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link BasicGMConnection}.
 * 
 * @author Kenneth Xu
 * 
 */
public class BasicGMConnectionTest extends AbstractGMConnectionTest {
    private BasicGMConnection sut;

    @Before
    public void setup() throws Exception {
        super.setup();
        sut = new BasicGMConnection(process);
    }

    @Test
    @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void constructor_chokes_onNullPath() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("process");
        new BasicGMConnection(null);
    }

    @Override
    protected GMConnection sut() {
        return sut;
    }
}
