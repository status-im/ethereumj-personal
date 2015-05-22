package org.ethereum.ethereum_android.di.components;

import android.content.Context;

import org.ethereum.ethereum_android.MainActivity;
import org.ethereum.ethereum_android.di.modules.ApplicationModule;
import org.ethereum.facade.Ethereum;

import dagger.Component;
import javax.inject.Singleton;

/**
 * Created by Adrian Tiberius on 20.05.2015.
 */
@Singleton // Constraints this component to one-per-application or unscoped bindings.
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(MainActivity mainActivity);

    //Exposed to sub-graphs.
    Context context();
}
