package com.tiza.leo.gateway.handler;

import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.utils.ForwardTcpClient;
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
public class ForwardTcpHandler extends ChannelInboundHandlerAdapter {
    private ForwardTcpClient forwardTcpClient;

    public ForwardTcpHandler(ForwardTcpClient forwardTcpClient) {
        this.forwardTcpClient = forwardTcpClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageContext messageContext= (MessageContext) msg;
        ByteBuf buf = Unpooled.copiedBuffer(messageContext.getRawBytes());
        //forwardTcpClient.send(messageContext);
        forwardTcpClient.send(buf);
        ctx.fireChannelRead(msg);
    }
}
