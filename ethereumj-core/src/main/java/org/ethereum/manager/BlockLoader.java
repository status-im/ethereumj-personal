package org.ethereum.manager;


import org.ethereum.core.Block;
import org.ethereum.facade.Blockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.ethereum.config.SystemProperties.CONFIG;

@Singleton
public class BlockLoader {

    private static final Logger logger = LoggerFactory.getLogger("BlockLoader");

    private Blockchain blockchain;

    Scanner scanner = null;

    @Inject
    public BlockLoader(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public long loadBlocks(){

        String fileSrc = CONFIG.blocksLoader();
        try {
            long startTime = System.currentTimeMillis();
            FileInputStream inputStream = null;
            inputStream = new FileInputStream(fileSrc);
            scanner = new Scanner(inputStream, "UTF-8");

            System.out.println("Loading blocks: " + fileSrc);

            while (scanner.hasNextLine()) {

                byte[] blockRLPBytes = Hex.decode( scanner.nextLine());
                Block block = new Block(blockRLPBytes);

                long t1 = System.nanoTime();
                if (block.getNumber() > blockchain.getBestBlock().getNumber()){
                    blockchain.tryToConnect(block);
                    long t1_ = System.nanoTime();
                    String result = String.format("Imported block #%d took: [%02.2f msec]",
                            block.getNumber(), ((float)(t1_ - t1) / 1_000_000));

                    System.out.println(result);
                } else {

                    if (block.getNumber() % 10000 == 0)
                        System.out.println("Skipping block #" + block.getNumber());
                }


            }
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Finished loading blocks in " + (duration / 1000) + " seconds (" + (duration / 60000) + " minutes)");
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }




}
