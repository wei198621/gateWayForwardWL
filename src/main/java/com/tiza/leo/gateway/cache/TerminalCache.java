package com.tiza.leo.gateway.cache;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.google.errorprone.annotations.Var;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: tz_wl
 * Date: 2021/4/18 9:03
 * Content:  从配置文件取出终端列表
 *
 */
@Slf4j
public class TerminalCache {

    public static List listTerminal =new ArrayList();
    public void init(){
        buildData();
    }
    public void buildData(){
        String path="terminalList.txt";
        String strTerminals = ResourceUtil.readUtf8Str(path);
        listTerminal=JSONUtil.parseArray(strTerminals);
        log.info("===========将要转发指令终端列表============");
        log.info(strTerminals);
    }

}
