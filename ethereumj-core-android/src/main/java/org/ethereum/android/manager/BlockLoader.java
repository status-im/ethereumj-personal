package org.ethereum.android.manager;


import org.ethereum.core.Block;
import org.ethereum.core.ImportResult;
import org.ethereum.core.Blockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.FileInputStream;
import java.io.IOException;
import org.ethereum.android.util.Scanner;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.ethereum.config.SystemProperties.CONFIG;

@Singleton
public class BlockLoader extends org.ethereum.manager.BlockLoader {

    private static final Logger logger = LoggerFactory.getLogger("BlockLoader");

    Scanner scanner = null;

    @Inject
    public BlockLoader(Blockchain blockchain) {
        super(blockchain);
    }

    public void loadBlocks(String dumpFile){

        try {
            long startTime = System.currentTimeMillis();
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(dumpFile);
            scanner = new Scanner(inputStream);

            System.out.println("Loading blocks: " + dumpFile);

            while (scanner.hasNext()) {

                byte[] blockRLPBytes = Hex.decode(scanner.nextLine());
                Block block = new Block(blockRLPBytes);

                long t1 = System.nanoTime();
                if (block.getNumber() > blockchain.getBestBlock().getNumber()){
                    blockchain.tryToConnect(block);
                    long t1_ = System.nanoTime();
                    float elapsed = ((float)(t1_ - t1) / 1_000_000);

                    if (block.getNumber() % 1000 == 0 || elapsed > 10_000) {
                        String result = String.format("Imported block #%d took: [%02.2f msec]",
                                block.getNumber(), elapsed);

                        System.out.println(result);
                    }
                } else {

                    if (block.getNumber() % 10000 == 0)
                        System.out.println("Skipping block #" + block.getNumber());
                }
                block = null;
                blockRLPBytes = null;

            }
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Finished loading blocks in " + (duration / 1000) + " seconds (" + (duration / 60000) + " minutes)");
            //return duration;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.out.println(e.getMessage());
        }
        //return 0;
    }




}
