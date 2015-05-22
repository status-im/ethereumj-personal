package org.ethereum.core;

import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Repository;
import org.ethereum.manager.WorldManager;
import org.ethereum.util.Utils;

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;

import java.math.BigInteger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Representation of an actual account or contract
 */
//@Component
//@Scope("prototype")
public class Account {

    private ECKey ecKey;
    private byte[] address;

    private Set<Transaction> pendingTransactions =
            Collections.synchronizedSet(new HashSet<Transaction>());

    Repository repository;

    @Inject
    public Account(Repository repository) {
        this.repository = repository;
    }

    public void init() {
        this.ecKey = new ECKey(Utils.getRandom());
        address = this.ecKey.getAddress();
    }

    public void init(ECKey ecKey) {
        this.ecKey = ecKey;
        address = this.ecKey.getAddress();
    }

    public BigInteger getNonce() {
        AccountState accountState =
                repository.getAccountState(getAddress());

        return accountState.getNonce();
    }

    public BigInteger getBalance() {

        AccountState accountState =
                repository.getAccountState(this.getAddress());

        BigInteger balance = BigInteger.ZERO;

        if (accountState != null)
            balance = accountState.getBalance();

        synchronized (getPendingTransactions()) {
            if (!getPendingTransactions().isEmpty()) {

                for (Transaction tx : getPendingTransactions()) {
                    if (Arrays.equals(getAddress(), tx.getSender())) {
                        balance = balance.subtract(new BigInteger(1, tx.getValue()));
                    }

                    if (Arrays.equals(getAddress(), tx.getReceiveAddress())) {
                        balance = balance.add(new BigInteger(1, tx.getValue()));
                    }
                }
                // todo: calculate the fee for pending
            }
        }


        return balance;
    }


    public ECKey getEcKey() {
        return ecKey;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public Set<Transaction> getPendingTransactions() {
        return this.pendingTransactions;
    }

    public void addPendingTransaction(Transaction transaction) {
        synchronized (pendingTransactions) {
            pendingTransactions.add(transaction);
        }
    }

    public void clearAllPendingTransactions() {
        synchronized (pendingTransactions) {
            pendingTransactions.clear();
        }
    }
}