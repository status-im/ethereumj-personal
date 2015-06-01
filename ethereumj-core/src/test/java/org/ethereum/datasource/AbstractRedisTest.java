package org.ethereum.datasource;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.redis.RedisConnection;
import org.ethereum.db.BlockStore;
import org.ethereum.db.InMemoryBlockStore;
import org.ethereum.di.components.TestEthereumComponent;
import org.ethereum.di.components.DaggerTestEthereumComponent;
import org.ethereum.di.modules.TestEthereumModule;
import org.ethereum.manager.WorldManager;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import redis.clients.jedis.Jedis;
import org.ethereum.TestContext;

import java.net.URI;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;

public abstract class AbstractRedisTest {

    static class ContextConfiguration extends TestContext {
        static {
            SystemProperties.CONFIG.setDataBaseDir("test_db/" + "RedisAll");
            SystemProperties.CONFIG.setDatabaseReset(true);
        }

        public BlockStore blockStore(SessionFactory sessionFactory){
            return new InMemoryBlockStore();
        }
    }

    TestEthereumComponent component;

    @Inject
    RedisConnection redisConnection;

    @Inject
    WorldManager worldManager;

    @Before
    public void setup() {
        component = DaggerTestEthereumComponent.builder()
                .testEthereumModule(new TestEthereumModule())
                .build();
        redisConnection = component.redisConnection();
        worldManager = component.worldManager();
    }

    @After
    public void close(){
        worldManager.close();
    }


    private Boolean connected;

    protected RedisConnection getRedisConnection() {
        return redisConnection;
    }

    protected Boolean isConnected() {
        if (connected == null) {
            String url = System.getenv(RedisConnection.REDISCLOUD_URL);
            try {
                Jedis jedis = new Jedis(new URI(url));
                connected = jedis.ping().equals("PONG");
                jedis.close();
            } catch (Exception e) {
                connected = false;
                System.out.printf("Cannot connect to '%s' Redis cloud.\n", url);
            }

            assertFalse(connected ^ redisConnection.isAvailable());
        }

        return connected;
    }

}
