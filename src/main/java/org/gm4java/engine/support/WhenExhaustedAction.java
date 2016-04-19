package org.gm4java.engine.support;

/**
 * Defines the behavior of the {@link PooledGMService#getConnection()} method when the pool is exhausted.
 * 
 * @see GMConnectionPoolConfig#getWhenExhaustedAction()
 */
public enum WhenExhaustedAction {
    /**
     * Throw a {@link NoSuchElementException}.
     */
    FAIL,

	/**
	 * Blocks until a new or idle connection is available, waiting up to
	 * <code>maxWait</code> milliseconds. If <code>maxWait</code> is negative,
	 * then {@link PooledGMService#getConnection()} blocks indefinitely.
	 */
    BLOCK,

    /**
     * Create a new connection and return it (essentially making maxActive meaningless).
     */
    GROW;
}
