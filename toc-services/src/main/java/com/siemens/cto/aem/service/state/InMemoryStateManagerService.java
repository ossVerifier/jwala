package com.siemens.cto.aem.service.state;

/**
 * Provides a contract for managing states in memory.
 *
 * Created by JC043760 on 3/25/2016.
 */
public interface InMemoryStateManagerService<K, V> {

    void put(K key, V val);

    V get(K key);

    void remove(K key);

    boolean containsKey(K key);

}
