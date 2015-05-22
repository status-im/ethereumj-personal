package org.ethereum.ethereum_android.di.components;

import android.content.Context;

import org.ethereum.di.components.EthereumComponent;

/**
 * Created by Adrian Tiberius on 20.05.2015.
 */
public interface EthereumApplicationComponent extends EthereumComponent {

    Context context();
}
