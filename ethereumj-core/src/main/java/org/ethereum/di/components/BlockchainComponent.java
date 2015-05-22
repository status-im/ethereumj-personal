package org.ethereum.di.components;

import org.ethereum.di.modules.BlockchainModule;
import org.ethereum.di.modules.EthereumModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = BlockchainModule.class)
public interface BlockchainComponent {
}