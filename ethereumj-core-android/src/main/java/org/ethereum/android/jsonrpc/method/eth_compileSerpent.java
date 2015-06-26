package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.core.AccountState;
import org.ethereum.facade.Ethereum;
import org.ethereum.serpent.SerpentCompiler;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
TODO: Serpent will be depricated in future.
*/
public class eth_compileSerpent extends JsonRpcServerMethod {

    public eth_compileSerpent (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            String code = (String)params.get(0);
            String asmResult = "";
            byte[] machineCode = null;

            try {
                Pattern pattern = Pattern.compile("(.*?)init:(.*?)code:(.*?)", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(code);
                if (matcher.find()) {
                    asmResult = SerpentCompiler.compileFullNotion(code);
                    machineCode = SerpentCompiler.compileFullNotionAssemblyToMachine(asmResult);
                } else {
                    asmResult = SerpentCompiler.compile(code);
                    machineCode = SerpentCompiler.compileAssemblyToMachine(asmResult);
                    machineCode = SerpentCompiler.encodeMachineCodeForVMRun(machineCode, null);
                }
            } catch (Throwable th) {
                return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
            }


            return new JSONRPC2Response("0x" + Hex.toHexString(machineCode), req.getID());
        }

    }
}