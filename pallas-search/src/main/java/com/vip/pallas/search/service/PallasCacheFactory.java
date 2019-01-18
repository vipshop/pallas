package com.vip.pallas.search.service;

import com.vip.pallas.search.service.impl.PallasCacheServiceImpl;

public class PallasCacheFactory {

    private static PallasCacheService instance = PallasCacheServiceImpl.getInstance();

    public static PallasCacheService getCacheService(){
        return instance;
    }

    static void setCacheService(PallasCacheService service) {
        instance = service;
    }
}
