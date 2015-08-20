package org.ethereum.android.di.modules;

import android.content.Context;

import org.ethereum.android.db.InMemoryBlockStore;
import org.ethereum.android.db.OrmLiteBlockStoreDatabase;
import org.ethereum.core.Blockchain;
import org.ethereum.android.manager.BlockLoader;
import org.ethereum.core.Repository;
import org.ethereum.android.datasource.LevelDbDataSource;
import org.ethereum.db.BlockStore;
import org.ethereum.db.RepositoryImpl;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.PeerServer;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EthereumModule extends org.ethereum.di.modules.EthereumModule {

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

    @Override
    protected Ethereum createEthereum(WorldManager worldManager, AdminInfo adminInfo, ChannelManager channelManager,
                                      org.ethereum.manager.BlockLoader blockLoader, Provider<PeerClient> peerClientProvider, EthereumListener listener, PeerServer peerServer) {
        return new org.ethereum.android.Ethereum(worldManager, adminInfo, channelManager, blockLoader, peerClientProvider, listener, peerServer);
    }

    @Override
    protected Repository createRepository() {
        LevelDbDataSource detailsDS = new LevelDbDataSource();
        LevelDbDataSource stateDS = new LevelDbDataSource();
        return new RepositoryImpl(detailsDS, stateDS);
    }

    @Override
    protected BlockStore createBlockStore() {

        OrmLiteBlockStoreDatabase database = OrmLiteBlockStoreDatabase.getHelper(context);
        return new InMemoryBlockStore(database, storeAllBlocks);
    }
    @Override
    protected org.ethereum.manager.BlockLoader createBlockLoader(Blockchain blockchain) {
        return (org.ethereum.manager.BlockLoader) new BlockLoader(blockchain);
    }
/*
    @Override
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
*/
    @Provides
    @Singleton
    public Context provideContext() {
        return context;
    }
}
