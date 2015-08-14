package org.ethereum.facade;

import org.ethereum.di.components.EthereumComponent;
import org.ethereum.di.modules.EthereumModule;
import org.ethereum.di.components.DaggerEthereumComponent;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.shh.ShhHandler;

import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
public class EthereumFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private static EthereumComponent ethereumComponent;

    public static Ethereum createEthereum() {

        if (CONFIG.databaseReset()){
            FileUtil.recursiveDelete(CONFIG.databaseDir());
            logger.info("Database reset done");
        }

        return createEthereum(null);
    }

    public static Ethereum createEthereum(Class clazz) {

        logger.info("capability eth version: [{}]", EthHandler.VERSION);
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);

        ethereumComponent = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule())
                .build();
        return ethereumComponent.ethereum();
    }

}
