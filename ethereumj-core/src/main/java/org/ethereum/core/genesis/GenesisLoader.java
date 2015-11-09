package org.ethereum.core.genesis;

import com.google.common.io.ByteStreams;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.jsontestsuite.Utils;
import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.program.Program;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigInteger.ZERO;
import static org.ethereum.config.SystemProperties.CONFIG;
import static org.ethereum.core.Genesis.ZERO_HASH_2048;
import static org.ethereum.crypto.HashUtil.EMPTY_LIST_HASH;
import static org.ethereum.util.ByteUtil.wrap;

public class GenesisLoader {

    public static Genesis loadGenesis()  {


        try {

            String genesisFile = CONFIG.genesisInfo();

            InputStream is = ClassLoader.getSystemResourceAsStream("genesis/" + genesisFile);
            String json = new String(ByteStreams.toByteArray(is));

            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructType(GenesisJson.class);

            GenesisJson genesisJson  = new ObjectMapper().readValue(json, type);

            Genesis genesis = createBlockForJson(genesisJson);

            Map<ByteArrayWrapper, AccountState> premine = generatePreMine(genesisJson.getAlloc());
            genesis.setPremine(premine);

            byte[] rootHash = generateRootHash(premine);
            genesis.setStateRoot(rootHash);

            return genesis;
        } catch (Throwable e) {
            System.err.print(e.getMessage());
            System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
            System.exit(-1);
        }
        System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
        System.exit(-1);
        return null;
    }

    public static void exportGenesis(Genesis genesis) {
        try {
            byte[] data = genesis.getEncoded();
            FileOutputStream fos = new FileOutputStream("genesis.bin");
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(genesis.getPremine());
            byte[] data1 = b.toByteArray();
            FileOutputStream fos1 = new FileOutputStream("premine.bin");
            fos1.write(data1, 0, data1.length);
            fos1.flush();
            fos1.close();


            FileOutputStream fos2 = new FileOutputStream("roothash.bin");
            fos2.write(genesis.getStateRoot(), 0, genesis.getStateRoot().length);
            fos2.flush();
            fos2.close();
        } catch (Exception e) {

        }
    }

    public static Genesis loadBinaryGenesis(InputStream genesisStream, InputStream premineStream, InputStream roothashStream)  {


        try {

            byte[] data = ByteStreams.toByteArray(genesisStream);
            Genesis genesis = new Genesis(data);

            data = ByteStreams.toByteArray(premineStream);
            Map<ByteArrayWrapper, AccountState> premine = (Map<ByteArrayWrapper, AccountState>)Genesis.deserialize(data);
            genesis.setPremine(premine);

            data = ByteStreams.toByteArray(roothashStream);
            genesis.setStateRoot(data);

            return genesis;
        } catch (Throwable e) {
            System.err.print(e.getMessage());
            System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
            System.exit(-1);
        }
        System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
        System.exit(-1);
        return null;
    }

    public static Genesis loadBinaryGenesis()  {


        try {
            File inputFile = new File("genesis.bin");
            byte[] data = new byte[(int)inputFile.length()];
            FileInputStream fis = new FileInputStream(inputFile);
            fis.read(data, 0, data.length);
            fis.close();

            Genesis genesis = new Genesis(data);

            inputFile = new File("premine.bin");
            data = new byte[(int)inputFile.length()];
            fis = new FileInputStream(inputFile);
            fis.read(data, 0, data.length);
            fis.close();

            Map<ByteArrayWrapper, AccountState> premine = (Map<ByteArrayWrapper, AccountState>)Genesis.deserialize(data);
            genesis.setPremine(premine);

            inputFile = new File("roothash.bin");
            data = new byte[(int)inputFile.length()];
            fis = new FileInputStream(inputFile);
            fis.read(data, 0, data.length);
            fis.close();
            genesis.setStateRoot(data);

            return genesis;
        } catch (Throwable e) {
            System.err.print(e.getMessage());
            System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
            System.exit(-1);
        }
        System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
        System.exit(-1);
        return null;
    }

    public static Genesis androidLoadGenesis(InputStream is)  {


        try {

            String json = new String(ByteStreams.toByteArray(is));

            ObjectMapper mapper = new ObjectMapper();
            JavaType type = mapper.getTypeFactory().constructType(GenesisJson.class);

            GenesisJson genesisJson  = new ObjectMapper().readValue(json, type);

            Genesis genesis = createBlockForJson(genesisJson);

            Map<ByteArrayWrapper, AccountState> premine = generatePreMine(genesisJson.getAlloc());
            genesis.setPremine(premine);

            byte[] rootHash = generateRootHash(premine);
            genesis.setStateRoot(rootHash);


            return genesis;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
            System.exit(-1);
        }

        System.err.println("Genesis block configuration is corrupted or not found ./resources/genesis/...");
        System.exit(-1);
        return null;
    }


    private static Genesis createBlockForJson(GenesisJson genesisJson){

        byte[] nonce       = Utils.parseData(genesisJson.nonce);
        byte[] difficulty  = Utils.parseData(genesisJson.difficulty);
        byte[] mixHash     = Utils.parseData(genesisJson.mixhash);
        byte[] coinbase    = Utils.parseData(genesisJson.coinbase);

        byte[] timestampBytes = Utils.parseData(genesisJson.timestamp);
        long   timestamp         = ByteUtil.byteArrayToLong(timestampBytes);

        byte[] parentHash  = Utils.parseData(genesisJson.parentHash);
        byte[] extraData   = Utils.parseData(genesisJson.extraData);

        byte[] gasLimitBytes    = Utils.parseData(genesisJson.gasLimit);
        long   gasLimit         = ByteUtil.byteArrayToLong(gasLimitBytes);

        return new Genesis(parentHash, EMPTY_LIST_HASH, coinbase, ZERO_HASH_2048,
                            difficulty, 0, gasLimit, 0, timestamp, extraData,
                            mixHash, nonce);
    }


    private static Map<ByteArrayWrapper, AccountState> generatePreMine(Map<String, AllocatedAccount> alloc){

        Map<ByteArrayWrapper, AccountState> premine = new HashMap<>();
        for (String key : alloc.keySet()){

            BigInteger balance = new BigInteger(alloc.get(key).getBalance());
            AccountState acctState = new AccountState(ZERO, balance);

            premine.put(wrap(Hex.decode(key)) , acctState);
        }

        return premine;
    }

    private static byte[] generateRootHash(Map<ByteArrayWrapper, AccountState> premine){

        Trie state = new SecureTrie(null);

        for (ByteArrayWrapper key : premine.keySet()) {
            state.update(key.getData(), premine.get(key).getEncoded());
        }

        return state.getRootHash();
    }
}
