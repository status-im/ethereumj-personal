package org.ethereum.manager;

import org.ethereum.core.*;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Repository;
import org.ethereum.listener.EthereumListener;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.server.ChannelManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import java.util.ArrayList;

import static org.ethereum.config.SystemProperties.CONFIG;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * WorldManager is a singleton containing references to different parts of the system.
 *
 * @author Roman Mandeleil
 * @since 01.06.2014
 */
@Singleton
public class WorldManager {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private Blockchain blockchain;

    private Repository repository;

    private Wallet wallet;

    private PeerClient activePeer;

    private PeerDiscovery peerDiscovery;

    private BlockStore blockStore;

    private ChannelManager channelManager;

    private AdminInfo adminInfo;


    private EthereumListener listener;

    @Inject
	public WorldManager(Blockchain blockchain, Repository repository, Wallet wallet, PeerDiscovery peerDiscovery
                        ,BlockStore blockStore, ChannelManager channelManager, AdminInfo adminInfo, EthereumListener listener) {
        logger.info("World manager instantiated");
        this.blockchain = blockchain;
        this.repository = repository;
        this.wallet = wallet;
        this.peerDiscovery = peerDiscovery;
        this.blockStore = blockStore;
        this.channelManager = channelManager;
		this.adminInfo = adminInfo;
        this.listener = listener;

        this.init();
    }

    public void init() {
        byte[] cowAddr = HashUtil.sha3("cow".getBytes());
        wallet.importKey(cowAddr);

        String secret = CONFIG.coinbaseSecret();
        byte[] cbAddr = HashUtil.sha3(secret.getBytes());
        wallet.importKey(cbAddr);
    }

    public void addListener(EthereumListener listener) {
        logger.info("Ethereum listener added");
        ((CompositeEthereumListener) this.listener).addListener(listener);
    }

    public void startPeerDiscovery() {
        if (!peerDiscovery.isStarted())
            peerDiscovery.start();
    }

    public void stopPeerDiscovery() {
        if (peerDiscovery.isStarted())
            peerDiscovery.stop();
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public PeerDiscovery getPeerDiscovery() {
        return peerDiscovery;
    }

    public EthereumListener getListener() {
        return listener;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Repository getRepository() {
        return repository;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setActivePeer(PeerClient peer) {
        this.activePeer = peer;
    }

    public PeerClient getActivePeer() {
        return activePeer;
    }


    public boolean isBlockchainLoading() {
        return blockchain.getQueue().size() > 2;
    }

    public void loadBlockchain() {

        Block bestBlock = blockStore.getBestBlock();
        if (bestBlock == null) {
            logger.info("DB is empty - adding Genesis");

            Genesis genesis = (Genesis)Genesis.getInstance();
            for (ByteArrayWrapper key : genesis.getPremine().keySet()) {
                repository.createAccount(key.getData());
                repository.addBalance(key.getData(), genesis.getPremine().get(key).getBalance());
            }

            blockStore.saveBlock(Genesis.getInstance(), new ArrayList<TransactionReceipt>());

            blockchain.setBestBlock(Genesis.getInstance());
            blockchain.setTotalDifficulty(Genesis.getInstance().getCumulativeDifficulty());

            listener.onBlock(Genesis.getInstance());
            repository.dumpState(Genesis.getInstance(), 0, 0, null);

            logger.info("Genesis block loaded");
        } else {

            blockchain.setBestBlock(bestBlock);

            BigInteger totalDifficulty = blockStore.getTotalDifficulty();
            blockchain.setTotalDifficulty(totalDifficulty);

            logger.info("*** Loaded up to block [{}] totalDifficulty [{}] with stateRoot [{}]",
                    blockchain.getBestBlock().getNumber(),
                    blockchain.getTotalDifficulty().toString(),
                    Hex.toHexString(blockchain.getBestBlock().getStateRoot()));
        }

        if (CONFIG.rootHashStart() != null) {

            // update world state by dummy hash
            byte[] rootHash = Hex.decode(CONFIG.rootHashStart());
            logger.info("Loading root hash from property file: [{}]", CONFIG.rootHashStart());
            this.repository.syncToRoot(rootHash);

        } else {

            // Update world state to latest loaded block from db
            this.repository.syncToRoot(blockchain.getBestBlock().getStateRoot());
        }

/* todo: return it when there is no state conflicts on the chain
        boolean dbValid = this.repository.getWorldState().validate() || bestBlock.isGenesis();
        if (!dbValid){
            logger.error("The DB is not valid for that blockchain");
            System.exit(-1); //  todo: reset the repository and blockchain
        }
*/
    }

    public void reset() {
        logger.info("Resetting blockchain ");
        repository.reset();
        blockchain.reset();
        loadBlockchain();
    }


    @PreDestroy
    public void close() {
        stopPeerDiscovery();
        repository.close();
        blockchain.close();
    }
}
