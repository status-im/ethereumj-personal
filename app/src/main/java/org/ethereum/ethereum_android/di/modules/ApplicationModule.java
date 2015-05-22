package org.ethereum.ethereum_android.di.modules;

import android.content.Context;

import org.ethereum.ethereum_android.EthereumApplication;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final EthereumApplication application;

    public ApplicationModule(EthereumApplication application) {
        this.application = application;
    }

    @Provides @Singleton
    Context provideApplicationContext() {
        return this.application;
    }


}