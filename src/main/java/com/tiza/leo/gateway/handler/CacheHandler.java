package com.tiza.leo.gateway.handler;

import com.tiza.leo.gateway.cache.DeviceInfo;
import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.message.MessageContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: tz_wl
 * Date: 2021/4/18 9:52
 * Content:  缓存处理
 * 从上一步的 channel 取出数据 （MessageContext）
 * 以 deviceSn 为 key 缓存 消息
 * 然后
 * 发到下一个channel
 */
@Slf4j
@ChannelHandler.Sharable
public class CacheHandler extends ChannelInboundHandlerAdapter {

    private GatewayCacheManager cacheManager;
    public CacheHandler(GatewayCacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    /**
     * @param ctx
     * @param msg
     * @throws Exception
     *
     * 1. 如果缓存中没有消息       将消息缓存起来
     * 2. 如果缓存中的消息内容与当前不一致  更新缓存
     * 3. 消息往下发送
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        MessageContext context= (MessageContext) msg;
        String deviceSn = context.getDeviceSn();
        // step 01 添加消息到缓存
        DeviceInfo deviceInfo = cacheManager.getDeviceInfo(deviceSn);
        if(deviceInfo == null){
            DeviceInfo.DeviceInfoBuilder builder =DeviceInfo.builder()
                    .messageContext(context)
                    .channel(ctx.channel())
                    .recipient((InetSocketAddress)context.getRecipient())
                    .outboundSn(new AtomicInteger(0));
            deviceInfo =builder.build();
            log.debug("缓存{}设备信息{}",deviceSn,deviceInfo.toString());
            cacheManager.putDeviceInfo(deviceSn,deviceInfo);
        }
        //step 02 更新消息
        deviceInfo.setMessageContext(context);
        Channel newChannel = ctx.channel();
        if(deviceInfo.getChannel() !=null && deviceInfo.getChannel()!=newChannel){
            log.info("channel replaced ,old :[{}],new:[{}] ,old address:[{}],new address:[{}]"
                    ,deviceInfo.getChannel()
                    ,newChannel
                    , deviceInfo.getChannel().remoteAddress()
                    ,newChannel.remoteAddress()
            );
            deviceInfo.getChannel().alloc().buffer().release();
            deviceInfo.getChannel().close();
        }
        deviceInfo.setChannel(newChannel);
        deviceInfo.setRecipient((InetSocketAddress) context.getRecipient());
        deviceInfo.setLastUpTime(System.currentTimeMillis());


        log.info("CacheHandler channelRead ----------- 2222  收到终端数据[{}]  [{}] ",context.getDeviceSn(),context.getCmd());
        //step 03 往下面管道发送
        ctx.fireChannelRead(msg);
    }
}
