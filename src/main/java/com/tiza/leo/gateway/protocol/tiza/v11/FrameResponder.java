package com.tiza.leo.gateway.protocol.tiza.v11;

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

import static com.tiza.leo.gateway.protocol.tiza.v11.FrameCodec.CMD_SN;

/**
 * Author: tz_wl
 * Date: 2021/4/18 17:26
 * Content:
 */
@Slf4j
public class FrameResponder  implements ProtocolResponder {


    private ProtocolCodec codec;
    public FrameResponder(ProtocolCodec codec){
        this.codec = codec;
    }

    private  byte[] heat = new byte[]{0x00,0x02};


    private Set<Integer> respCmds= new HashSet(){
        {
            this.add(0x73);
            this.add(0x74);
            this.add(0x75);
            this.add(0x77);
            this.add(0x78);
            this.add(0x7A);
            this.add(0x7B);
            this.add(0x7C);
            this.add(0x7D);
            this.add(0x7E);
            this.add(0x7F);
            this.add(0x80);
            this.add(0x81);
            this.add(0x82);
            this.add(0x83);
            this.add(0x8B);
            this.add(0x91);
            this.add(0x92);
            this.add(0x93);
            this.add(0x98);
            this.add(0xA0);
            this.add(0xA1);
            this.add(0xB1);
            this.add(0xB2);
            this.add(0xB4);
            this.add(0xB5);
            this.add(0xB6);
            this.add(0xB7);
        }
    };


    @Override
    public Optional<MessageContext> ack(MessageContext message) {
        MessageContext outBoundMessage = MessageContext.clone(message);
        if(respCmds.contains(message.getCmd())) {
            ByteBuf buf = Unpooled.buffer(7);
            byte[] respContent;
            outBoundMessage.setCmd(0x01);
            buf.writeShort(Integer.parseInt(message.get(CMD_SN).toString()));
            buf.writeByte(message.getCmd());
            //成功00
            buf.writeByte(0x00);
            //回应时设置cmd 2020-01-08 by lbc 设置获取有意义byte
            respContent = ByteBufUtil.getBytes(buf);
            buf.release();
            outBoundMessage.setCmdBytes(respContent);
            byte[] bytes = codec.encode(outBoundMessage);
            outBoundMessage.setRawBytes(bytes);
            return Optional.of(outBoundMessage);
        }else {
            outBoundMessage.setCmdBytes(new byte[]{});
            return Optional.empty();
        }
    }

    public void heartBeat(GatewayCacheManager cacheManager) {
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        TimerTask timerTask = new TimerTask(cacheManager);
        // 30秒后开始心跳，每隔120秒发送一次心跳
        timer.scheduleAtFixedRate( timerTask, 30*1000, 2*60*1000, TimeUnit.MILLISECONDS);
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
                    resp.setCmd(0x70);
                    resp.setRawBytes(heat);
                    deviceInfo.getChannel().writeAndFlush(resp);
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }

}
