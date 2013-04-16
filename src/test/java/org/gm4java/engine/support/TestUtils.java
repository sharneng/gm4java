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

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.VarargMatcher;

/**
 * Utilities method to help test.
 * 
 * @author Kenneth Xu
 * 
 */
final class TestUtils {
    public static final String EOL = System.getProperty("line.separator");

    private TestUtils() {
    }

    /**
     * Assert given factory's {@link ReaderWriterProcess.Factory#getProcess(String...)} method was called with given
     * command as the first parameter.
     * 
     * @param factory
     *            the factory whose method to be checked
     * @param command
     *            the expected command
     * @throws Exception
     *             when any error occurs during test.
     */
    public static void verifyFactoryCalledWithGMPath(ReaderWriterProcess.Factory factory, final String command)
            throws Exception {
        verify(factory).getProcess(argThat(new VarargArgumentMatcher<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean matches(String[] arguments) {
                return command.equals(arguments[0]);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(command).appendText(", <0 to many any string>");
            }
        }));
    }

    private abstract static class VarargArgumentMatcher<T> extends ArgumentMatcher<T[]> implements VarargMatcher {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        @Override
        public final boolean matches(Object argument) {
            return matches((T[]) argument);
        }

        public abstract boolean matches(T[] arguments);
    }
}
