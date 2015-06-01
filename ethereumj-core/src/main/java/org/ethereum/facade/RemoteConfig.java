package org.ethereum.facade;

import org.ethereum.db.BlockStore;
import org.ethereum.db.InMemoryBlockStore;
import org.hibernate.SessionFactory;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 27/01/2015 01:05
 */
public class RemoteConfig {


    public BlockStore blockStore(SessionFactory sessionFactory){
        return new InMemoryBlockStore();
    }
}
