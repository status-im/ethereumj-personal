package org.ethereum.di.components;

import org.ethereum.di.modules.EthereumModule;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.server.ChannelManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = EthereumModule.class)
public interface TestEthereumComponent {

    void inject(ChannelManager channelManager);
    void inject(WorldManager worldManager);

    Ethereum ethereum();
    EthereumListener listener();
    ChannelManager channelManager();
    WorldManager worldManager();
}