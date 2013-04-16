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
// SUPPRESS CHECKSTYLE UnusedImport BECAUSE it is used in javadoc.
import org.gm4java.engine.GMConnection;

/**
 * Configuration of PooledGMService that provides a number of configuration parameters.
 * <ul>
 * <li>
 * {@link #setMaxActive <i>maxActive</i>} controls the maximum number of GM connections that can be allocated by the
 * pool (checked out to clients, or idle awaiting checkout) at a given time. When non-positive, there is no limit to the
 * number of GM connections that can be managed by the pool at one time. When {@link #setMaxActive <i>maxActive</i>} is
 * reached, the pool is said to be exhausted. The default setting for this parameter is 8.</li>
 * <li>
 * {@link #setMaxIdle <i>maxIdle</i>} controls the maximum number of GM connections that can sit idle in the pool at any
 * time. When negative, there is no limit to the number of GM connections that may be idle at one time. The default
 * setting for this parameter is 8.</li>
 * <li>
 * {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>} specifies the behavior of the
 * {@link PooledGMService#getConnection()} method when the pool is exhausted:
 * <ul>
 * <li>
 * When {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>} is {@link WhenExhaustedAction#FAIL},
 * {@link PooledGMService#getConnection()} will throw a {@link NoSuchElementException}</li>
 * <li>
 * When {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>} is {@link WhenExhaustedAction#GROW},
 * {@link PooledGMService#getConnection()} will create a new GM connection and return it (essentially making
 * {@link #setMaxActive <i>maxActive</i>} meaningless.)</li>
 * <li>
 * When {@link #setWhenExhaustedAction <i>whenExhaustedAction</i>} is {@link WhenExhaustedAction#BLOCK},
 * {@link PooledGMService#getConnection()} will block (invoke {@link Object#wait()}) until a new or idle GM connection
 * is available. If a positive {@link #setMaxWait <i>maxWait</i>} value is supplied, then
 * {@link PooledGMService#getConnection()} will block for at most that many milliseconds, after which a
 * {@link NoSuchElementException} will be thrown. If {@link #setMaxWait <i>maxWait</i>} is non-positive, the
 * {@link PooledGMService#getConnection()} method will block indefinitely.</li>
 * </ul>
 * The default <code>whenExhaustedAction</code> setting is {@link WhenExhaustedAction#BLOCK} and the default
 * <code>maxWait</code> setting is -1. By default, therefore, {@link PooledGMService#getConnection()} will block
 * indefinitely until an idle instance becomes available.</li>
 * <li>
 * When {@link #setTestOnGet <i>testOnGet</i>} is set, the pool will attempt to validate each GM connection before it is
 * returned from the {@link PooledGMService#getConnection()} method. (Using the provided factory's
 * {@link PoolableObjectFactory#validateObject} method.) Objects that fail to validate will be dropped from the pool,
 * and a different GM connection will be returned. The default setting for this parameter is <code>false.</code></li>
 * <li>
 * When {@link #setTestOnReturn <i>testOnReturn</i>} is set, the pool will attempt to validate each GM connection before
 * it is returned to the pool in the {@link #returnObject} method. (Using the provided factory's
 * {@link PoolableObjectFactory#validateObject} method.) Objects that fail to validate will be dropped from the pool.
 * The default setting for this parameter is <code>false.</code></li>
 * </ul>
 * <p>
 * Optionally, one may configure the pool to examine and possibly evict GM connections as they sit idle in the pool and
 * to ensure that a minimum number of idle GM connections are available. This is performed by an
 * "idle GM connection eviction" thread, which runs asynchronously. Caution should be used when configuring this
 * optional feature. Eviction runs contend with client threads for access to GM connections in the pool, so if they run
 * too frequently performance issues may result. The idle GM connection eviction thread may be configured using the
 * following attributes:
 * <ul>
 * <li>
 * {@link #setTimeBetweenEvictionRunsMillis <i>timeBetweenEvictionRunsMillis</i>} indicates how long the eviction thread
 * should sleep before "runs" of examining idle GM connections. When non-positive, no eviction thread will be launched.
 * The default setting for this parameter is -1 (i.e., idle GM connection eviction is disabled by default).</li>
 * <li>
 * {@link #setMinEvictableIdleTimeMillis <i>minEvictableIdleTimeMillis</i>} specifies the minimum amount of time that an
 * GM connection may sit idle in the pool before it is eligible for eviction due to idle time. When non-positive, no GM
 * connection will be dropped from the pool due to idle time alone. This setting has no effect unless
 * <code>timeBetweenEvictionRunsMillis > 0.</code> The default setting for this parameter is 30 minutes.</li>
 * <li>
 * {@link #setTestWhileIdle <i>testWhileIdle</i>} indicates whether or not idle GM connections should be validated using
 * the factory's {@link PoolableObjectFactory#validateObject} method. Objects that fail to validate will be dropped from
 * the pool. This setting has no effect unless <code>timeBetweenEvictionRunsMillis > 0.</code> The default setting for
 * this parameter is <code>false.</code></li>
 * <li>
 * {@link #setSoftMinEvictableIdleTimeMillis <i>softMinEvictableIdleTimeMillis</i>} specifies the minimum amount of time
 * a GM connection may sit idle in the pool before it is eligible for eviction by the idle GM connection evictor (if
 * any), with the extra condition that at least "minIdle" GM connection instances remain in the pool. When non-positive,
 * no GM connection will be evicted from the pool due to idle time alone. This setting has no effect unless
 * <code>timeBetweenEvictionRunsMillis > 0.</code> and it is superceded by {@link #setMinEvictableIdleTimeMillis
 * <i>minEvictableIdleTimeMillis</i>} (that is, if <code>minEvictableIdleTimeMillis</code> is positive, then
 * <code>softMinEvictableIdleTimeMillis</code> is ignored). The default setting for this parameter is -1 (disabled).</li>
 * <li>
 * {@link #setNumTestsPerEvictionRun <i>numTestsPerEvictionRun</i>} determines the number of GM connections examined in
 * each run of the idle GM connection evictor. This setting has no effect unless
 * <code>timeBetweenEvictionRunsMillis > 0.</code> The default setting for this parameter is 3.</li>
 * </ul>
 * <p>
 * <p>
 * The pool can be configured to behave as a LIFO queue with respect to idle GM connections - always returning the most
 * recently used GM connection from the pool, or as a FIFO queue, where {@link PooledGMService#getConnection()} always
 * returns the oldest GM connection in the idle GM connection pool.
 * <ul>
 * <li>
 * {@link #setLifo <i>lifo</i>} determines whether or not the pool returns idle GM connections in last-in-first-out
 * order. The default setting for this parameter is <code>true.</code></li>
 * </ul>
 * <p>
 * 
 * @author Kenneth Xu
 * 
 */
