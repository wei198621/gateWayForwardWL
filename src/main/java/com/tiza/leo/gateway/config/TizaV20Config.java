package com.tiza.leo.gateway.config;

import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.handler.CacheHandler;
import com.tiza.leo.gateway.handler.ForwardTcpHandler;
import com.tiza.leo.gateway.handler.ForwardUdpHandler;
import com.tiza.leo.gateway.handler.FrameDecoderHandler;
import com.tiza.leo.gateway.handler.FrameEncoderHandler;
import com.tiza.leo.gateway.handler.ResponderHandler;
import com.tiza.leo.gateway.handler.ResponderSpecialHandler;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import com.tiza.leo.gateway.protocol.ProtocolResponder;
import com.tiza.leo.gateway.protocol.ProtocolResponderSpecial;
import com.tiza.leo.gateway.protocol.tiza.v20.FrameCodec;
import com.tiza.leo.gateway.protocol.tiza.v20.FrameResponder;
import com.tiza.leo.gateway.protocol.tiza.v20.FrameResponderSpecial;
import com.tiza.leo.gateway.utils.ForwardTcpClient;
import com.tiza.leo.gateway.utils.ForwardUdpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * Author: tz_wl
 * Date: 2021/4/18 18:26
 * Content:
 */
@Configuration
public class TizaV20Config {

    @Value("${gateway.socketprotocol}")
    private String socketProtocol;


    @Value("${forward.gateway.host}")
    private String forwardHost;
    @Value("${forward.gateway.port}")
    private int forwardPort;

    // 2.0 协议 80 指令向中心要网关地址，此处是网关地址
    @Value("${gateway.socket.host}")
    private String gateWaySocketHost;

    @Value("${gateway.socket.port}")
    private int gateWaySocketPort;

    /**
     * 设备信息缓存 localCache =(deviceId,DeviceInfo)   5 分钟后过期
     * @return
     */
    @Bean
    public GatewayCacheManager gatewayCacheManager(){
        return new GatewayCacheManager();
    }
    /**
     *   缓存消息处理管道
     * @param gatewayCacheManager
     * @return
     */
    @Bean
    public CacheHandler cacheHandler(GatewayCacheManager gatewayCacheManager){
        return new CacheHandler(gatewayCacheManager);
    }


    /**
     * 实例化  编码解码器
     * @param cacheManager
     * @return
     *  （不同协议不同的）FrameCodec 会实际执行 encode decode 将数据从 buf 转变为  MessageContext （或者逆过程）
     */
    @Bean
    ProtocolCodec protocolCodec(GatewayCacheManager cacheManager){
        return new FrameCodec(cacheManager);
    }
    /**
     * 将数据从 buf 转变为  MessageContext
     * @param protocolCodec
     * @return
     */
    @Bean
    public FrameDecoderHandler decoderHandler(ProtocolCodec protocolCodec){
        return new FrameDecoderHandler(protocolCodec);
    }

    /**
     * 协议编码器  按照 tcp udp 不同方式 写数据
     * @param protocolCodec
     * @param cacheManager
     * @return
     * 将数据回给GPS 需要走这块
     */
    @Bean
    public FrameEncoderHandler encoderHandler(ProtocolCodec protocolCodec, GatewayCacheManager cacheManager){
        return new FrameEncoderHandler(protocolCodec, socketProtocol, cacheManager);
    }


    /**
     * 响应终端信息
     * @param codec
     * @param cacheManager
     * @return
     * 启动一个线程 专门用于发送心跳数据 回给GPS
     */
    @Bean
    public ProtocolResponder protocolResponder(ProtocolCodec codec, GatewayCacheManager cacheManager){
        FrameResponder frameResponder = new FrameResponder(codec,this.gateWaySocketHost,this.gateWaySocketPort);
        frameResponder.heartBeat(cacheManager);
        return frameResponder;
    }
    @Bean
    public ResponderHandler responseHandler(ProtocolResponder protocolResponder){
        ResponderHandler responseHandler = new ResponderHandler(protocolResponder);
        return responseHandler;
    }

    //用于转发  UDP
    @Bean
    public ForwardUdpClient forwardUdpClient (FrameDecoderHandler decoderHandler, GatewayCacheManager cacheManager){
        InetSocketAddress forwardAddress = new InetSocketAddress(this.forwardHost,this.forwardPort);
        ForwardUdpClient forwardUdpClient = new ForwardUdpClient(forwardAddress,decoderHandler,cacheManager);
        forwardUdpClient.connect();
        return forwardUdpClient;
    }
    @Bean
    public ForwardUdpHandler forwardUdpHandler(ForwardUdpClient forwardUdpClient){
        return new ForwardUdpHandler(forwardUdpClient);
    }


    //用于下发  设置主中心IP地址
    @Bean
    public ProtocolResponderSpecial protocolResponderSpecial (ProtocolCodec codec, GatewayCacheManager cacheManager){
        FrameResponderSpecial frameResponderSpecial = new FrameResponderSpecial(codec);
        return frameResponderSpecial;
    }
    @Bean
    public ResponderSpecialHandler responderSpecialHandler(ProtocolResponderSpecial protocolResponderSpecial){
        ResponderSpecialHandler responderSpecialHandler = new ResponderSpecialHandler(protocolResponderSpecial);
        return  responderSpecialHandler;
    }




  /*  //用于转发  tcp
    @Bean
    public ForwardTcpClient forwardTcpClient (FrameDecoderHandler decoderHandler, GatewayCacheManager cacheManager){
        InetSocketAddress forwardAddress = new InetSocketAddress(this.forwardHost,this.forwardPort);
        ForwardTcpClient forwardTcpClient = new ForwardTcpClient(forwardAddress,decoderHandler,cacheManager);
        forwardTcpClient.connect();
        return forwardTcpClient;
    }
    @Bean
    public ForwardTcpHandler forwardTcpHandler(ForwardTcpClient forwardTcpClient){
        return new ForwardTcpHandler(forwardTcpClient);
    }*/



}
