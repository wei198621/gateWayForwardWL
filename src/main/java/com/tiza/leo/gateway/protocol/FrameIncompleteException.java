package com.tiza.leo.gateway.protocol;

import com.tiza.binary.Bytes;

/**
 * 协议帧不完整，主要用户处理TCP的拆包发生的情况
 * Author: tz_wl
 * Date: 2021/4/18 11:27
 * Content:
 */
public class FrameIncompleteException extends RuntimeException {
    private byte[] bytes;

    public FrameIncompleteException(byte[] bytes){
        super("协议帧不完整");
        this.bytes = bytes;
    }

    @Override
    public String toString(){
        return getMessage() + ":" + Bytes.toHexString(bytes);
    }
}
