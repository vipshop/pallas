package com.vip.pallas.utils;

public class MetricConvertUtil {

    public static double byteToMb(long value) {
        if(value <= 0 ){
            return 0.0;
        }
        return value * 1.0 /1024 / 1024;
    }

}
