package org.ethereum.di.modules;

import android.content.Context;

import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Wallet;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.db.BlockStore;
import org.ethereum.db.InMemoryBlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.facade.Repository;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.BlockLoader;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.BlockQueue;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.vm.ProgramInvokeFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Adrian Tiberius on 20.05.2015.
 */
@Module
public class EthereumModule {

    private Context context;

    public EthereumModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    Ethereum provideEthereum(WorldManager worldManager, AdminInfo adminInfo, ChannelManager channelManager,
                             BlockLoader blockLoader, PeerClient backupPeerClient) {
        return new EthereumImpl(worldManager, adminInfo, channelManager, blockLoader, backupPeerClient);
    }

    @Provides
    @Singleton
    WorldManager provideWorldManager(Blockchain blockchain, Repository repository, Wallet wallet, PeerDiscovery peerDiscovery
            ,BlockStore blockStore, ChannelManager channelManager, EthereumListener listener) {
        return new WorldManager(blockchain, repository, wallet, peerDiscovery, blockStore, channelManager, listener);
    }

    @Provides
    @Singleton
    Blockchain provideBlockchain(BlockStore blockStore, Repository repository,
                                 Wallet wallet, AdminInfo adminInfo,
                                 EthereumListener listener, ChannelManager channelManager) {
        return new BlockchainImpl(blockStore, repository, wallet, adminInfo, listener, channelManager);
    }

    @Provides
    @Singleton
    BlockStore provideBlockStore() {
        return new InMemoryBlockStore();
    }

    @Provides
    @Singleton
    Repository provideRepository() {
        LevelDbDataSource detailsDS = new LevelDbDataSource();
        detailsDS.setContext(context);
        LevelDbDataSource stateDS = new LevelDbDataSource();
        stateDS.setContext(context);
        return new RepositoryImpl(detailsDS, stateDS);
    }

    @Provides
    @Singleton
    AdminInfo provideAdminInfo() {
        return new AdminInfo();
    }

    @Provides
    @Singleton
    EthereumListener provideEthereumListener() {
        return new CompositeEthereumListener();
    }

    @Provides
    @Singleton
    PeerDiscovery providePeerDiscovery() {
        return new PeerDiscovery();
    }

    @Provides
    EthHandler provideEthHandler() {
        return new EthHandler();
    }

    @Provides
    ShhHandler provideShhHandler() {
        return new ShhHandler();
    }

    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }

    @Provides
    String provideRemoteId() {
        return "e3d09d2f829950b5f3f82d1bddb6f5388bff2f2cca880fa47923df4d8129e8c9b5ba5d4371efcffc416b0cefe20cb55b81b2b71183464713a86e60b423b77947";
    }


}
