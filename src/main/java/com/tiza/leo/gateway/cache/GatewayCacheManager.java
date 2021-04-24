package com.tiza.leo.gateway.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.local.LocalAddress;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Author: tz_wl
 * Date: 2021/4/18 9:00
 * Content:
 *      设备信息缓存(deviceId,DeviceInfo)   5 分钟后过期
 *
 */
@Slf4j
public class GatewayCacheManager {
    private final Cache<String,DeviceInfo> localCache;
    public GatewayCacheManager(){
        localCache = CacheBuilder.newBuilder()
                .maximumSize(20000)
                .expireAfterAccess(Duration.ofMinutes(5))
                .build();
    }

    public DeviceInfo getDeviceInfo(String deviceSn){
        return localCache.getIfPresent(deviceSn);
    }

    public void putDeviceInfo(String deviceSn,DeviceInfo deviceInfo){
        localCache.put(deviceSn,deviceInfo);
    }

    public Set<String> getDeviceInfoAll(){
        return localCache.asMap().keySet();
    }

    public void evict(String deviceSn){
        localCache.invalidate(deviceSn);
    }

    public ConcurrentMap<String,DeviceInfo> asMap(){
        return this.localCache.asMap();
    }

}
