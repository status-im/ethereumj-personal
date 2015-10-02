package org.ethereum.di.modules;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Wallet;
import org.ethereum.datasource.mapdb.MapDBDataSource;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.db.BlockStore;
import org.ethereum.db.InMemoryBlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.core.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.core.Repository;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.BlockLoader;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.MessageQueue;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.eth.handler.Eth60;
import org.ethereum.net.eth.handler.Eth61;
import org.ethereum.net.eth.handler.Eth62;
import org.ethereum.net.eth.handler.EthHandlerFactory;
import org.ethereum.net.eth.handler.EthHandlerFactoryImpl;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.peerdiscovery.DiscoveryChannel;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.peerdiscovery.WorkerThread;
import org.ethereum.net.rlpx.MessageCodec;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.PeerConnectionTester;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.EthereumChannelInitializer;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.sync.PeersPool;
import org.ethereum.sync.SyncManager;
import org.ethereum.sync.SyncQueue;
import org.ethereum.validator.BlockHeaderRule;
import org.ethereum.validator.BlockHeaderValidator;
import org.ethereum.validator.DependentBlockHeaderRule;
import org.ethereum.validator.DifficultyRule;
import org.ethereum.validator.ExtraDataRule;
import org.ethereum.validator.GasLimitRule;
import org.ethereum.validator.GasValueRule;
import org.ethereum.validator.ParentBlockHeaderValidator;
import org.ethereum.validator.ParentGasLimitRule;
import org.ethereum.validator.ParentNumberRule;
import org.ethereum.validator.ProofOfWorkRule;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static java.util.Arrays.asList;
import static org.ethereum.config.SystemProperties.CONFIG;

@Module
public class EthereumModule {

    public EthereumModule() {

    }

    @Provides
    @Singleton
    Ethereum provideEthereum(WorldManager worldManager, AdminInfo adminInfo, ChannelManager channelManager,
                             BlockLoader blockLoader, ProgramInvokeFactory programInvokeFactory, Provider<PeerClient> peerClientProvider) {
        return new EthereumImpl(worldManager, adminInfo, channelManager, blockLoader, programInvokeFactory, peerClientProvider);
    }

    @Provides
    @Singleton
    WorldManager provideWorldManager(EthereumListener listener, Blockchain blockchain, Repository repository, Wallet wallet, PeerDiscovery peerDiscovery
            ,BlockStore blockStore, ChannelManager channelManager, AdminInfo adminInfo, NodeManager nodeManager, SyncManager syncManager) {
        return new WorldManager(listener, blockchain, repository, wallet, peerDiscovery, blockStore, channelManager, adminInfo, nodeManager, syncManager);
    }

    @Provides
    @Singleton
    Blockchain provideBlockchain(BlockStore blockStore, Repository repository,
                                 Wallet wallet, AdminInfo adminInfo,
                                 ParentBlockHeaderValidator parentHeaderValidator, EthereumListener listener) {
        return new BlockchainImpl(blockStore, repository, wallet, adminInfo, parentHeaderValidator, listener);
    }

    @Provides
    @Singleton
    BlockStore provideBlockStore() {
        return new InMemoryBlockStore();
    }

    @Provides
    @Singleton
    Repository provideRepository() {
        MapDBDataSource detailsDS = new MapDBDataSource();
        MapDBDataSource stateDS = new MapDBDataSource();
        return new RepositoryImpl(detailsDS, stateDS);
    }

    @Provides
    @Singleton
    SyncManager provideSyncManagery(Blockchain blockchain, SyncQueue queue, NodeManager nodeManager, EthereumListener ethereumListener
            , PeersPool pool) {
        return new SyncManager(blockchain, queue, nodeManager, ethereumListener, pool);
    }

    @Provides
    @Singleton
    SyncQueue provideSyncQueue(Blockchain blockchain, BlockHeaderValidator headerValidator) {
        return new SyncQueue(blockchain, headerValidator);
    }

    @Provides
    BlockHeaderValidator provideBlockHeaderValidator() {
        List<BlockHeaderRule> rules = new ArrayList<>(asList(
                new GasValueRule(),
                new ExtraDataRule(),
                new ProofOfWorkRule()
        ));

        if (!CONFIG.isFrontier()) {
            rules.add(new GasLimitRule());
        }

        return new BlockHeaderValidator(rules);
    }

    @Provides
    @Singleton
    PeersPool providePeersPool() {
        return new PeersPool();
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
    ChannelManager provideChannelManager(EthereumListener listener, SyncManager syncManager, NodeManager nodeManager) {
        return new ChannelManager(listener, syncManager, nodeManager);
    }

    @Provides
    @Singleton
    NodeManager provideNodeManager(PeerConnectionTester peerConnectionManager, MapDBFactory mapDBFactory) {
        return new NodeManager(peerConnectionManager, mapDBFactory);
    }

    @Provides
    @Singleton
    PeerConnectionTester providePeerConnectionTester() {
        return new PeerConnectionTester();
    }

    @Provides
    @Singleton
    MapDBFactory provideMapDBFactory() {
        return new MapDBFactoryImpl();
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
    ShhHandler provideShhHandler(EthereumListener listener) {
        return new ShhHandler(listener);
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
        return SystemProperties.CONFIG.peerActive().get(0).getHexId();
    }

    @Provides
    Eth60 provideEth60() { return new Eth60(); }

    @Provides
    Eth61 provideEth61() { return new Eth61(); }

    @Provides
    Eth62 provideEth62(Blockchain blockchain, SyncQueue queue, WorldManager worldManager) { return new Eth62(blockchain, queue, worldManager); }

    @Provides
    @Singleton
    EthHandlerFactory provideEthHandlerFactory(Provider<Eth60> eth60Provider, Provider<Eth61> eth61Provider, Provider<Eth62> eth62Provider) {
        return new EthHandlerFactoryImpl(eth60Provider, eth61Provider, eth62Provider);
    }

    @Provides
    @Singleton
    ParentBlockHeaderValidator provideParentBlockHeaderValidator() {

        List<DependentBlockHeaderRule> rules = new ArrayList<>(asList(
                new ParentNumberRule(),
                new DifficultyRule()
        ));

        if (!CONFIG.isFrontier()) {
            rules.add(new ParentGasLimitRule());
        }

        return new ParentBlockHeaderValidator(rules);
    }
}
