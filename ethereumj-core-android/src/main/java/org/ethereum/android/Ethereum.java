package org.ethereum.android;


import org.ethereum.android.manager.BlockLoader;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.core.Wallet;
import org.ethereum.crypto.HashUtil;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Repository;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.peerdiscovery.PeerDiscovery;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.PeerServer;
import org.ethereum.net.submit.TransactionExecutor;
import org.ethereum.net.submit.TransactionTask;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.ethereum.config.SystemProperties.CONFIG;

public class Ethereum implements org.ethereum.facade.Ethereum {

    private static final Logger logger = LoggerFactory.getLogger("facade");

    BlockStore blockStore;

    Blockchain blockchain;

    Repository repository;

    EthereumListener listener;

    AdminInfo adminInfo;

    ChannelManager channelManager;

    PeerServer peerServer;

    PeerDiscovery peerDiscovery;

    BlockLoader blockLoader;

    Provider<PeerClient> peerClientProvider;

    Wallet wallet;

    private PeerClient activePeer;

    @Inject
    public Ethereum(Blockchain blockchain, BlockStore blockStore, Repository repository, AdminInfo adminInfo,
                        ChannelManager channelManager, BlockLoader blockLoader,
                        Provider<PeerClient> peerClientProvider, EthereumListener listener,
                        PeerDiscovery peerDiscovery, Wallet wallet) {

        System.out.println();
        logger.info("EthereumImpl constructor");
        this.blockchain = blockchain;
        this.blockStore = blockStore;
        this.repository = repository;
        this.adminInfo = adminInfo;
        this.channelManager = channelManager;
        this.blockLoader = blockLoader;
        this.peerClientProvider = peerClientProvider;
        this.listener = listener;
        this.peerDiscovery = peerDiscovery;
        this.wallet = wallet;
    }

    @Override
    public void init() {

        init(null);
    }

    public void init(List<String> addresses) {

        if (addresses != null) {
            for (String address: addresses) {
                wallet.importKey(address.getBytes());
            }
        }

        // Load the blockchain
        loadBlockchain();

        // Start peer server
        if (CONFIG.listenPort() > 0) {
            Executors.newSingleThreadExecutor().submit(
                    new Runnable() {
                        public void run() {
//                            peerServer.start(CONFIG.listenPort());
                        }
                    }
            );
        }
    }

    public byte[] createRandomAccount() {

        byte[] randomAddress = HashUtil.sha3(HashUtil.randomPeerId());
        wallet.importKey(randomAddress);
        return randomAddress;
    }

    public void loadBlockchain() {

        if (!CONFIG.databaseReset())
            blockStore.load();

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

            listener.onBlock(Genesis.getInstance(), new ArrayList<TransactionReceipt>() );
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

    /**
     * Find a peer but not this one
     *
     * @param peer - peer to exclude
     * @return online peer
     */
    @Override
    public PeerInfo findOnlinePeer(PeerInfo peer) {

        Set<PeerInfo> excludePeers = new HashSet<>();
        excludePeers.add(peer);
        return findOnlinePeer(excludePeers);
    }

    @Override
    public PeerInfo findOnlinePeer() {

        Set<PeerInfo> excludePeers = new HashSet<>();
        return findOnlinePeer(excludePeers);
    }

    @Override
    public PeerInfo findOnlinePeer(Set<PeerInfo> excludePeers) {

        logger.info("Looking for online peers...");

        final EthereumListener listener = this.listener;
        listener.trace("Looking for online peer");

        startPeerDiscovery();

        final Set<PeerInfo> peers = getPeers();
        for (PeerInfo peer : peers) { // it blocks until a peer is available.
            if (peer.isOnline() && !excludePeers.contains(peer)) {
                logger.info("Found peer: {}", peer.toString());
                listener.trace(String.format("Found online peer: [ %s ]", peer.toString()));
                return peer;
            }
        }
        return null;
    }

    @Override
    public PeerInfo waitForOnlinePeer() {

        PeerInfo peer = null;
        while (peer == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            peer = this.findOnlinePeer();
        }
        return peer;
    }

    @Override
    public Set<PeerInfo> getPeers() {

        return peerDiscovery.getPeers();
    }

    @Override
    public void startPeerDiscovery() {

        if (!peerDiscovery.isStarted())
            peerDiscovery.start();
    }

    @Override
    public void stopPeerDiscovery() {

        if (peerDiscovery.isStarted())
            peerDiscovery.stop();
    }

    @Override
    public void connect(InetAddress addr, int port, String remoteId) {

        connect(addr.getHostName(), port, remoteId);
    }

    @Override
    public void connect(String ip, int port, String remoteId) {

        logger.info("Connecting to: {}:{}", ip, port);

        if (activePeer == null)
            activePeer = peerClientProvider.get();

        activePeer.connect(ip, port, remoteId);
    }

    @Override
    public Blockchain getBlockchain() {


        return blockchain;
    }

    @Override
    public void addListener(EthereumListener listener) {

        ((CompositeEthereumListener) this.listener).addListener(listener);
    }

    @Override
    public boolean isBlockchainLoading() {

        return blockchain.getQueue().size() > 2;
    }

    @Override
    public void close() {

        stopPeerDiscovery();
        repository.close();
        blockchain.close();
    }

    @Override
    public PeerClient getDefaultPeer() {

        if (activePeer == null) {
            activePeer = peerClientProvider.get();
        }
        return activePeer;
    }

    @Override
    public boolean isConnected() {

        return activePeer != null;
    }

    @Override
    public Transaction createTransaction(BigInteger nonce,
                                         BigInteger gasPrice,
                                         BigInteger gas,
                                         byte[] receiveAddress,
                                         BigInteger value, byte[] data) {

        byte[] nonceBytes = ByteUtil.bigIntegerToBytes(nonce);
        byte[] gasPriceBytes = ByteUtil.bigIntegerToBytes(gasPrice);
        byte[] gasBytes = ByteUtil.bigIntegerToBytes(gas);
        byte[] valueBytes = ByteUtil.bigIntegerToBytes(value);

        return new Transaction(nonceBytes, gasPriceBytes, gasBytes,
                receiveAddress, valueBytes, data);
    }


    @Override
    public Future<Transaction> submitTransaction(Transaction transaction) {

        TransactionTask transactionTask = new TransactionTask(transaction, channelManager);

        return TransactionExecutor.instance.submitTransaction(transactionTask);
    }


    @Override
    public Wallet getWallet() {

        return wallet;
    }


    @Override
    public Repository getRepository() {

        return repository;
    }

    @Override
    public AdminInfo getAdminInfo() {

        return adminInfo;
    }

    @Override
    public ChannelManager getChannelManager() {

        return channelManager;
    }


    @Override
    public Set<Transaction> getPendingTransactions() {

        return blockchain.getPendingTransactions();
    }

    @Override
    public BlockLoader getBlockLoader() {

        return  blockLoader;
    }

    @Override
    public void exitOn(long number) {

        blockchain.setExitOn(number);
    }
}
