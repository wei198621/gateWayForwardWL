package com.tiza.leo.gateway.protocol;

import com.tiza.binary.Bytes;
import lombok.Getter;
import lombok.Setter;

/**
 * Author: tz_wl
 * Date: 2021/4/18 9:48
 * Content:  数据校验错误  暂时没有用到
 */
@Getter
@Setter
public class CheckException extends RuntimeException {
    /**
     * 校验数据内容
     */
    private byte[] bytes;
    /**
     * 数据校验码
     */
    private byte[] excepted;
    /**
     * 实际校验码
     */
    private byte[] actual;

    public CheckException(){
        super();
    }

    public CheckException(String message, byte[] bytes, byte[] excepted, byte[] actual){
        super(message);
        this.bytes = bytes;
        this.excepted = excepted;
        this.actual = actual;
    }


    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder(bytes.length * 2 + 100);
        builder.append("校验码错误，期望校验码[");
        builder.append(Bytes.toHexString(excepted));
        builder.append("]，实际检验码[");
        builder.append(Bytes.toHexString(actual));
        builder.append("]。数据内容：");
        builder.append(Bytes.toHexString(bytes));
        return builder.toString();
    }
}
