package org.ethereum.ethereum_android.di;

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by userica on 22.05.2015.
 */
@Scope
@Retention(RUNTIME)
public @interface PerActivity {}
