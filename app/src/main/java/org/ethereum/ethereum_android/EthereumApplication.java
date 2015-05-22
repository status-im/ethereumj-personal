package org.ethereum.ethereum_android;


import android.app.Application;
import android.support.multidex.MultiDexApplication;

import javax.inject.Singleton;
import dagger.Component;

import org.ethereum.ethereum_android.di.components.ApplicationComponent;
import org.ethereum.ethereum_android.di.components.DaggerApplicationComponent;
import org.ethereum.ethereum_android.di.modules.ApplicationModule;

public class EthereumApplication extends MultiDexApplication {

    private ApplicationComponent applicationComponent = null;

    @Override public void onCreate() {
        super.onCreate();
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
    }

    public void setComponent(ApplicationComponent component) {
        this.applicationComponent = component;
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

}