public class GMConnectionPoolConfig {

    /**
     * Default path to GraphcisMagick executable.
     */
    public static final String DEFAULT_GM_PATH = "gm";

    /**
     * Default value for the number of times a GraphicsMagick process can be used to execute commands before it gets
     * evicted from pool.
     */
    public static final int DEFAULT_EVICT_AFTER_NUMBER_OF_USE = 0;

    private GenericObjectPool.Config config = new GenericObjectPool.Config();

    private String gmPath = DEFAULT_GM_PATH;

    private int evictAfterNumberOfUse = DEFAULT_EVICT_AFTER_NUMBER_OF_USE;

    /**
     * Returns the maximum number of {@link GMConnection}s that can be allocated by the pool (checked out to clients, or
     * idle awaiting checkout) at a given time. When non-positive, there is no limit to the number of
     * {@link GMConnection}s that can be managed by the pool at one time.
     * 
     * @return the cap on the total number of {@link GMConnection} instances managed by the pool.
     * @see #setMaxActive
     */
    public int getMaxActive() {
        return config.maxActive;
    }

    /**
     * Sets the cap on the number of {@link GMConnection}s that can be allocated by the pool (checked out to clients, or
     * idle awaiting checkout) at a given time. Use a negative value for no limit.
     * 
     * @param maxActive
     *            The cap on the total number of {@link GMConnection} instances managed by the pool. Negative values
     *            mean that there is no limit to the number of {@link GMConnection}s allocated by the pool.
     * @see #getMaxActive
     */
    public void setMaxActive(int maxActive) {
        config.maxActive = maxActive;
    }

    /**
     * Returns the action to take when the {@link PooledGMService#getConnection()} method is invoked when the pool is
     * exhausted (the maximum number of "active" {@link GMConnection}s has been reached).
     * 
     * @return one of {@link WhenExhaustedAction} enum value
     * @see #setWhenExhaustedAction
     */
    public WhenExhaustedAction getWhenExhaustedAction() {
        return WhenExhaustedAction.fromValue(config.whenExhaustedAction);
    }

