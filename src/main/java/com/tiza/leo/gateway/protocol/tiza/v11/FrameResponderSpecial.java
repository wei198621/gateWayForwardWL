package com.tiza.leo.gateway.protocol.tiza.v11;

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
    public Optional<MessageContext> ackSpecial(MessageContext message) {
        //return Optional.empty();
        MessageContext outBoundMessage = MessageContext.clone(message);
        if(respCmds.contains(message.getCmd())){
            ByteBuf buf = Unpooled.buffer(8);
            byte[] respContent;
            outBoundMessage.setCmd(0x04);

            //       2F 5C  E9  8B
            // 0201  47.92.233.139
            buf.writeBytes(new byte[]{ 0x02 , 0x01});
            buf.writeByte(0x2F);
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
