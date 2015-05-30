package org.ethereum.android.di.components;

import android.content.Context;

import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.facade.Ethereum;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = EthereumModule.class)
public interface EthereumComponent {

    Context context();
    Ethereum ethereum();
}
