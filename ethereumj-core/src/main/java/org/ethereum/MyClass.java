package org.ethereum;

import org.ethereum.cli.CLIInterface;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.robospring.RoboSpring;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyClass {

    public static ApplicationContext context = null;

    public static void start(android.content.Context androidContext) {


        //CLIInterface.call(args);RoboSpring.getContext("applicationContext.xml");
        RoboSpring.autowire(androidContext);
        context = RoboSpring.getContext();
        Ethereum ethereum = context.getBean(Ethereum.class);
        ethereum.setContext(context);
        ethereum.connect(SystemProperties.CONFIG.activePeerIP(),
                SystemProperties.CONFIG.activePeerPort());

    }
}
