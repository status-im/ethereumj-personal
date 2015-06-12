package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import net.minidev.json.JSONObject;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.core.Account;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.ethereum.core.Denomination.SZABO;
import static org.ethereum.config.SystemProperties.CONFIG;

/*
Not sure if we must call submitTransaction from here but logically to do it. Also not clear how created transaction added to pending and to "from" account pending (in test - it didn't)
TODO: get advice from Roman. By spec if created transaction (empty data param) - result must be 20 bytes hash, but I got 32 bytes for both contract and transaction create.
*/
public class eth_sendTransaction extends JsonRpcServerMethod {

    public eth_sendTransaction (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            JSONObject obj = (JSONObject)params.get(0);
            if (!obj.containsKey("from") || (!obj.containsKey("to") && !obj.containsKey("data"))) {
                return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
            }

            byte[] from = jsToAddress((String)obj.get("from"));
            Account acc = null;
            for (Account ac : ethereum.getWallet().getAccountCollection()) {
                if (Arrays.equals(ac.getAddress(), from)) {
                    acc = ac;
                    break;
                }
            }
            if (acc == null) {
                return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
            }

            byte[] senderPrivKey = acc.getEcKey().getPrivKeyBytes();

            // default - from ethereumj-studio
            byte[] to = null;
            if (obj.containsKey("to") && !((String)obj.get("to")).equals("")) {
                to = jsToAddress((String) obj.get("to"));
            }

            // default - from ethereumj-studio
            BigInteger gasPrice = SZABO.value().multiply(BigInteger.TEN);
            if (obj.containsKey("gasPrice") && !((String)obj.get("gasPrice")).equals("")) {
                gasPrice = jsToBigInteger((String) obj.get("gasPrice"));
            }

            // default - from cpp-ethereum
            BigInteger gas = acc.getBalance().divide(gasPrice);
            BigInteger gasBBLimit = new BigInteger(Long.toString(ethereum.getBlockchain().getBestBlock().getGasLimit() / 5));
            if (gasBBLimit.compareTo(gas) < 0)
                gas = gasBBLimit;
            if (obj.containsKey("gas") && !((String)obj.get("gas")).equals("")) {
                gas = jsToBigInteger((String) obj.get("gas"));
            }

            // default - from ethereumj-studio
            BigInteger value = new BigInteger("1000");
            if (obj.containsKey("value") && !((String)obj.get("value")).equals("")) {
                value = jsToBigInteger((String) obj.get("value"));
            }

            // default - from ethereumj-studio
            BigInteger nonce = ethereum.getRepository().getNonce(acc.getAddress());
            if (obj.containsKey("nonce") && !((String)obj.get("nonce")).equals("")) {
                nonce = jsToBigInteger((String) obj.get("nonce"));
            }

            // default - from ethereumj-studio
            byte[] data = new byte[]{};
            if (obj.containsKey("data") && !((String)obj.get("data")).equals("")) {
                data = jsToAddress((String) obj.get("data"));
            }

            Transaction tx = ethereum.createTransaction(nonce, gasPrice, gas, to, value, data);

            tx.sign(senderPrivKey);

            try {
                ethereum.submitTransaction(tx).get(CONFIG.transactionApproveTimeout(), TimeUnit.SECONDS);
            } catch (Exception e) {
                return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
            }

            String tmp = "0x" + Hex.toHexString(tx.getHash());
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}