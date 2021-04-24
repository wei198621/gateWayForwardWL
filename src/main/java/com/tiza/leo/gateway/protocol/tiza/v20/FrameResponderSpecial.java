package com.tiza.leo.gateway.protocol.tiza.v20;

import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.ProtocolCodec;
import com.tiza.leo.gateway.protocol.ProtocolResponderSpecial;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Author: tz_wl
 * Date: 2021/4/18 12:22
 * Content:
 */
@Slf4j
public class FrameResponderSpecial implements ProtocolResponderSpecial {

    private ProtocolCodec codec;
    public FrameResponderSpecial(ProtocolCodec codec){
        this.codec = codec;
    }

    private byte[] heat = new byte[]{0x00, 0x02};

    private Set<Integer> respCmds= new HashSet(){{

        this.add(0x80);
        this.add(0x81);
        this.add(0x82);
        this.add(0x83);
        this.add(0x84);
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
    }};


    /**
     * 响应指令 （终端返回命令应答）（4.4.3.1.中心命令应答（命令ID：01H））
     *
     15	参数ID	    2
     17	参数ID长度	1
     18	参数值	   不定
     *
     * @param message
     * @return
     */
    @Override
    public Optional<MessageContext> ackSpecial(MessageContext message) {
        //return Optional.empty();
        MessageContext outBoundMessage = MessageContext.clone(message);
        if(respCmds.contains(message.getCmd())){

            ByteBuf buf = Unpooled.buffer(8);
            byte[] respContent;
            outBoundMessage.setCmd(0x04);
            //       2F 5C  E9  8B
            // 0201  47.92.233.139
            buf.writeBytes(new byte[]{ 0x00 , 0x06});  //参数ID
            buf.writeByte(0x04);   //参数ID长度
            buf.writeByte(0x2F);   //参数值
            buf.writeByte(0x5C);
            buf.writeByte(0xE9);
            buf.writeByte(0x8B);
            respContent = ByteBufUtil.getBytes(buf);
            buf.release();
            outBoundMessage.setCmdBytes(respContent);
            byte[] outBytes = codec.encode(outBoundMessage);
            outBoundMessage.setRawBytes(outBytes);

            try {
                String str = new String(respContent, "UTF-8");
                log.error(" 成功下发修改 IP 地址指令 ， 终端编号是[{}],  [{}],  [{}],  [{}] ,  [{}]",message.getDeviceSn(),message.getCmd()
                        ,outBoundMessage.getDeviceSn(), com.tiza.binary.Bytes.toHexString(outBoundMessage.getCmdBytes())
                        ,com.tiza.binary.Bytes.toHexString(outBoundMessage.getRawBytes())
                );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return Optional.of(outBoundMessage);
        }else{
            outBoundMessage.setCmdBytes(new byte[]{});
            return  Optional.empty();
        }
    }




}
