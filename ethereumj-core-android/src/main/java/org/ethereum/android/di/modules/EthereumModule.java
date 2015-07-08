package org.ethereum.android.di.modules;

import android.content.Context;

import org.ethereum.android.datasource.LevelDbDataSource;
import org.ethereum.android.db.InMemoryBlockStore;
import org.ethereum.android.db.OrmLiteBlockStoreDatabase;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Wallet;
import org.ethereum.db.BlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.facade.Repository;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.android.manager.BlockLoader;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.peerdiscovery.DiscoveryChannel;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.peerdiscovery.WorkerThread;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.EthereumChannelInitializer;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.wire.MessageCodec;
import org.ethereum.vm.ProgramInvokeFactory;
import org.ethereum.vm.ProgramInvokeFactoryImpl;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EthereumModule {

    private Context context;

    boolean storeAllBlocks;

    public EthereumModule(Context context) {

        this.context = context;
        this.storeAllBlocks = false;
    }

    public EthereumModule(Context context,boolean storeAllBlocks) {

        this.context = context;
        this.storeAllBlocks = storeAllBlocks;
    }

    @Provides
    @Singleton
    Ethereum provideEthereum(WorldManager worldManager, AdminInfo adminInfo, ChannelManager channelManager,
                             BlockLoader blockLoader, Provider<PeerClient> peerClientProvider, EthereumListener listener) {
        return new EthereumImpl(worldManager, adminInfo, channelManager, blockLoader, peerClientProvider, listener);
    }

    @Provides
    @Singleton
    WorldManager provideWorldManager(Blockchain blockchain, Repository repository, Wallet wallet, PeerDiscovery peerDiscovery
            ,BlockStore blockStore, ChannelManager channelManager, AdminInfo adminInfo, EthereumListener listener) {
        return new WorldManager(blockchain, repository, wallet, peerDiscovery, blockStore, channelManager, adminInfo, listener);
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
        OrmLiteBlockStoreDatabase database = OrmLiteBlockStoreDatabase.getHelper(context);
        return new InMemoryBlockStore(database, storeAllBlocks);
    }

    @Provides
    @Singleton
    Repository provideRepository() {
        LevelDbDataSource detailsDS = new LevelDbDataSource();
        LevelDbDataSource stateDS = new LevelDbDataSource();
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
    @Singleton
    ChannelManager provideChannelManager(EthereumListener listener) {
        return new ChannelManager(listener);
    }

    @Provides
    @Singleton
    BlockLoader provideBlockLoader(Blockchain blockchain) {
        return new BlockLoader(blockchain);
    }

    @Provides
    @Singleton
    ProgramInvokeFactory provideProgramInvokeFactory() {
        return new ProgramInvokeFactoryImpl();
    }

    @Provides
    EthHandler provideEthHandler(Blockchain blockchain, EthereumListener listener, Wallet wallet) {
        return new EthHandler(blockchain, listener, wallet);
    }

    @Provides
    ShhHandler provideShhHandler(WorldManager worldManager) {
        return new ShhHandler(worldManager);
    }

    @Provides
    P2pHandler provideP2pHandler(PeerDiscovery peerDiscovery, EthereumListener listener) {
        return new P2pHandler(peerDiscovery, listener);
    }

    @Provides
    MessageCodec provideMessageCodec(EthereumListener listener) {
        return new MessageCodec(listener);
    }

    @Provides
    PeerClient providePeerClient(EthereumListener listener, ChannelManager channelManager,
                                 Provider<EthereumChannelInitializer> ethereumChannelInitializerProvider) {
        return new PeerClient(listener, channelManager, ethereumChannelInitializerProvider);
    }

    @Provides
    MessageQueue provideMessageQueue(EthereumListener listener) {
        return new MessageQueue(listener);
    }

    @Provides
    WorkerThread provideWorkerThread(Provider<DiscoveryChannel> discoveryChannelProvider) {
        return new WorkerThread(discoveryChannelProvider);
    }

    @Provides
    String provideRemoteId() {
        return SystemProperties.CONFIG.activePeerNodeid();
    }

    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }
}
