package com.tiza.leo.gateway.config;

import com.tiza.leo.gateway.cache.TerminalCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Author: tz_wl
 * Date: 2021/4/18 8:46
 * Content:
 * ComponentConfig 放置的是 1. 需要显示配置参数的类
 *                         2. 需要初始加载的类
 */
@Slf4j
@Configuration
public class ComponentConfig {

    @Autowired
    private Environment env;

    /**
     *     执行 TerminalCache 的 init 方法 加载待下发指令列表
     *     后面直接使用 TerminalCache.listTerminal  获取终端数据
     *     */
    @Bean(initMethod = "init")
    public TerminalCache terminalCache(){
        return new TerminalCache();
    }






}
