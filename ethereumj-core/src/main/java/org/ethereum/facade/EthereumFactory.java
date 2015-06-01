package org.ethereum.facade;

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

    public static Ethereum createEthereum() {
        return createEthereum(DefaultConfig.class);
    }

    public static Ethereum createEthereum(Class clazz) {

        logger.info("capability eth version: [{}]", EthHandler.VERSION);
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);

        return null;
    }

}
