package com.siemens.cto.aem.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper to be able to wire a map using Spring's @Autowire on a constructor.
 *
 * Created by JC043760 on 3/23/2016.
 */
public class MapWrapper<K, V> {

    private final Map<K, V> map;

    public MapWrapper(Map<K, V> map) {
        this.map = map;
    }

    public Map<K, V> getMap() {
        return map;
    }

}
