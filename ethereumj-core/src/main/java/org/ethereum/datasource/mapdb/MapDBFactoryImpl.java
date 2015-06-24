package org.ethereum.datasource.mapdb;

import org.ethereum.datasource.KeyValueDataSource;

public class MapDBFactoryImpl implements MapDBFactory {

    @Override
    public KeyValueDataSource createDataSource() {
        return new MapDBDataSource();
    }
}
