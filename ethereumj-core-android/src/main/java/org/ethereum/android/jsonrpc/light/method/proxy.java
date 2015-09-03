package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

import java.util.ArrayList;

public class proxy extends JsonRpcServerMethod {

    ArrayList<String> proxyMethods;
    ArrayList<String> deprecatedMethods;

    public proxy (Ethereum ethereum)
    {
        super(ethereum);
        proxyMethods = new ArrayList<>();
        proxyMethods.add("web3_clientVersion");
        proxyMethods.add("web3_sha3");
        proxyMethods.add("net_version");
        proxyMethods.add("net_listening");
        proxyMethods.add("net_peerCount");
        proxyMethods.add("eth_protocolVersion");
        proxyMethods.add("eth_gasPrice");
        proxyMethods.add("eth_blockNumber");
        proxyMethods.add("eth_getBalance");
        proxyMethods.add("eth_getStorageAt");
        proxyMethods.add("eth_getTransactionCount");
        proxyMethods.add("eth_getBlockTransactionCountByHash");
        proxyMethods.add("eth_getBlockTransactionCountByNumber");
        proxyMethods.add("eth_getUncleCountByBlockHash");
        proxyMethods.add("eth_getUncleCountByBlockNumber");
        proxyMethods.add("eth_getCode");
        proxyMethods.add("eth_getBlockByHash");
        proxyMethods.add("eth_getBlockByNumber");
        proxyMethods.add("eth_getTransactionByHash");
        proxyMethods.add("eth_getTransactionByBlockHashAndIndex");
        proxyMethods.add("eth_getTransactionByBlockNumberAndIndex");
        proxyMethods.add("eth_getTransactionReceipt");
        proxyMethods.add("eth_getUncleByBlockHashAndIndex");
        proxyMethods.add("eth_getUncleByBlockNumberAndIndex");
        proxyMethods.add("eth_getCompilers");
        proxyMethods.add("eth_compileSolidity");
        proxyMethods.add("eth_compileLLL");
        proxyMethods.add("eth_compileSerpent");
        proxyMethods.add("eth_newFilter");
        proxyMethods.add("eth_newBlockFilter");
        proxyMethods.add("eth_newPendingTransactionFilter");
        proxyMethods.add("eth_uninstallFilter");
        proxyMethods.add("eth_getFilterChanges");
        proxyMethods.add("eth_getFilterLogs");
        proxyMethods.add("eth_getLogs");

        proxyMethods.add("shh_version");
        proxyMethods.add("shh_post");
        proxyMethods.add("shh_newIdentity");
        proxyMethods.add("shh_hasIdentity");
        proxyMethods.add("shh_newGroup");
        proxyMethods.add("shh_addToGroup");
        proxyMethods.add("shh_newFilter");
        proxyMethods.add("shh_uninstallFilter");
        proxyMethods.add("shh_getFilterChanges");
        proxyMethods.add("shh_getMessages");

        //TODO: issue methods - they generate transaction but must call them in blockchain.
        proxyMethods.add("eth_call");
        proxyMethods.add("eth_estimateGas");


        deprecatedMethods = new ArrayList<>();
        //db - deprecated in specification
        deprecatedMethods.add("db_getHex");
        deprecatedMethods.add("db_getString");
        deprecatedMethods.add("db_putHex");
        deprecatedMethods.add("db_putString");
        //mining - deprecated because will be mess over global.
        deprecatedMethods.add("eth_getWork");
        deprecatedMethods.add("eth_hashrate");
        deprecatedMethods.add("eth_mining");
        deprecatedMethods.add("eth_submitWork");
    }

    @Override
    public String[] handledRequests() {
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.addAll(proxyMethods);
        tmp.addAll(deprecatedMethods);
        return tmp.toArray(new String[tmp.size()]);
    }

    @Override
    public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
        if (proxyMethods.contains(req.getMethod())) {
            return worker(req, ctx);
        } else if (deprecatedMethods.contains(req.getMethod())) {
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        } else {
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {
        JSONRPC2Response res = getRemoteData(req);
        if (res == null) {
            return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
        }
        return res;
    }
}