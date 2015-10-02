package org.ethereum.net.eth.handler;

import org.ethereum.net.eth.EthVersion;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Default factory implementation
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
@Singleton
public class EthHandlerFactoryImpl implements EthHandlerFactory {

    Provider<Eth60> eth60Provider;
    Provider<Eth61> eth61Provider;
    Provider<Eth62> eth62Provider;

    @Inject
    public EthHandlerFactoryImpl(Provider<Eth60> eth60Provider, Provider<Eth61> eth61Provider, Provider<Eth62> eth62Provider) {
        this.eth60Provider = eth60Provider;
        this.eth61Provider = eth61Provider;
        this.eth62Provider = eth62Provider;
    }
    @Override
    public EthHandler create(EthVersion version) {
        switch (version) {
            case V60:   return eth60Provider.get();
            case V61:   return eth61Provider.get();
            case V62:   return eth62Provider.get();
            default:    throw new IllegalArgumentException("Eth " + version + " is not supported");
        }
    }
}
