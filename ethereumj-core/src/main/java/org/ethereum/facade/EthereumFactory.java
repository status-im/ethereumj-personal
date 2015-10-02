package org.ethereum.facade;

import org.ethereum.di.components.EthereumComponent;
import org.ethereum.di.modules.EthereumModule;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.di.components.DaggerEthereumComponent;

import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.sync.PeersPool;
import org.ethereum.util.BuildInfo;
import org.ethereum.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Singleton
public class EthereumFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private static EthereumComponent ethereumComponent;

    public static Ethereum createEthereum() {

        logger.info("Running {},  core version: {}-{}", CONFIG.genesisInfo(), CONFIG.projectVersion(), CONFIG.projectVersionModifier());
        BuildInfo.printInfo();

        if (CONFIG.databaseReset()){
            FileUtil.recursiveDelete(CONFIG.databaseDir());
            logger.info("Database reset done");
        }

        return createEthereum(null);
    }

    public static Ethereum createEthereum(Class clazz) {

        if (logger.isInfoEnabled()) {
            StringBuilder versions = new StringBuilder();
            for (EthVersion v : EthVersion.supported()) {
                versions.append(v.getCode()).append(", ");
            }
            versions.delete(versions.length() - 2, versions.length());
            logger.info("capability eth version: [{}]", versions);
        }
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);
        logger.info("capability bzz version: [{}]", BzzHandler.VERSION);

        ethereumComponent = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule())
                .build();
        Ethereum ethereum = ethereumComponent.ethereum();
        PeersPool peersPool = ethereumComponent.peersPool();
        peersPool.setEthereum(ethereum);
        ChannelManager channelManager = ethereumComponent.channelManager();
        channelManager.setEthereum(ethereum);
        return ethereum;
    }

}
