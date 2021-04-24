package com.tiza.leo.gateway.utils;

import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.handler.FrameDecoderHandler;
import com.tiza.leo.gateway.message.MessageContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Author: tz_wl
 * Date: 2021/4/18 14:32
 * Content:
 */
@Slf4j
public class ForwardTcpClient {

    private InetSocketAddress forwardAddress;
    private FrameDecoderHandler decoderHandler;
    public GatewayCacheManager cacheManager;

    private Channel channel;

    public ForwardTcpClient(InetSocketAddress forwardAddress, FrameDecoderHandler decoderHandler, GatewayCacheManager cacheManager) {
        this.forwardAddress = forwardAddress;
        this.decoderHandler = decoderHandler;
        this.cacheManager = cacheManager;
    }

     public void connect() {
         EventLoopGroup bossGroup = new NioEventLoopGroup();
         //EventLoopGroup workGourp = new NioEventLoopGroup();
         try {
             Bootstrap bootstrap = new Bootstrap();
             bootstrap.group(bossGroup)
                     .channel(NioSocketChannel.class)
                     .option(ChannelOption.SO_KEEPALIVE, true)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel channel) throws Exception {
                             ChannelPipeline pipeline = channel.pipeline();
                             // 注意这里的顺序不能颠倒  第一个必须是decoder
                             pipeline.addLast("decoder", decoderHandler);
                            pipeline.addLast("sender", new TcpInboundSendHandler(cacheManager));

                         }
                     });

             ChannelFuture future = bootstrap.connect(forwardAddress).sync();
             channel = future.channel();
              log.info( " channel   远程地址是[{}], 本地地址是[{}],  是否 open ? [{}]  isActive [{}]"
                ,channel.remoteAddress().toString()
                ,channel.localAddress()
                ,channel.isOpen() ,channel.isActive()
              );

         } catch (Exception ex) {
             log.error(ex.getMessage(), ex);
         } finally {
             bossGroup.shutdownGracefully();
         }
     }

    public void send(ByteBuf buf){
        log.info("send  remoteAddress is [{}]",channel.remoteAddress());
       /* log.info( " 远程地址是[{}], 本地地址是[{}],  是否 open ? [{}]  isActive [{}]"
                ,channel.remoteAddress().toString()
                ,channel.localAddress()
                ,channel.isOpen() ,channel.isActive()
        );
        */
        channel.writeAndFlush(buf);
    }

  /*  public void send(MessageContext messageContext){
        channel.writeAndFlush(messageContext);
    }*/

}
