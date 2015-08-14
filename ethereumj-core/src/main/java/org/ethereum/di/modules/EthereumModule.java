package org.ethereum.di.modules;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.Wallet;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.datasource.KeyValueDataSource;
import org.ethereum.datasource.LevelDbDataSource;
import org.ethereum.datasource.mapdb.MapDBDataSource;
import org.ethereum.datasource.mapdb.MapDBFactory;
import org.ethereum.datasource.mapdb.MapDBFactoryImpl;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
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
import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.eth.SyncManager;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.net.peerdiscovery.DiscoveryChannel;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.peerdiscovery.WorkerThread;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.net.rlpx.discover.PeerConnectionTester;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.EthereumChannelInitializer;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.rlpx.MessageCodec;
import org.ethereum.vm.ProgramInvokeFactory;
import org.ethereum.vm.ProgramInvokeFactoryImpl;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.ethereum.db.IndexedBlockStore.BLOCK_INFO_SERIALIZER;

@Module
public class EthereumModule {

    public EthereumModule() {

    }

    @Provides
    @Singleton
    public Ethereum provideEthereum(WorldManager worldManager, AdminInfo adminInfo, ChannelManager channelManager,
                             BlockLoader blockLoader, Provider<PeerClient> peerClientProvider, EthereumListener listener) {
        return new EthereumImpl(worldManager, adminInfo, channelManager, blockLoader, peerClientProvider, listener);
    }

    @Provides
    @Singleton
    public WorldManager provideWorldManager(Blockchain blockchain, Repository repository, Wallet wallet, PeerDiscovery peerDiscovery
            ,BlockStore blockStore, ChannelManager channelManager, AdminInfo adminInfo, EthereumListener listener, NodeManager nodeManager, SyncManager syncManager) {
        return new WorldManager(blockchain, repository, wallet, peerDiscovery, blockStore, channelManager, adminInfo, listener, nodeManager, syncManager);
    }

    @Provides
    @Singleton
    public Blockchain provideBlockchain(BlockStore blockStore, Repository repository,
                                 Wallet wallet, AdminInfo adminInfo,
                                 EthereumListener listener, ChannelManager channelManager) {
        return new BlockchainImpl(blockStore, repository, wallet, adminInfo, listener, channelManager);
    }

    @Provides
    @Singleton
    public BlockStore provideBlockStore() {
        return createBlockStore();
    }

    protected BlockStore createBlockStore() {
        String database = SystemProperties.CONFIG.databaseDir();

        String blocksIndexFile = database + "/blocks/index";
        File dbFile = new File(blocksIndexFile);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();

        DB indexDB = DBMaker.fileDB(dbFile)
                .closeOnJvmShutdown()
                .make();

        Map<Long, List<IndexedBlockStore.BlockInfo>> indexMap = indexDB.hashMapCreate("index")
                .keySerializer(Serializer.LONG)
                .valueSerializer(BLOCK_INFO_SERIALIZER)
                .counterEnable()
                .makeOrGet();

        KeyValueDataSource blocksDB = new LevelDbDataSource("blocks");
        blocksDB.init();


        IndexedBlockStore cache = new IndexedBlockStore();
        cache.init(new HashMap<Long, List<IndexedBlockStore.BlockInfo>>(), new HashMapDB(), null, null);

        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        indexedBlockStore.init(indexMap, blocksDB, cache, indexDB);


        return indexedBlockStore;
    }

    @Provides
    @Singleton
    public Repository provideRepository() {
        MapDBDataSource detailsDS = new MapDBDataSource();
        MapDBDataSource stateDS = new MapDBDataSource();
        return new RepositoryImpl(detailsDS, stateDS);
    }

    @Provides
    @Singleton
    public AdminInfo provideAdminInfo() {
        return new AdminInfo();
    }

    @Provides
    @Singleton
    public EthereumListener provideEthereumListener() {
        return new CompositeEthereumListener();
    }

    @Provides
    @Singleton
    public PeerDiscovery providePeerDiscovery() {
        return new PeerDiscovery();
    }

    @Provides
    @Singleton
    public ChannelManager provideChannelManager() {
        return new ChannelManager();
    }

    @Provides
    @Singleton
    public BlockLoader provideBlockLoader(Blockchain blockchain) {
        return createBlockLoader(blockchain);
    }

    protected BlockLoader createBlockLoader(Blockchain blockchain) {
        return new BlockLoader(blockchain);
    }

    @Provides
    @Singleton
    public ProgramInvokeFactory provideProgramInvokeFactory() {
        return new ProgramInvokeFactoryImpl();
    }

    @Provides
    public EthHandler provideEthHandler(Blockchain blockchain, EthereumListener listener, Wallet wallet) {
        return new EthHandler(blockchain, listener, wallet);
    }

    @Provides
    public ShhHandler provideShhHandler(EthereumListener listener) {
        return new ShhHandler(listener);
    }

    @Provides
    public P2pHandler provideP2pHandler(PeerDiscovery peerDiscovery, EthereumListener listener) {
        return new P2pHandler(peerDiscovery, listener);
    }

    @Provides
    public MessageCodec provideMessageCodec(WorldManager worldManager) {
        return new MessageCodec(worldManager);
    }

    @Provides
    public PeerClient providePeerClient(EthereumListener listener, ChannelManager channelManager,
                                 Provider<EthereumChannelInitializer> ethereumChannelInitializerProvider) {
        return new PeerClient(listener, channelManager, ethereumChannelInitializerProvider);
    }

    @Provides
    public MessageQueue provideMessageQueue(EthereumListener listener) {
        return new MessageQueue(listener);
    }

    @Provides
    public WorkerThread provideWorkerThread(Provider<DiscoveryChannel> discoveryChannelProvider) {
        return new WorkerThread(discoveryChannelProvider);
    }

    @Provides
    public String provideRemoteId() {
        return "";
    }

    @Provides
    @Singleton
    public SyncManager provideSyncManager(NodeManager nodeManager, EthereumListener ethereumListener) {

        return new SyncManager(nodeManager, ethereumListener);
    }

    @Provides
    @Singleton
    public PeerConnectionTester providePeerConnectionTester() {
        return new PeerConnectionTester();
    }

    @Provides
    @Singleton
    public MapDBFactory provideMapDBFactory() {
        return new MapDBFactoryImpl();
    }
}
