package org.ethereum.android;


import org.ethereum.crypto.HashUtil;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.PeerClient;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;


@Singleton
public class Ethereum extends org.ethereum.facade.EthereumImpl {

    @Inject
    public Ethereum(WorldManager worldManager, AdminInfo adminInfo,
                    ChannelManager channelManager, org.ethereum.manager.BlockLoader blockLoader, ProgramInvokeFactory programInvokeFactory,
                    Provider<PeerClient> peerClientProvider) {

        super(worldManager, adminInfo, channelManager, blockLoader, programInvokeFactory, peerClientProvider);
    }

    public void init(List<String> privateKeys) {

        for (String privateKey: privateKeys) {
            worldManager.getWallet().importKey(Hex.decode(privateKey));
        }

        worldManager.init();
        init();
    }

    public void initSync() {
        worldManager.initSync();
    }

    public byte[] createRandomAccount() {

        byte[] randomPrivateKey = HashUtil.sha3(HashUtil.randomPeerId());
        worldManager.getWallet().importKey(randomPrivateKey);
        return randomPrivateKey;
    }


}
