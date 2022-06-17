/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid;

import com.alibaba.druid.wall.WallConfig;
import com.baomidou.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 防火墙配置工具类
 *
 * @author Taoyu
 * @since 3.5.0
 */
@Slf4j
public final class DruidWallConfigUtil {

    private static final Map<String, Method> METHODS = DsConfigUtil.getSetterMethods(WallConfig.class);

    /**
     * 根据当前的配置和全局的配置生成druid防火墙配置
     *
     * @param c 当前配置
     * @param g 全局配置
     * @return 防火墙配置
     */
    public static WallConfig toWallConfig(Map<String, Object> c, Map<String, Object> g) {
        WallConfig wallConfig = new WallConfig();
        Map<String, Object> map = DsConfigUtil.mergeConfig(c, g);
        Object dir = map.get("dir");
        if (dir != null) {
            wallConfig.loadConfig(String.valueOf(dir));
        }
        for (Map.Entry<String, Object> item : map.entrySet()) {
            String key = DsConfigUtil.lineToUpper(item.getKey());
            Method method = METHODS.get(key);
            if (method != null) {
                try {
                    method.invoke(wallConfig, DsConfigUtil.convertValue(method,item.getValue()));
                } catch (Exception e) {
                    log.warn("druid wall set param {} error", key, e);
                }
            } else {
                log.warn("druid wall does not have param {}", key);
            }
        }
        return wallConfig;
    }
}
