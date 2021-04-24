package com.tiza.leo.gateway.utils;

import com.tiza.leo.gateway.cache.DeviceInfo;
import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.message.MessageContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: tz_wl
 * Date: 2021/4/18 14:41
 * Content:
 */
@Slf4j
public class UdpInboundSendHandler extends ChannelInboundHandlerAdapter {

    private GatewayCacheManager cacheManager;
    public UdpInboundSendHandler(GatewayCacheManager cacheManager){
        this.cacheManager=cacheManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageContext messageContext =(MessageContext)msg;
        DeviceInfo deviceInfo = cacheManager.getDeviceInfo(messageContext.getDeviceSn());
        deviceInfo.getChannel().writeAndFlush(messageContext);   //将MessageContext 延管道发回
        ctx.fireChannelRead(msg);                                // 继续往下流

    }
}
