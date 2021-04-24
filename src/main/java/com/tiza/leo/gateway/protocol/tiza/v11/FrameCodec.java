package com.tiza.leo.gateway.protocol.tiza.v11;

import com.tiza.binary.Bytes;
import com.tiza.binary.Endian;
import com.tiza.leo.gateway.cache.DeviceInfo;
import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.FrameIncompleteException;
import com.tiza.leo.gateway.protocol.IllegalFrameException;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import com.tiza.leo.gateway.utils.util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

/**   数据帧解码器
 * Author: tz_wl
 * Date: 2021/4/18 17:17
 * Content:
 */
@Slf4j
public class FrameCodec  implements ProtocolCodec {

    public static final String CMD_SN = "cmd_sn";
    //public static final String VERSION = "version";

    private GatewayCacheManager cacheManager;

    public FrameCodec(GatewayCacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    @Override
    public MessageContext decode(ByteBuf in) {
        int length = in.readableBytes();
        if (length < 10) {
            return null;
        }

        in.markReaderIndex();
        try {
            MessageContext message = parse(in);
            return message;
        } catch (FrameIncompleteException ex) {
            log.info(ex.toString());
            in.resetReaderIndex();
            return null;
        }
    }

    @Override
    public MessageContext decode(byte[] bytes) {
        if (bytes.length < 10) {
            throw new IllegalFrameException("协议格式错误", bytes);
        }

        ByteBuf in = Unpooled.copiedBuffer(bytes);
        MessageContext message = parse(in);
        in.release();
        message.setRawBytes(bytes);

        log.info(" 收到 来自 gps 数据 [{}] ,[{}]",message.getDeviceSn(),message.getCmd());
        return message;
    }

    @Override
    public void encode(MessageContext message, ByteBuf buf) {
        DeviceInfo deviceInfo = cacheManager.getDeviceInfo(message.getDeviceSn());
        if(deviceInfo == null){
            log.error("未获取到设备{}的缓存信息", message.getDeviceSn());
        }

        //开始长度位
        buf.writeShort(message.getCmdBytes().length + 11);
        //车载终端id
        buf.writeBytes(util.longToBytes(Long.parseLong(message.getDeviceSn()), 6));
        //命令序号
        Integer sn = message.getSn();
        if(deviceInfo.getOutboundSn() == null){
            log.error("{}deviceInfo未获取到sn", message.getDeviceSn());
        }
        if (sn == null) {
            sn = deviceInfo.getOutboundSn().getAndIncrement();
        }
        buf.writeShort(sn);
        //命令ID
        buf.writeByte(message.getCmd());
        //内容
        buf.writeBytes(message.getCmdBytes());
    }

    @Override
    public byte[] encode(MessageContext message) {
        ByteBuf buf = Unpooled.buffer();
        encode(message, buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(0, bytes);
        return bytes;
    }


    public MessageContext parse(ByteBuf in) {
        MessageContext message = new MessageContext();
        byte[] bytes = new byte[in.readableBytes()];
        in.getBytes(in.readerIndex(), bytes);

        int length = in.readUnsignedShort();

        if (length != in.readableBytes() + 2) {
            throw new IllegalFrameException("协议长度错误", bytes);
        }

        //定位终端id
        byte[] deviceSn = new byte[6];
        in.readBytes(deviceSn);
        message.setDeviceSn(String.valueOf(Bytes.unsignedLong(Endian.BIG, deviceSn)));

        //命令序号
        message.put(CMD_SN, in.readUnsignedShort());
        //命令Id
        message.setCmd(in.readUnsignedByte());
        //指令内容 不定长度，指令后面全部内容
        byte[] content = new byte[length - 11];
        in.readBytes(content);
        message.setCmdBytes(content);

        in.resetReaderIndex();
        byte[] rawByte = new byte[length];
        in.readBytes(rawByte);
        message.setRawBytes(rawByte);
        return message;
    }

}