    /**
     * Sets the action to take when the {@link PooledGMService#getConnection()} method is invoked when the pool is
     * exhausted (the maximum number of "active" {@link GMConnection}s has been reached).
     * 
     * @param whenExhaustedAction
     *            the action to set
     * @see #getWhenExhaustedAction
     */
    public void setWhenExhaustedAction(WhenExhaustedAction whenExhaustedAction) {
        config.whenExhaustedAction = whenExhaustedAction.toValue();
    }

    /**
     * Returns the maximum amount of time (in milliseconds) the {@link PooledGMService#getConnection()} method should
     * block before throwing an exception when the pool is exhausted and the {@link #setWhenExhaustedAction
     * "when exhausted" action} is {@link WhenExhaustedAction#BLOCK}.
     * 
     * When less than or equal to 0, the {@link PooledGMService#getConnection()} method may block indefinitely.
     * 
     * @return maximum number of milliseconds to block when getting an {@link GMConnection}.
     * @see #setMaxWait
     * @see #setWhenExhaustedAction
     */
    public long getMaxWait() {
        return config.maxWait;
    }

    /**
     * Sets the maximum amount of time (in milliseconds) the {@link PooledGMService#getConnection()} method should block
     * before throwing an exception when the pool is exhausted and the {@link #setWhenExhaustedAction "when exhausted"
     * action} is {@link WhenExhaustedAction#BLOCK}.
     * 
     * When less than or equal to 0, the {@link PooledGMService#getConnection()} method may block indefinitely.
     * 
     * @param maxWait
     *            maximum number of milliseconds to block when getting an {@link GMConnection}.
     * @see #getMaxWait
     * @see #setWhenExhaustedAction
     */
    public void setMaxWait(long maxWait) {
        config.maxWait = maxWait;
    }

    /**
     * Returns the cap on the number of "idle" instances in the pool.
     * 
     * @return the cap on the number of "idle" instances in the pool.
     * @see #setMaxIdle
     */
    public int getMaxIdle() {
        return config.maxIdle;
    }

    /**
     * Sets the cap on the number of "idle" instances in the pool. If maxIdle is set too low on heavily loaded systems
     * it is possible you will see {@link GMConnection}s being destroyed and almost immediately new {@link GMConnection}
     * s being created. This is a result of the active threads momentarily returning {@link GMConnection}s faster than
     * they are requesting them them, causing the number of idle {@link GMConnection}s to rise above maxIdle. The best
     * value for maxIdle for heavily loaded system will vary but the default is a good starting point.
     * 
     * @param maxIdle
     *            The cap on the number of "idle" instances in the pool. Use a negative value to indicate an unlimited
     *            number of idle instances.
     * @see #getMaxIdle
     */
    public void setMaxIdle(int maxIdle) {
        config.maxIdle = maxIdle;
    }

    /**
     * Sets the minimum number of {@link GMConnection}s allowed in the pool before the evictor thread (if active) spawns
     * new {@link GMConnection}s. Note that no {@link GMConnection}s are created when
     * <code>numActive + numIdle >= maxActive.</code> This setting has no effect if the idle {@link GMConnection}
     * evictor is disabled (i.e. if <code>timeBetweenEvictionRunsMillis <= 0</code>).
     * 
     * @param minIdle
     *            The minimum number of {@link GMConnection}s.
     * @see #getMinIdle
     * @see #getTimeBetweenEvictionRunsMillis()
     */
    public void setMinIdle(int minIdle) {
        config.minIdle = minIdle;
    }

    /**
     * Returns the minimum number of {@link GMConnection}s allowed in the pool before the evictor thread (if active)
     * spawns new {@link GMConnection}s. (Note no {@link GMConnection}s are created when: numActive + numIdle >=
     * maxActive)
     * 
     * @return The minimum number of {@link GMConnection}s.
     * @see #setMinIdle
     */
    public int getMinIdle() {
        return config.minIdle;
    }

    /**
     * When <tt>true</tt>, {@link GMConnection}s will be validated} before being returned by the
     * {@link PooledGMService#getConnection()} method. If the {@link GMConnection} fails to validate, it will be dropped
     * from the pool, and we will attempt to get another.
     * 
     * @return <code>true</code> if {@link GMConnection}s are validated before being returned.
     * @see #setTestOnGet
     */
    public boolean getTestOnGet() {
        return config.testOnBorrow;
    }

