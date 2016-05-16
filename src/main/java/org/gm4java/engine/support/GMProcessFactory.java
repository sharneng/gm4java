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

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Internally used factory and builder interface to crease and return the GraphicsMagick process running in interactive
 * mode.
 * 
 * This is introduce to overcome the compatibility problem when GM drop -safe-mode option since 1.3.22. But this
 * interface is designed to allow future expansion. Other GM features can be exposed through this interface when needed.
 * 
 */
interface GMProcessFactory {

    /**
     * A factory interface for better test isolation.
     */
    interface Builder {
        // The factory method is thread safe.
        @Nonnull
        GMProcessFactory buildFactory(@Nonnull String gmPath);
    }

    @Nonnull
    DefaultArtifactVersion getVersion();

    @Nonnull
    String getGMPath();

    @Nonnull
    ReaderWriterProcess getProcess() throws IOException;
}
