package com.tiza.leo.gateway.utils;

/**
 * Author: tz_wl
 * Date: 2021/4/18 11:39
 * Content:
 */
public class util {

    public static byte[] longToBytes(long number, int length) {
        long temp = number;

        byte[] bytes = new byte[length];
        for (int i = bytes.length - 1; i > -1; i--) {
            bytes[i] = new Long(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return bytes;
    }

}
