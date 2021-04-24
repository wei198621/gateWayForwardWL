package com.tiza.leo.gateway.handler;

import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.utils.ForwardUdpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: tz_wl
 * Date: 2021/4/18 14:29
 * Content:
 */
@Slf4j
public class ForwardUdpHandler extends ChannelInboundHandlerAdapter {

    private ForwardUdpClient forwardUdpClient;

    public ForwardUdpHandler(ForwardUdpClient forwardUdpClient) {
        this.forwardUdpClient = forwardUdpClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageContext messageContext= (MessageContext) msg;
        ByteBuf buf = Unpooled.copiedBuffer(messageContext.getRawBytes());
        forwardUdpClient.send(buf);
        log.info("ForwardUdpHandler channelRead ------ 4444  收到终端数据[{}]  [{}] ",messageContext.getDeviceSn(),messageContext.getCmd());

        ctx.fireChannelRead(msg);
    }
}
