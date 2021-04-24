package com.tiza.leo.gateway.protocol;

import com.tiza.binary.Bytes;
import lombok.Getter;

/**
 * Author: tz_wl
 * Date: 2021/4/18 11:13
 * Content:  协议帧格式错误
 */
@Getter
public class IllegalFrameException extends RuntimeException {
    private byte[] bytes;

    public IllegalFrameException(){
        super();
    }

    public IllegalFrameException(String message, byte[] bytes){
        super(message);
        this.bytes = bytes;
    }

    @Override
    public String toString(){
        return getMessage() + ":" + Bytes.toHexString(bytes);
    }
}