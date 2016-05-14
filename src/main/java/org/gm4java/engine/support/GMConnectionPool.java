package org.gm4java.engine.support;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.gm4java.engine.GMServiceException;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Manages a pool of GraphicsMagick instances represented by {@link PooledGMConnection}. The implementation builds upon
 * {@link GenericObjectPool} from <a href="http://commons.apache.org/pool/">Apache Commons Pool</a> project.
 * 
 * @author Kenneth Xu
 * 
 */
class GMConnectionPool extends GenericObjectPool<PooledGMConnection> {
    private ReaderWriterProcess.Factory factory = ReaderWriterProcessImpl.FACTORY;

    private String[] gmCommand;
    private int evictAfterNumberOfUse = GMConnectionPoolConfig.DEFAULT_EVICT_AFTER_NUMBER_OF_USE;

    /**
     * Construct a new instance of {@linkplain GMConnectionPool}.
     */
    public GMConnectionPool(@Nonnull GMConnectionPoolConfig config, @Nonnull CommandSelector commandSelector) {
        this(new Factory(), config, commandSelector);
    }

    protected GMConnectionPool(Factory factory, GMConnectionPoolConfig config, CommandSelector commandSelector) {
        super(factory, notNull(config));
        factory.pool = this;
        evictAfterNumberOfUse = config.getEvictAfterNumberOfUse();

        notNull(commandSelector).setGmPath(config.getGMPath());
        this.gmCommand = commandSelector.gmCommand();
    }

    private static GenericObjectPool.Config notNull(GMConnectionPoolConfig config) {
        if (config == null) throw new NullPointerException("config");
        return config.getConfig();
    }
    private static CommandSelector notNull(CommandSelector commandSelector) {
        if (commandSelector == null) throw new NullPointerException("commandselector");
        return commandSelector;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to super class but ensures that there is no other checked exception except
     * {@link GMServiceException} will be thrown.
     */
    @Override
    @Nonnull
    public PooledGMConnection borrowObject() throws GMServiceException {
        try {
            return super.borrowObject();
        } catch (GMServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GMServiceException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to super class but ensures that there is no other checked exception except
     * {@link GMServiceException} will be thrown.
     * 
     */
    @Override
    public void returnObject(@Nonnull PooledGMConnection connection) throws GMServiceException {
        try {
            super.returnObject(connection);
        } catch (GMServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GMServiceException(e.getMessage(), e);
        }
    };

    /**
     * Gets the path to GraphicsMagick executable set by {@link #setGMPath(String)} or
     * {@link GMConnectionPoolConfig#DEFAULT_GM_PATH} if it was not explicitly set.
     * 
     * @return the path to GraphicsMagick executable
     */
    public String getGMPath() {
        return gmCommand[0];
    }

    /**
     * Sets the path to GraphicsMagick executable.
     * 
     * @param gmPath
     *            the path to GraphicsMagick executable
     */
    public void setGMPath(@Nonnull String gmPath) {
        if (gmPath == null) throw new NullPointerException("gmPath");
        gmCommand[0] = gmPath;
    }

    /**
     * Gets the values set by {@link #setEvictAfterNumberOfUse(int)}.
     * 
     * @return the values set by {@link #setEvictAfterNumberOfUse(int)}
     */
    public int getEvictAfterNumberOfUse() {
        return evictAfterNumberOfUse;
    }

    /**
     * Sets the number of times a GraphicsMagick process can be used to execute commands before it gets evicted and
     * destroyed. A non-positive value disables this feature. This feature is disabled by default until a positive value
     * is set.
     * <p>
     * <i>Note:</i> This setting doesn't guarantee the max number of times a GraphicsMagick process is used to execute
     * the command. The eviction and destruction can only occur when the {@link PooledGMConnection} instance is borrowed
     * from or returned back to the pool. But a client can get hold of the connection and execute as many commands as it
     * wants.
     * 
     * @param evictAfterNumberOfUse
     *            the number of times a GraphicsMagick process can execute commands before the pool mark it for eviction
     *            and destroy
     */
    public void setEvictAfterNumberOfUse(int evictAfterNumberOfUse) {
        this.evictAfterNumberOfUse = evictAfterNumberOfUse;
    }

    void setProcessFactory(ReaderWriterProcess.Factory factory) {
        this.factory = factory;
    }

    ReaderWriterProcess createProcess() throws GMServiceException {
        try {
            return factory.getProcess(gmCommand);
        } catch (IOException e) {
            throw new GMServiceException(e.getMessage(), e);
        }
    }

    private static final class Factory extends BasePoolableObjectFactory<PooledGMConnection> {

        private GMConnectionPool pool;

        @Override
        public PooledGMConnection makeObject() throws GMServiceException {
            return new PooledGMConnection(pool);
        }

        @Override
        public void destroyObject(PooledGMConnection connection) throws GMServiceException {
            connection.close();
        }

        @Override
        public boolean validateObject(PooledGMConnection connection) {
            try {
                connection.execute("ping");
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void activateObject(PooledGMConnection connection) throws GMServiceException {
            connection.ensureHealthy();
        }

        @Override
        public void passivateObject(PooledGMConnection connection) throws GMServiceException {
            connection.ensureHealthy();
        }
    }
}
