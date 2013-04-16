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

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Configuration of PooledGMService.
 * 
 * @author Kenneth Xu
 * 
 */
public class GMConnectionPoolConfig extends GenericObjectPool.Config {
    /**
     * Default path to GraphcisMagick executable is simply "gm".
     */
    public static final String DEFAULT_GM_PATH = Constants.DEFAULT_GM_PATH;

    /**
     * Default value for the number of times a GraphicsMagick process can be used to execute commands before it gets
     * evicted from pool.
     */
    public static final int DEFAULT_EVICT_AFTER_NUMBER_OF_USE = 0;

    /**
     * The path to GraphicsMagick executable.
     * 
     */
    // SUPPRESS CHECKSTYLE VisibilityModifier BECAUSE trying to confirm with parent class style
    public String gmPath = DEFAULT_GM_PATH;

    /**
     * The number of times a GraphicsMagick process can be used to execute commands before it gets evicted and
     * destroyed. A non-positive value disables this feature. This feature is disabled by default until a positive value
     * is set.
     * <p>
     * <i>Note:</i> This setting doesn't guarantee the max number of times a GraphicsMagick process is used to execute
     * the command. The eviction and destruction can only occur when the {@link org.gm4java.engine.GMConnection}
     * instance is obtained from or returned back to the pool. But a client can get hold of the connection and execute
     * as many commands as it wants.
     */
    // SUPPRESS CHECKSTYLE VisibilityModifier BECAUSE trying to confirm with parent class style
    public int evictAfterNumberOfUse = DEFAULT_EVICT_AFTER_NUMBER_OF_USE;
}
