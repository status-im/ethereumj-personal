package org.ethereum.android.service;


import android.os.Messenger;

import java.util.BitSet;

public class ClientListener {

    public BitSet flags;
    public Messenger listener;
    public String identifier;

    public ClientListener(Messenger listener, BitSet flags, String identifier) {

        this.listener = listener;
        this.flags = flags;
        this.identifier = identifier;
    }
}
