package com.tiza.leo.gateway.protocol.tiza.v10;

import com.tiza.leo.gateway.cache.DeviceInfo;
import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import com.tiza.leo.gateway.protocol.ProtocolResponder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tiza.leo.gateway.protocol.tiza.v10.FrameCodec.CMD_SN;

/**
 * Author: tz_wl
 * Date: 2021/4/18 12:22
 * Content:
 */
@Slf4j
public class FrameResponder implements ProtocolResponder {

    private ProtocolCodec codec;
    public FrameResponder(ProtocolCodec codec){
        this.codec = codec;
    }

    private byte[] heat = new byte[]{0x00, 0x02};

    private Set<Integer> respCmds= new HashSet(){{
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
    }};


    /**
     * 响应指令 （终端返回命令应答）（4.4.3.1.中心命令应答（命令ID：01H））
     *
     * 中心命令应答（命令ID：01H）
     000F23D59FB0F602000B01004A7F00
     000F     ----1	消息长度
     23       ----3	厂家编号
     D59FB0F6 ----4	终端ID
     02       ----8	协议版本号
     000B     ----9	命令序号
     01       ----11	命令ID
     004A     ----12	应答命令序号
     7F       ----14	应答那条命令ID （73 74 ... 7F）
     00       ----15	处理结果    (0：成功   1：失败)
     *
     * @param message
     * @return
     */
    @Override
    public Optional<MessageContext> ack(MessageContext message) {
        //return Optional.empty();
        MessageContext outBoundMessage = MessageContext.clone(message);
        if(respCmds.contains(message.getCmd())){
            ByteBuf buf = Unpooled.buffer(7);
            byte[] respContent;
            outBoundMessage.setCmd(0x01);
            buf.writeShort(Integer.parseInt(message.get(CMD_SN).toString()));
            buf.writeByte(message.getCmd());
            buf.writeByte(0x00);
            respContent = ByteBufUtil.getBytes(buf);
            buf.release();
            outBoundMessage.setCmdBytes(respContent);
            byte[] outBytes = codec.encode(outBoundMessage);
            outBoundMessage.setRawBytes(outBytes);
            return Optional.of(outBoundMessage);
        }else{
            outBoundMessage.setCmdBytes(new byte[]{});
            return  Optional.empty();
        }
    }

    public void heartBeat(GatewayCacheManager cacheManager) {
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        TimerTask timerTask = new TimerTask(cacheManager);
        // 30秒后开始心跳，每隔120秒发送一次心跳
        timer.scheduleAtFixedRate(timerTask, 30* 1000, 2*60 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 将心跳信息沿着 缓存中的 设备信心中的 channel  ,发回去
     */
    public class TimerTask implements Runnable{
        private GatewayCacheManager deviceCache;
        public TimerTask(GatewayCacheManager deviceCache){
            this.deviceCache =deviceCache;
        }

        @Override
        public void run() {
            try {
                if (deviceCache.getDeviceInfoAll().size() < 1) {
                    return;
                }
                List<String> deviceSnList = deviceCache.getDeviceInfoAll().stream().collect(Collectors.toList());
                for (String deviceSn : deviceSnList) {
                    DeviceInfo deviceInfo = deviceCache.getDeviceInfo(deviceSn);
                    if (deviceInfo.getLastUpTime() < (System.currentTimeMillis() - (5 * 60 * 1000))) {
                        deviceCache.evict(deviceSn);
                        continue;
                    }
                    MessageContext respMessage = deviceInfo.getMessageContext();
                    respMessage.setCmd(0x70);
                    respMessage.setRawBytes(heat);
                    deviceInfo.getChannel().writeAndFlush(respMessage);
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }


}
