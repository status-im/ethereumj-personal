package org.ethereum;


import org.ethereum.config.SystemProperties;
import org.ethereum.di.components.DaggerEthereumComponent;
import org.ethereum.di.components.EthereumComponent;
import org.ethereum.di.modules.EthereumModule;
import org.ethereum.facade.Ethereum;
//import org.robospring.RoboSpring;
//import org.springframework.context.ApplicationContext;

public class EthereumFactory {

//    public static ApplicationContext context = null;
    public static EthereumComponent ethereumComponent = null;

    public static Ethereum getEthereum(android.content.Context androidContext) {

//        RoboSpring.autowire(androidContext);
//        context = RoboSpring.getContext();
//        Ethereum ethereum = context.getBean(org.ethereum.facade.Ethereum.class);
//        ethereum.setContext(context);
        ethereumComponent = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule(androidContext))
                .build();
        return ethereumComponent.ethereum();
    }
}
