package com.tiza.leo.gateway.handler;

import com.tiza.binary.Bytes;
import com.tiza.leo.gateway.cache.DeviceInfo;
import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;


/**
 * 协议编码器
 * Author: tz_wl
 * Date: 2021/4/18 13:18
 * Content:  将数据回给GPS 需要走这块
 */
@ChannelHandler.Sharable
@Slf4j
public class FrameEncoderHandler extends ChannelOutboundHandlerAdapter {

    private ProtocolCodec codec;
    private GatewayCacheManager cacheManager;
    private String socketProtocol;

    public FrameEncoderHandler(ProtocolCodec codec,String socketProtocol,GatewayCacheManager cacheManager){
        this.codec=codec;
        this.socketProtocol = socketProtocol;
        this.cacheManager =cacheManager;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        MessageContext context =  (MessageContext)msg ;
        if(context.getRawBytes()!=null){
            buf.writeBytes(context.getRawBytes());
        }else {
            codec.encode(context,buf);
        }

        promise.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(!future.isSuccess()){
                    log.warn("向{}发送数据{}失败", context.getDeviceSn(), Bytes.toHexString(context.getRawBytes()));
                    log.warn(future.cause().getMessage(), future.cause());
                }
            }
        });
        if(socketProtocol.equalsIgnoreCase("TCP")){
            super.write(ctx,buf,promise);
        }else if(socketProtocol.equalsIgnoreCase("UDP")){
            String deviceSn = context.getDeviceSn();
            DeviceInfo deviceInfo = cacheManager.getDeviceInfo(deviceSn);
            DatagramPacket datagramPacket = new DatagramPacket(buf, deviceInfo.getRecipient());
            if(!ctx.channel().isOpen()){
                log.error("ChannelHandlerContext  channel is not open");
            }
            super.write(ctx,datagramPacket,promise);
        }
        super.flush(ctx);
    }
}
