package com.tiza.leo.gateway.utils;

import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.handler.FrameDecoderHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Author: tz_wl
 * Date: 2021/4/18 14:32
 * Content:
 */
@Slf4j
public class ForwardUdpClient {
    private InetSocketAddress forwardAddress;
    private FrameDecoderHandler decoderHandler;
    public GatewayCacheManager cacheManager;

    private Channel channel;
    public ForwardUdpClient(InetSocketAddress forwardAddress,FrameDecoderHandler decoderHandler,GatewayCacheManager cacheManager){
        this.forwardAddress=forwardAddress;
        this.decoderHandler=decoderHandler;
        this.cacheManager=cacheManager;
    }

    /*
    在 TizaV10Config 中 构造bean 的时候就 调用此方法
    连接到 forwardAddress
    获取到当前channel
     */
    public void connect(){
        EventLoopGroup bossGroup =new NioEventLoopGroup();
        Bootstrap bootstrap =new Bootstrap();
        try{
        bootstrap.group(bossGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        // 注意这里的顺序不能颠倒  第一个必须是decoder
                        pipeline.addLast("decoder",decoderHandler);
                        pipeline.addLast("sender",new UdpInboundSendHandler(cacheManager));
                    }
                });

        ChannelFuture future=bootstrap.connect(forwardAddress).sync();
        this.channel =future.channel();
        }
        catch (Exception ex){
            bossGroup.shutdownGracefully();
        }
    }

    public void send(ByteBuf buf){
        DatagramPacket packet =new DatagramPacket(buf,forwardAddress);
        channel.writeAndFlush(packet);
    }

}
