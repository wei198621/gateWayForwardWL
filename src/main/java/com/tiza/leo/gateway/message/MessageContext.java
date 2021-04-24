package com.tiza.leo.gateway.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: tz_wl
 * Date: 2021/4/18 9:21
 * Content:  协议消息上下文
 */
@Getter
@Setter
public class MessageContext {
    /**
     * 设备地址
     */
    private SocketAddress recipient;
    /**
     * 设备序号
     */
    private String deviceSn;
    /**
     * 网关时间
     */
    private long gatewayTime;
    /**
     * 消息流水号
     */
    private Integer sn;
    /**
     * 数据流向
     */
    private Flow flow;
    /**
     * 命令标识
     */
    private int cmd;
    /**
     * 命令数据内容
     */
    private byte[] cmdBytes;
    /**
     * 数据帧内容
     */
    private byte[] rawBytes;


    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, Object> attributes = new HashMap<>();

    public void put(String key, Object value){
        attributes.put(key, value);
    }

    public Object get(String key){
        return attributes.get(key);
    }

    public static MessageContext clone(MessageContext message){
        MessageContext msgClone = new MessageContext();
        msgClone.gatewayTime = System.currentTimeMillis();
        msgClone.deviceSn = message.deviceSn;
        msgClone.flow = Flow.DOWN;
        msgClone.cmd = message.cmd;
        msgClone.attributes = message.attributes;
        return msgClone;
    }
}
