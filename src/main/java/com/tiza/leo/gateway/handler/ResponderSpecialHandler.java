package com.tiza.leo.gateway.handler;

import com.tiza.leo.gateway.cache.TerminalCache;
import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.ProtocolResponderSpecial;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Author: tz_wl
 * Date: 2021/4/18 16:40
 * Content:
 */
@Slf4j
@ChannelHandler.Sharable
public class ResponderSpecialHandler extends ChannelInboundHandlerAdapter {

    ProtocolResponderSpecial responderSpecial;

    public ResponderSpecialHandler(ProtocolResponderSpecial responderSpecial) {
        this.responderSpecial = responderSpecial;
    }

    /**
     * 如果待更新列表中有此终端编号  进入此流程
     *  判断如果是7F指令就下发 设置主中心地址
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageContext message= (MessageContext) msg;

        if(TerminalCache.listTerminal.contains(message.getDeviceSn())) {
            Optional<MessageContext> outBoundMessage = responderSpecial.ackSpecial(message);
            if (outBoundMessage == null) {
                return;
            }
            if (outBoundMessage.isPresent()) {
                ctx.pipeline().writeAndFlush(outBoundMessage.get());
            }
        }
        log.info("ResponderSpecialHandler channelRead  3333  收到终端数据[{}]  [{}] ",message.getDeviceSn(),message.getCmd());

        ctx.fireChannelRead(msg);
    }
}
