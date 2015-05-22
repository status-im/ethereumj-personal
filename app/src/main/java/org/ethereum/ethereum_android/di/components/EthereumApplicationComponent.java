package org.ethereum.ethereum_android.di.components;

import android.content.Context;

import org.ethereum.di.components.EthereumComponent;

public interface EthereumApplicationComponent extends EthereumComponent {

    Context context();
}