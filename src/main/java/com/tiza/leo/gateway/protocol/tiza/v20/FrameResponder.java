package com.tiza.leo.gateway.protocol.tiza.v20;

import com.tiza.leo.gateway.cache.DeviceInfo;
import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import com.tiza.leo.gateway.protocol.ProtocolResponder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tiza.leo.gateway.protocol.tiza.v20.FrameCodec.CMD_SN;

/**
 * 自动应答机
 * Author: tz_wl
 * Date: 2021/4/18 18:09
 * Content:
 */

@Slf4j
public class FrameResponder implements ProtocolResponder {


    private ProtocolCodec codec;

    private String host;

    private int port;

    public FrameResponder(ProtocolCodec codec, String host, int port) {
        this.codec = codec;
        this.host = host;
        this.port = port;
    }

    private Set<Integer> respCmds= new HashSet(){
        {
            this.add(0x85);
            this.add(0x86);
            this.add(0x87);
            this.add(0x88);
            this.add(0x89);
            this.add(0x8A);
            this.add(0x8B);
            this.add(0x8C);
            this.add(0x8D);
            this.add(0x8E);
            this.add(0x8F);
        }
    };


    @Override
    public Optional<MessageContext> ack(MessageContext message) {

        MessageContext outBoundMessage = MessageContext.clone(message);
        if(respCmds.contains(message.getCmd())){
            ByteBuf buf= Unpooled.buffer(7);
            byte[] respContent;
            outBoundMessage.setCmd(0x02);
            buf.writeShort(Integer.parseInt(message.get(CMD_SN).toString()));
            buf.writeByte(message.getCmd());
            buf.writeByte(0x00);
            respContent= ByteBufUtil.getBytes(buf);
            outBoundMessage.setCmdBytes(respContent);
            buf.release();
        }else if(0x80==message.getCmd()){
            String apn="CMNET";
            ByteBuf buf = Unpooled.buffer();
            byte[] respContent;
            outBoundMessage.setCmd(0x01);
            buf.writeByte(apn.length());
            buf.writeBytes(apn.getBytes());
            //ip地址和端口号
            String[] ips = host.split("\\.");
            buf.writeBytes(new byte[]{(byte)Integer.parseInt(ips[0]),(byte)Integer.parseInt(ips[1]),(byte)Integer.parseInt(ips[2]),(byte)Integer.parseInt(ips[3])});
            buf.writeShort(port);
            respContent = ByteBufUtil.getBytes(buf);
            buf.release();
            outBoundMessage.setCmdBytes(respContent);
        }else{
            outBoundMessage.setCmdBytes(new byte[]{});
            return Optional.empty();
        }
        byte[] bytesOutMessage = codec.encode(outBoundMessage);
        outBoundMessage.setRawBytes(bytesOutMessage);
        return Optional.of(outBoundMessage);
    }
    public void heartBeat(GatewayCacheManager cacheManager) {
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        TimerTask timerTask = new TimerTask(cacheManager);
        // 十秒后开始心跳，每隔120秒发送一次心跳
        timer.scheduleAtFixedRate( timerTask, 1000*10, 1000*120, TimeUnit.MILLISECONDS);
    }

    public  class TimerTask implements Runnable {

        private GatewayCacheManager deviceCache;
        public TimerTask(GatewayCacheManager deviceCache){
            this.deviceCache = deviceCache;
        }


        @Override
        public void run() {
            try {
                if(deviceCache.getDeviceInfoAll().size()<1) {
                    return;
                }

                List<String> devices = deviceCache.getDeviceInfoAll().stream().collect(Collectors.toList());
                for(String device : devices){
                    DeviceInfo deviceInfo = deviceCache.getDeviceInfo(device);
                    if(deviceInfo.getLastUpTime() < (System.currentTimeMillis() - (300 * 1000))) {
                        deviceCache.evict(device);
                        continue;
                    }

                    MessageContext resp = deviceInfo.getMessageContext();
                    resp.setCmd(0);
                    resp.setCmdBytes(new byte[]{});
                    byte[] bytes = codec.encode(resp);
                    resp.setRawBytes(bytes);
                    deviceInfo.getChannel().writeAndFlush(resp);
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

        }
    }

}
