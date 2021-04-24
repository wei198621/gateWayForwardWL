package com.tiza.leo.gateway.protocol.tiza.v20;

import com.tiza.binary.Bytes;
import com.tiza.binary.BytesCheck;
import com.tiza.binary.Endian;
import com.tiza.leo.gateway.cache.DeviceInfo;
import com.tiza.leo.gateway.cache.GatewayCacheManager;
import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.CheckException;
import com.tiza.leo.gateway.protocol.FrameIncompleteException;
import com.tiza.leo.gateway.protocol.IllegalFrameException;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import com.tiza.leo.gateway.utils.util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据帧解码器
 *
 * Author: tz_wl
 * Date: 2021/4/18 18:05
 * Content:
 */
@Slf4j
public class FrameCodec implements ProtocolCodec {

    public static final String ATTR_SOFT = "soft";
    public static final String FACTORY = "factory";
    public static final String TERMINAL = "terminal";
    public static final String CONSUMER = "consumer";
    public static final String CMD_SN = "cmd_sn";

    private GatewayCacheManager cacheManager;
    public FrameCodec(GatewayCacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    @Override
    public MessageContext decode(ByteBuf in) {
        int length =  in.readableBytes();
        if(length < 15) {
            return null;
        }

        in.markReaderIndex();
        try {
            MessageContext message = parse(in);
            return message;
        }catch (FrameIncompleteException ex){
            log.info(ex.toString());
            in.resetReaderIndex();
            return null;
        }
    }

    @Override
    public MessageContext decode(byte[] bytes) {
        if(bytes.length < 15)
            throw new IllegalFrameException("协议格式错误", bytes);

        ByteBuf in = Unpooled.copiedBuffer(bytes);
        MessageContext message = parse(in);
        in.release();
        message.setRawBytes(bytes);
        return message;
    }

    @Override
    public void encode(MessageContext message, ByteBuf buf) {
        DeviceInfo deviceInfo = cacheManager.getDeviceInfo(message.getDeviceSn());
        if(deviceInfo == null){
            log.error("未获取到设备{}的缓存信息", message.getDeviceSn());
        }

        //开始长度位
        buf.writeShort(message.getCmdBytes().length + 14);
        //车载终端id
        buf.writeBytes(util.longToBytes(Long.parseLong(message.getDeviceSn()),5));
        //版本
        buf.writeByte(Integer.parseInt(message.get(ATTR_SOFT).toString()));
        //厂家
        buf.writeByte(Integer.parseInt(message.get(FACTORY).toString()));
        //终端
        buf.writeByte(Integer.parseInt(message.get(TERMINAL).toString()));
        //使用方
        buf.writeByte(Integer.parseInt(message.get(CONSUMER).toString()));
        //命令序号
        Integer sn = message.getSn();
        if(sn == null)
            sn = deviceInfo.getOutboundSn().getAndIncrement();
        buf.writeShort(sn);
        //命令ID
        buf.writeByte(message.getCmd());
        //内容
        buf.writeBytes(message.getCmdBytes());
        //校验位
        byte[] checkBytes = new byte[message.getCmdBytes().length+14];
        buf.getBytes(0, checkBytes);
        byte bbcByte = BytesCheck.bbc(checkBytes);
        buf.writeByte(bbcByte);
        //结束位
        buf.writeBytes(new byte[]{0x0D,0x0A});
    }

    @Override
    public byte[] encode(MessageContext message) {
        ByteBuf buf = Unpooled.buffer();
        encode(message, buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(0, bytes);
        buf.release();
        return bytes;
    }


    public MessageContext parse(ByteBuf in) {
        MessageContext message = new MessageContext();
        byte[] bytes = new byte[in.readableBytes()];
        in.getBytes(in.readerIndex(), bytes);
        //length 后三位不包含在length中
        int length = in.readUnsignedShort();

        if(length != in.readableBytes()-1)
            throw new IllegalFrameException("协议长度错误", bytes);

        //车载信息终端id
        byte[] deviceSn = new byte[5];
        in.readBytes(deviceSn);
        message.setDeviceSn(String.valueOf(Bytes.unsignedLong(Endian.BIG,deviceSn)));
        //版本号
        message.put(ATTR_SOFT, in.readByte());
        //厂家编号
        message.put(FACTORY, in.readByte());
        //终端编号
        message.put(TERMINAL, in.readByte());
        //使用方编号
        message.put(CONSUMER, in.readByte());
        //命令序号
        message.put(CMD_SN, in.readUnsignedShort());
        //命令Id
        message.setCmd(in.readUnsignedByte());
        //指令内容 不算后三位  17
        byte[] content = new byte[length-14];
        in.readBytes(content);
        message.setCmdBytes(content);

        byte checkByte =  in.readByte();
        byte bbc = BytesCheck.bbc(bytes,0,length);
        if(checkByte!=bbc){
            String msg = message.getDeviceSn()+"校验码错误：";
            throw  new CheckException(msg,bytes,new byte[]{checkByte},new byte[] {bbc});
        }
        if(0x0D !=in.readByte() && 0x0A != in.readByte())
            throw  new IllegalFrameException("该协议结束位有误", bytes);

        in.resetReaderIndex();
        byte[] rawByte = new byte[3+length];
        in.readBytes(rawByte);
        message.setRawBytes(rawByte);

        return message;
    }

}
