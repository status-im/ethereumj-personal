package org.ethereum.android.di.components;

import android.content.Context;

import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.android.Ethereum;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.sync.PeersPool;
import org.ethereum.validator.ParentBlockHeaderValidator;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = EthereumModule.class)
public interface EthereumComponent {

    Context context();
    Ethereum ethereum();
    ChannelManager channelManager();
    WorldManager worldManager();
    ParentBlockHeaderValidator parentBlockHeaderValidator();
    PeersPool peersPool();
}
