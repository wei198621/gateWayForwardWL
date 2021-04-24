package com.tiza.leo.gateway.cache;

import com.tiza.leo.gateway.message.MessageContext;
import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: tz_wl
 * Date: 2021/4/18 8:59
 * Content:
 */

@Getter
@Setter
@Builder
@ToString
/**
 * 设备缓存信息
 */
public class DeviceInfo{
    private MessageContext messageContext;
    private Channel channel;
    private InetSocketAddress recipient;
    //流数据管道
    private String streamTopic;
    //下行指令序号
    private AtomicInteger outboundSn;
    //机构分支路径
    private String orgBranch;
    //上行数据包数量
    private int upPackets;
    //上行数据流量
    private int upBytes;
    //下行数据包数量
    private int downPackets;
    //下行数据包流量
    private int downBytes;
    //最后上行数据时间戳
    private long lastUpTime;

    public void increaseUp(int bytesLength){
        this.upPackets++;
        this.upBytes += bytesLength;
    }

    public void increaseDown(int bytesLength){
        this.downPackets++;
        this.downBytes += bytesLength;
    }

    public void resetCounter(){
        upPackets = 0;
        upBytes = 0;
        downPackets = 0;
        downBytes = 0;
    }
}