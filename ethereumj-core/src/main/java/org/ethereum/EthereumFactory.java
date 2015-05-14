package org.ethereum;


import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.robospring.RoboSpring;
import org.springframework.context.ApplicationContext;

/**
 * Created by userica on 06.05.2015.
 */
public class EthereumFactory {

    public static ApplicationContext context = null;

    public static Ethereum getEthereum(android.content.Context androidContext) {

        RoboSpring.autowire(androidContext);
        context = RoboSpring.getContext();
        Ethereum ethereum = context.getBean(org.ethereum.facade.Ethereum.class);
        ethereum.setContext(context);

        return ethereum;
    }
}