    /**
     * When <tt>true</tt>, {@link GMConnection}s will be validated before being returned by the
     * {@link PooledGMService#getConnection()} method. If the {@link GMConnection} fails to validate, it will be dropped
     * from the pool, and we will attempt to get another.
     * 
     * @param testOnGet
     *            <code>true</code> if {@link GMConnection}s should be validated before being returned.
     * @see #getTestOnGet
     */
    public void setTestOnGet(boolean testOnGet) {
        config.testOnBorrow = testOnGet;
    }

    /**
     * When <tt>true</tt>, {@link GMConnection}s will be {@link PoolableObjectFactory#validateObject validated} before
     * being returned to the pool within the {@link #returnObject}.
     * 
     * @return <code>true</code> when {@link GMConnection}s will be validated after returned to {@link #returnObject}.
     * @see #setTestOnReturn
     */
    public boolean getTestOnReturn() {
        return config.testOnReturn;
    }

    /**
     * When <tt>true</tt>, {@link GMConnection}s will be {@link PoolableObjectFactory#validateObject validated} before
     * being returned to the pool within the {@link #returnObject}.
     * 
     * @param testOnReturn
     *            <code>true</code> so {@link GMConnection}s will be validated after returned to {@link #returnObject}.
     * @see #getTestOnReturn
     */
    public void setTestOnReturn(boolean testOnReturn) {
        config.testOnReturn = testOnReturn;
    }

    /**
     * Returns the number of milliseconds to sleep between runs of the idle {@link GMConnection} evictor thread. When
     * non-positive, no idle {@link GMConnection} evictor thread will be run.
     * 
     * @return number of milliseconds to sleep between evictor runs.
     * @see #setTimeBetweenEvictionRunsMillis
     */
    public long getTimeBetweenEvictionRunsMillis() {
        return config.timeBetweenEvictionRunsMillis;
    }

    /**
     * Sets the number of milliseconds to sleep between runs of the idle {@link GMConnection} evictor thread. When
     * non-positive, no idle {@link GMConnection} evictor thread will be run.
     * 
     * @param timeBetweenEvictionRunsMillis
     *            number of milliseconds to sleep between evictor runs.
     * @see #getTimeBetweenEvictionRunsMillis
     */
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        config.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    /**
     * Returns the max number of {@link GMConnection}s to examine during each run of the idle {@link GMConnection}
     * evictor thread (if any).
     * 
     * @return max number of {@link GMConnection}s to examine during each evictor run.
     * @see #setNumTestsPerEvictionRun
     * @see #setTimeBetweenEvictionRunsMillis
     */
    public int getNumTestsPerEvictionRun() {
        return config.numTestsPerEvictionRun;
    }

    /**
     * Sets the max number of {@link GMConnection}s to examine during each run of the idle {@link GMConnection} evictor
     * thread (if any).
     * <p>
     * When a negative value is supplied, <tt>ceil({@link #getNumIdle})/abs({@link #getNumTestsPerEvictionRun})</tt>
     * tests will be run. That is, when the value is <i>-n</i>, roughly one <i>n</i>th of the idle {@link GMConnection}s
     * will be tested per run. When the value is positive, the number of tests actually performed in each run will be
     * the minimum of this value and the number of instances idle in the pool.
     * 
     * @param numTestsPerEvictionRun
     *            max number of {@link GMConnection}s to examine during each evictor run.
     * @see #getNumTestsPerEvictionRun
     * @see #setTimeBetweenEvictionRunsMillis
     */
    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        config.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    /**
     * Returns the minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     * eviction by the idle {@link GMConnection} evictor (if any).
     * 
     * @return minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     *         eviction.
     * @see #setMinEvictableIdleTimeMillis
     * @see #setTimeBetweenEvictionRunsMillis
     */
    public long getMinEvictableIdleTimeMillis() {
        return config.minEvictableIdleTimeMillis;
    }

    /**
     * Sets the minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     * eviction by the idle {@link GMConnection} evictor (if any). When non-positive, no {@link GMConnection}s will be
     * evicted from the pool due to idle time alone.
     * 
     * @param minEvictableIdleTimeMillis
     *            minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     *            eviction.
     * @see #getMinEvictableIdleTimeMillis
     * @see #setTimeBetweenEvictionRunsMillis
     */
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        config.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    /**
     * Returns the minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     * eviction by the idle {@link GMConnection} evictor (if any), with the extra condition that at least "minIdle"
     * amount of {@link GMConnection} remain in the pool.
     * 
     * @return minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     *         eviction.
     * @see #setSoftMinEvictableIdleTimeMillis
     */
    public long getSoftMinEvictableIdleTimeMillis() {
        return config.softMinEvictableIdleTimeMillis;
    }

