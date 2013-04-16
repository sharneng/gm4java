package org.gm4java.engine.support;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Defines the behavior of the {@link PooledGMService#getConnection()} method when the pool is exhausted.
 * 
 * @see GMConnectionPoolConfig#getWhenExhaustedAction()
 */
public enum WhenExhaustedAction {
    /**
     * Throw a {@link NoSuchElementException}.
     */
    FAIL(GenericObjectPool.WHEN_EXHAUSTED_FAIL),

    /**
     * Blocks until a new or idle connection is available. Or fail if maxWait is positive and passed.
     */
    BLOCK(GenericObjectPool.WHEN_EXHAUSTED_BLOCK),

    /**
     * Create a new connection and return it (essentially making maxActive meaningless).
     */
    GROW(GenericObjectPool.WHEN_EXHAUSTED_GROW);

    private WhenExhaustedAction(byte whenExhaustedAction) {
        this.whenExhaustedAction = whenExhaustedAction;
    }

    private final byte whenExhaustedAction;

    byte toValue() {
        return whenExhaustedAction;
    }

    static WhenExhaustedAction fromValue(byte whenExhaustedAction) {
        switch (whenExhaustedAction) {
        case GenericObjectPool.WHEN_EXHAUSTED_BLOCK:
            return WhenExhaustedAction.BLOCK;
        case GenericObjectPool.WHEN_EXHAUSTED_FAIL:
            return WhenExhaustedAction.FAIL;
        case GenericObjectPool.WHEN_EXHAUSTED_GROW:
            return WhenExhaustedAction.GROW;
        default:
            throw new IllegalArgumentException("whenExhaustedAction " + whenExhaustedAction + " not recognized.");
        }
    }
}
