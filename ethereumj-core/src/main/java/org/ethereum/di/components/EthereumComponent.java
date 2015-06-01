package org.ethereum.di.components;

import org.ethereum.di.modules.EthereumModule;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListener;

import javax.inject.Singleton;

import dagger.Component;
import org.ethereum.net.server.ChannelManager;

@Singleton
@Component(modules = EthereumModule.class)
public interface EthereumComponent {

    Ethereum ethereum();
    EthereumListener listener();
    ChannelManager channelManager();
}