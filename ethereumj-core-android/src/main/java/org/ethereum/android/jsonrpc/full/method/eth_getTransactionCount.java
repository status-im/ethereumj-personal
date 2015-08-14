package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServer;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.core.AccountState;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class eth_getTransactionCount extends JsonRpcServerMethod {

    public eth_getTransactionCount(Ethereum ethereum) { super(ethereum); }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 2) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] address = jsToAddress((String) params.get(0));
            String height = (String)params.get(1);

            long blockNumber = getBlockNumber(height);

            byte[] root = ethereum.getBlockchain().getBestBlock().getStateRoot();

            if (blockNumber >= 0) {
                // TODO: Missing method on repository
                //ethereum.getRepository().syncToRoot(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
            }

            BigInteger nonce = BigInteger.ZERO;

            // TODO: Missing method on repository
            //AccountState accountState = ethereum.getRepository().getAccountState(address);
            //if (accountState != null)
            //    nonce = accountState.getNonce();

            if (blockNumber == -1) {
                synchronized (ethereum.getBlockchain().getPendingTransactions()) {
                    for (Transaction tx : ethereum.getBlockchain().getPendingTransactions()) {
                        if (Arrays.equals(address, tx.getSender())) {
                            nonce.add(BigInteger.ONE);
                        }
                    }
                }
            }

            if (blockNumber >= 0) {
                // TODO: Missing method on repository
                //ethereum.getRepository().syncToRoot(root);
            }

            String tmp = "0x" + nonce.toString(16);
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}