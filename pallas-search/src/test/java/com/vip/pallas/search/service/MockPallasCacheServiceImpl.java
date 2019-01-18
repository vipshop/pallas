package com.vip.pallas.search.service;

/**
 * Created by owen on 10/01/2018.
 */
public class MockPallasCacheServiceImpl {

    public static void mockPallasCacheFactory(PallasCacheService mockService) {
        PallasCacheFactory.setCacheService(mockService);
    }
}
