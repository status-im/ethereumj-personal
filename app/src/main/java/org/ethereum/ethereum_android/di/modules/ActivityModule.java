package org.ethereum.ethereum_android.di.modules;

import android.app.Activity;

import org.ethereum.ethereum_android.di.PerActivity;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Adrian Tiberius on 20.05.2015.
 */
@Module
public class ActivityModule {

    private final Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    Activity activity() {
        return this.activity;
    }


}
