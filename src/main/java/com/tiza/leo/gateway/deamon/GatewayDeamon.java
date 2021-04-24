package com.tiza.leo.gateway.deamon;

import com.tiza.leo.gateway.handler.CacheHandler;
import com.tiza.leo.gateway.handler.ForwardTcpHandler;
import com.tiza.leo.gateway.handler.ForwardUdpHandler;
import com.tiza.leo.gateway.handler.FrameDecoderHandler;
import com.tiza.leo.gateway.handler.FrameEncoderHandler;
import com.tiza.leo.gateway.handler.ResponderHandler;
import com.tiza.leo.gateway.handler.ResponderSpecialHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Author: tz_wl
 * Date: 2021/4/18 7:39
 * Content:
 */
@Slf4j
@Component
public class GatewayDeamon {

    @Autowired
    private Environment env;

    @Value("${gateway.port}")
    private int port;


    //01 encode  decode  handler
    @Autowired
    private FrameDecoderHandler decoderHandler;
    @Autowired
    private FrameEncoderHandler encoderHandler;
    //02 消息缓存处理 handler
    @Autowired
    private CacheHandler cacheHandler;
    //03 处理响应  handler
    @Autowired
    private ResponderHandler responderHandler;

    //04 转发  handler    udp  / tcp
    @Autowired
    private ForwardUdpHandler forwardUdpHandler;
   /* @Autowired
    private ForwardTcpHandler forwardTcpHandler;*/

    //05 接收 7F（87） 指令 回传 下发修改主中心IP 命令
    @Autowired
    private ResponderSpecialHandler responderSpecialHandler;

    public void StartTCP() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("decoder",decoderHandler);
                            pipeline.addLast("encoder",encoderHandler);
                            pipeline.addLast("cache",cacheHandler);
                            //pipeline.addLast("responder", responderHandler);
                            //pipeline.addLast("forwardTcp",forwardTcpHandler);
                            pipeline.addLast("responderSpecial",responderSpecialHandler);
                        }
                    });
            log.info("GatewayDeamon-----StartTCP----绑定端口:[{}]", port);
            ChannelFuture future = null;
            future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


    public void StartUDP(){
        //log.info("GatewayDeamon  start");
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(bossGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                .option(ChannelOption.SO_RCVBUF,2*1024*1024)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("decoder",decoderHandler);
                        pipeline.addLast("encoder",encoderHandler);
                        pipeline.addLast("cache",cacheHandler);
                        //pipeline.addLast("responder", responderHandler);
                        pipeline.addLast("responderSpecial",responderSpecialHandler);
                        pipeline.addLast("forwardUdp",forwardUdpHandler);
                        //...

                    }
                });
        log.info("GatewayDeamon-----StartUDP----绑定端口:[{}]",port);

            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
