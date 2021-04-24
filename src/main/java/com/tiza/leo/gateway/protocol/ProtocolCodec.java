package com.tiza.leo.gateway.protocol;

import com.tiza.leo.gateway.message.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Author: tz_wl
 * Date: 2021/4/18 11:08
 * Content: 协议编解码器
 */
public interface ProtocolCodec {
    /**
     * 解码ByteBuf中的协议内容，包含处理TCP拆包的逻辑
     * @param in
     * @return
     */
    MessageContext decode(ByteBuf in);

    /**
     * 解码协议内容
     * @param bytes
     * @return
     */
    MessageContext decode(byte[] bytes);

    /**
     * 编码消息内容
     * @param message
     * @param buf 存放编码后的数据
     * @return
     */
    void encode(MessageContext message, ByteBuf buf);

    /**
     * 编码消息内容
     * @param message
     * @return
     */
    byte[] encode(MessageContext message);
}
