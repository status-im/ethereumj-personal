package org.ethereum.di.components;

import android.content.Context;

import org.ethereum.di.modules.EthereumModule;
import org.ethereum.facade.Ethereum;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Adrian Tiberius on 20.05.2015.
 */
@Singleton
@Component(modules = EthereumModule.class)
public interface EthereumComponent {

    //void inject(EthereumManager ethereumManager);

    Context context();
    Ethereum ethereum();
}