    /**
     * Sets the minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     * eviction by the idle {@link GMConnection} evictor (if any), with the extra condition that at least "minIdle"
     * {@link GMConnection} instances remain in the pool. When non-positive, no {@link GMConnection}s will be evicted
     * from the pool due to idle time alone.
     * 
     * @param softMinEvictableIdleTimeMillis
     *            minimum amount of time an {@link GMConnection} may sit idle in the pool before it is eligible for
     *            eviction.
     * @see #getSoftMinEvictableIdleTimeMillis
     */
    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        config.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }

    /**
     * When <tt>true</tt>, {@link GMConnection}s will be {@link PoolableObjectFactory#validateObject validated} by the
     * idle {@link GMConnection} evictor (if any). If an {@link GMConnection} fails to validate, it will be dropped from
     * the pool.
     * 
     * @return <code>true</code> when {@link GMConnection}s will be validated by the evictor.
     * @see #setTestWhileIdle
     * @see #setTimeBetweenEvictionRunsMillis
     */
    public boolean getTestWhileIdle() {
        return config.testWhileIdle;
    }

    /**
     * When <tt>true</tt>, {@link GMConnection}s will be {@link PoolableObjectFactory#validateObject validated} by the
     * idle {@link GMConnection} evictor (if any). If an {@link GMConnection} fails to validate, it will be dropped from
     * the pool.
     * 
     * @param testWhileIdle
     *            <code>true</code> so {@link GMConnection}s will be validated by the evictor.
     * @see #getTestWhileIdle
     * @see #setTimeBetweenEvictionRunsMillis
     */
    public void setTestWhileIdle(boolean testWhileIdle) {
        config.testWhileIdle = testWhileIdle;
    }

    /**
     * Whether or not the idle pool acts as a LIFO queue. True means that {@link PooledGMService#getConnection()}
     * returns the most recently used ("last in") idle {@link GMConnection} in the pool (if there are idle instances
     * available). False means that the pool behaves as a FIFO queue - {@link GMConnection} are taken from the idle pool
     * in the order that they are returned to the pool.
     * 
     * @return {@code true} if the pool is configured to act as a LIFO queue
     */
    public boolean isLifo() {
        return config.lifo;
    }

    /**
     * Sets the LIFO property of the pool. True means that {@link PooledGMService#getConnection()} returns the most
     * recently used ("last in") idle {@link GMConnection} in the pool (if there are idle instances available). False
     * means that the pool behaves as a FIFO queue - {@link GMConnection} are taken from the idle pool in the order that
     * they are returned to the pool.
     * 
     * @param lifo
     *            the new value for the LIFO property
     */
    public void setLifo(boolean lifo) {
        config.lifo = lifo;
    }

    /**
     * Gets the path to GraphicsMagick executable.
     * 
     * @return the path to GraphicsMagick executable.
     */
    public String getGMPath() {
        return gmPath;
    }

    /**
     * Sets the path to GraphicsMagick executable.
     * 
     * @param gmPath
     *            The path for the gm executable.
     */
    public void setGMPath(final String gmPath) {
        this.gmPath = gmPath;
    }

    /**
     * Get the number of times a GraphicsMagick process can be used to execute commands before it gets evicted and
     * destroyed. A non-positive value disables this feature. This feature is disabled by default until a positive value
     * 
     * @return the number of times a GraphicsMagick process can be used to execute commands before it gets evicted and
     *         destroyed. A non-positive value disables this feature. This feature is disabled by default until a
     *         positive value
     */
    public int getEvictAfterNumberOfUse() {
        return evictAfterNumberOfUse;
    }

    /**
     * Set the number of times a GraphicsMagick process can be used to execute commands before it gets evicted and
     * destroyed. A non-positive value disables this feature. This feature is disabled by default until a positive value
     * <p>
     * <i>Note:</i> This setting doesn't guarantee the max number of times a GraphicsMagick process is used to execute
     * the command. The eviction and destruction can only occur when the {@link org.gm4java.engine.GMConnection}
     * instance is obtained from or returned back to the pool. But a client can get hold of the connection and execute
     * as many commands as it wants.
     * 
     * @param evictAfterNumberOfUse
     *            the number of times a GraphicsMagick process can be used to execute commands before it gets evicted
     *            and destroyed.
     */
    public void setEvictAfterNumberOfUse(final int evictAfterNumberOfUse) {
        this.evictAfterNumberOfUse = evictAfterNumberOfUse;
    }

    GenericObjectPool.Config getConfig() {
        return config;
    }
}
