package com.tiza.leo.gateway.handler;

import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.CheckException;
import com.tiza.leo.gateway.protocol.IllegalFrameException;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * Author: tz_wl
 * Date: 2021/4/18 11:01
 * Content:
 */
@Slf4j
@ChannelHandler.Sharable
public class FrameDecoderHandler extends ChannelInboundHandlerAdapter {

    private ProtocolCodec codec;

    public FrameDecoderHandler(ProtocolCodec codec){
        this.codec = codec;
    }

    /**
     * 协议解码器
     * @param ctx
     * @param msg
     * @throws Exception
     * 1. 根据 msg 判断UDP 还是 TCP
     * 2. 调用 ProtocolCodec 接口对应类 处理数据  将流 转换为 MessageContext
     * 3. 往下发送
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        SocketAddress remoteAddress = null;
        if (msg instanceof ByteBuf) {
            buf = (ByteBuf) msg;
            remoteAddress = ctx.channel().remoteAddress();
        } else if (msg instanceof DatagramPacket) {
            DatagramPacket datagramPacket = (DatagramPacket) msg;
            buf = datagramPacket.content().copy();
            remoteAddress = datagramPacket.sender();
            datagramPacket.release();
        }

        try {
            MessageContext msgContext = codec.decode(buf);
            if (msgContext != null) {
                msgContext.setGatewayTime(System.currentTimeMillis());
                msgContext.setRecipient(remoteAddress);
                ctx.fireChannelRead(msgContext);
            }
        } catch (IllegalFrameException ex) {
            log.warn(ex.toString());
        } catch (CheckException ex) {
            log.warn(ex.toString());
        } finally {
            ReferenceCountUtil.release(buf);
        }
    }
}
