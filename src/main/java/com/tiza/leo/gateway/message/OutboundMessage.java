package com.tiza.leo.gateway.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Author: tz_wl
 * Date: 2021/4/18 9:25
 * Content:  下发消息
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboundMessage {
    /**
     * 命令Id
     */
    private int cmdId;
    /**
     * 命令数据
     */
    private byte[] cmdBytes;
}
