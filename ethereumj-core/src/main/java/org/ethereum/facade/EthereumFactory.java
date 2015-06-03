package org.ethereum.facade;

import org.ethereum.di.components.EthereumComponent;
import org.ethereum.di.modules.EthereumModule;
import org.ethereum.di.components.DaggerEthereumComponent;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.shh.ShhHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
public class EthereumFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private static EthereumComponent ethereumComponent;

    public static Ethereum createEthereum() {

        logger.info("capability eth version: [{}]", EthHandler.VERSION);
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);

        ethereumComponent = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule())
                .build();
        return ethereumComponent.ethereum();
    }

}
