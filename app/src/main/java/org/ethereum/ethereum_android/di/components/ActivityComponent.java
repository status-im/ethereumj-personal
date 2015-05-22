package org.ethereum.ethereum_android.di.components;

import android.app.Activity;

import dagger.Component;

import org.ethereum.ethereum_android.di.PerActivity;
import org.ethereum.ethereum_android.di.modules.ActivityModule;

/**
 * Created by Adrian Tiberius on 20.05.2015.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    Activity activity();
}
