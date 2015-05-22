package org.ethereum.ethereum_android.di.components;

import android.app.Activity;

import dagger.Component;

import org.ethereum.ethereum_android.di.PerActivity;
import org.ethereum.ethereum_android.di.modules.ActivityModule;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    Activity activity();
}