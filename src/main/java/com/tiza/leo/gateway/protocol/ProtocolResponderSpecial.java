package com.tiza.leo.gateway.protocol;

import com.tiza.leo.gateway.message.MessageContext;

import java.util.Optional;

/**
 * Author: tz_wl
 * Date: 2021/4/18 10:40
 * Content:  应答机 -- 针对特殊情况 上传7F 指令就回传 让其修改IP 地址
 */
public interface ProtocolResponderSpecial {
    Optional<MessageContext> ackSpecial(MessageContext message);
}
