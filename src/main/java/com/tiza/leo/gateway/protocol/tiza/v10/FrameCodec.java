package com.tiza.leo.gateway.protocol.tiza.v10;

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

/**
 * Author: tz_wl
 * Date: 2021/4/18 11:23
 * Content:
 */
@Slf4j
public class FrameCodec implements ProtocolCodec {

    public static final String CMD_SN = "cmd_sn";
    public static final String VERSION = "version";

    private GatewayCacheManager cacheManager;

    public FrameCodec(GatewayCacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    /**
     * 上行数据 将数据从gps获取后  解码为MessageContext  往下流
     * @param in
     * @return
     */
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
        message.setRawBytes(bytes);   //已经做过了，不需要了吧
        return message;
    }


    /**

     * @param message
     * @param buf 存放编码后的数据
     *
    1	消息长度	2
    3	定位终端ID	5   第一个字节是 23
    8	协议版本号	1   当前版本号为2
    9	命令序号	2
    11	命令ID	1
    12	信息内容	—

     */
    @Override
    public void encode(MessageContext message, ByteBuf buf) {
        DeviceInfo deviceInfo = cacheManager.getDeviceInfo(message.getDeviceSn());
        if(deviceInfo == null){
            log.error("未获取到设备{}的缓存信息", message.getDeviceSn());
        }
        //开始长度位
        buf.writeShort(message.getCmdBytes().length + 11);
        //车载终端id
        buf.writeByte(0x23);
        buf.writeBytes(util.longToBytes(Long.parseLong(message.getDeviceSn()), 4));
        buf.writeByte(0x02);
        //命令序号
        Integer sn = message.getSn();
        if (sn == null) {
            sn = deviceInfo.getOutboundSn().getAndIncrement();
        }
        buf.writeShort(sn);
        //命令ID
        buf.writeByte(message.getCmd());
        //内容
        buf.writeBytes(message.getCmdBytes());
    }

    /**
     * 下行数据 对数据MessageContext 进行编码后 发给 GPS
     * @param message  存放编码后的数据
     */
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
        in.skipBytes(1);
        byte[] deviceSn = new byte[4];
        in.readBytes(deviceSn);
        message.setDeviceSn(String.valueOf(Bytes.unsignedLong(Endian.BIG, deviceSn)));
        message.put(VERSION, in.readByte());
        //命令序号
        message.put(CMD_SN, in.readUnsignedShort());
        //命令Id
        message.setCmd(in.readUnsignedByte());
        //指令内容 不定长度，指令后面全部内容
        byte[] content = new byte[length - 11];
        in.readBytes(content);
        message.setCmdBytes(content);

        in.resetReaderIndex();  //重置readIndex  然后再将所有数据读入MessageContext.RawBytes中去
        byte[] rawByte = new byte[length];
        in.readBytes(rawByte);
        message.setRawBytes(rawByte);

        log.info("FrameCodec  decode ----------------- 1111  收到终端数据[{}]  [{}]  ",message.getDeviceSn(),message.getCmd());

        return message;
    }
}
