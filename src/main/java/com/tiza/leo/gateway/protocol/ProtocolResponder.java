package com.tiza.leo.gateway.protocol;

import com.tiza.leo.gateway.message.MessageContext;

import java.util.Optional;

/**
 * Author: tz_wl
 * Date: 2021/4/18 10:40
 * Content:  自动应答机
 */
public interface ProtocolResponder {
    Optional<MessageContext> ack (MessageContext message);
}
