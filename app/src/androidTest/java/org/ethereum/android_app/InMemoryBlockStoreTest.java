package org.ethereum.android_app;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.ethereum.android.db.OrmLiteBlockStoreDatabase;
import org.ethereum.android.util.Scanner;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.db.BlockStore;
import org.ethereum.android.db.InMemoryBlockStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigInteger.ZERO;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class InMemoryBlockStoreTest extends ActivityInstrumentationTestCase2<TestActivity> {

    private static final Logger logger = LoggerFactory.getLogger("test");

    private static boolean loaded = false;

    private TestActivity activity;
    private List<Block> blocks = new ArrayList<>();
    private OrmLiteBlockStoreDatabase database;

    public InMemoryBlockStoreTest() {

        super(TestActivity.class);
    }

    @Before
    public void setUp() throws Exception {

        super.setUp();
        loaded = false;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = getActivity();
        InputStream inputStream = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open("blockstore");
        final Scanner scanner = new Scanner(inputStream);
        ThreadGroup group = new ThreadGroup("threadGroup");
        new Thread(group, new Runnable() {
            @Override
            public void run() {

                logger.info("Loading blocks.");

                BigInteger cumDifficulty = ZERO;

                while (scanner.hasNext()) {

                    String blockString = scanner.nextLine();

                    logger.info("BlockString: " + blockString);
                    try {
                        Block block = new Block(
                                Hex.decode(blockString));
                        //if (block.getNumber() % 1000 == 0)
                        logger.info("adding block.hash: [{}] block.number: [{}]",
                                block.getShortHash(),
                                block.getNumber());

                        blocks.add(block);
                        cumDifficulty = cumDifficulty.add(block.getCumulativeDifficulty());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                logger.info("total difficulty: {}", cumDifficulty);
                InMemoryBlockStoreTest.loaded = true;
            }
        }, "EthereumConnect", 32768000).start();


        database = OpenHelperManager.getHelper(activity, OrmLiteBlockStoreDatabase.class);
    }

    @Test
    public void testSet0() {
        while(!loaded) {
            //Thread.sleep(1000);
        }
        assertThat(activity, notNullValue());
        assertThat(getInstrumentation(), notNullValue());
    }

    @Test
    public void testEmpty(){
        while(!loaded) {
            //Thread.sleep(1000);
        }
        BlockStore blockStore = new InMemoryBlockStore(database);
        assertNull(blockStore.getBestBlock());
    }

    @Test
    public void testFlush(){
        while(!loaded) {
            //Thread.sleep(1000);
        }
        BlockStore blockStore = new InMemoryBlockStore(database);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        blockStore.flush();
    }

    @Test
    public void testSimpleLoad(){

        while(!loaded) {
            //Thread.sleep(1000);
        }

        BlockStore blockStore = new InMemoryBlockStore(database);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        blockStore.flush();

        blockStore = new InMemoryBlockStore(database);

        blockStore.load();

        assertTrue(blockStore.getBestBlock().getNumber() == 8003);
    }

    @Test
    public void testFlushEach1000(){

        while(!loaded) {
            //Thread.sleep(1000);
        }
        InMemoryBlockStore blockStore = new InMemoryBlockStore(database);

        for( int i = 0; i < blocks.size(); ++i ){

            blockStore.saveBlock(blocks.get(i), null);
            if ( i % 1000 == 0){
                blockStore.flush();
                assertTrue(blockStore.blocks.size() == 1);
            }
        }
    }


    @Test
    public void testBlockHashByNumber(){

        while(!loaded) {
            //Thread.sleep(1000);
        }
        BlockStore blockStore = new InMemoryBlockStore(database);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        String hash = Hex.toHexString(blockStore.getBlockHashByNumber(7000));
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(6000));
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(5000));
        assertTrue(hash.startsWith("820aa7"));

        blockStore.flush();

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(7000));
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(6000));
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockHashByNumber(5000));
        assertTrue(hash.startsWith("820aa7"));
    }

    @Test
    public void testBlockByNumber(){

        while(!loaded) {
            //Thread.sleep(1000);
        }
        BlockStore blockStore = new InMemoryBlockStore(database);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        String hash = Hex.toHexString(blockStore.getBlockByNumber(7000).getHash());
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(6000).getHash());
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(5000).getHash());
        assertTrue(hash.startsWith("820aa7"));

        blockStore.flush();

        hash = Hex.toHexString(blockStore.getBlockByNumber(7000).getHash());
        assertTrue(hash.startsWith("459a8f"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(6000).getHash());
        assertTrue(hash.startsWith("7a577a"));

        hash = Hex.toHexString(blockStore.getBlockByNumber(5000).getHash());
        assertTrue(hash.startsWith("820aa7"));
    }


    @Test
    public void testGetBlockByNumber() {

        while(!loaded) {
            //Thread.sleep(1000);
        }
        BlockStore blockStore = new InMemoryBlockStore(database);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        assertEquals("4312750101",  blockStore.getTotalDifficulty().toString());

        blockStore.flush();
        assertEquals("4312750101",  blockStore.getTotalDifficulty().toString());
    }


    @Test
    public void testDbGetBlockByHash(){

        while(!loaded) {
            //Thread.sleep(1000);
        }
        BlockStore blockStore = new InMemoryBlockStore(database);

        for( Block block : blocks ){
            blockStore.saveBlock(block, null);
        }

        byte[] hash7000 = Hex.decode("459a8f0ee5d4b0c9ea047797606c94f0c1158ed0f30120490b96f7df9893e1fa");
        byte[] hash6000 = Hex.decode("7a577a6b0b7e72e51a646c4cec82cf684c977bca6307e2a49a4116af49316159");
        byte[] hash5000 = Hex.decode("820aa786619e1a2ae139877ba342078c83e5bd65c559069336c13321441e03dc");

        Long number = blockStore.getBlockByHash(hash7000).getNumber();
        assertTrue(number == 7000);

        number = blockStore.getBlockByHash(hash6000).getNumber();
        assertTrue(number == 6000);

        number = blockStore.getBlockByHash(hash5000).getNumber();
        assertTrue(number == 5000);

    }

    /*
    @Ignore // TO much time to run it on general basis
    @Test
    public void save100KBlocks() throws FileNotFoundException {

        String blocksFile = "E:\\temp\\_poc-9-blocks\\poc-9-492k.dmp";

        FileInputStream inputStream = new FileInputStream(blocksFile);
        Scanner scanner = new Scanner(inputStream, "UTF-8");

        BlockStore blockStore = new InMemoryBlockStore();
        //blockStore.setSessionFactory(sessionFactory());


        while (scanner.hasNextLine()) {

            byte[] blockRLPBytes = Hex.decode( scanner.nextLine());
            Block block = new Block(blockRLPBytes);

            System.out.println(block.getNumber());

            blockStore.saveBlock(block, null);

            if (block.getNumber() > 100_000) break;
        }

        blockStore.flush();
    }
    */

}
