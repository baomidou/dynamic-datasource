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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.toolkit.DsPropertyFingerprint;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源配置刷新编排器。
 * <p>
 * 框架本身不绑定任何配置中心（Nacos / Apollo / Spring Cloud Config ...），但把「拿到一份新配置之后
 * 怎么安全地落到运行中的连接池」这件最容易写错的事收进来：
 * <ol>
 *     <li><b>只动变化的</b>：按配置指纹 diff，未变化的库保持原连接池实例不动——避免「改一个库，全库重建」；</li>
 *     <li><b>空配置闸门</b>：新配置为空时直接拒绝，不删任何库——避免配置中心误操作把数据源团灭；</li>
 *     <li><b>删除熔断</b>：单次删除比例超过阈值时拒绝删除，只做新增/更新；</li>
 *     <li><b>失败隔离</b>：单个库创建失败保持旧池可用，不影响其他库；</li>
 *     <li><b>只管自己认识的</b>：运行期手工 {@code addDataSource} 加进来的库不在快照里，不会被误删。</li>
 * </ol>
 * <p>
 * 接入方式（以 Spring Cloud 为例）：
 * <pre>
 * &#64;EventListener(RefreshEvent.class)
 * public void onRefresh() {
 *     refresher.refresh(properties.getDatasource());
 * }
 * </pre>
 *
 * @author TaoYu
 * @see DynamicRoutingDataSource#addDataSource(String, DataSource)
 * @see DynamicRoutingDataSource#removeDataSource(String)
 * @since 4.7.0
 */
@Slf4j
public class DynamicDataSourceRefresher implements InitializingBean {

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final DefaultDataSourceCreator dataSourceCreator;
    private final DynamicDataSourceProperties properties;

    /**
     * 当前托管的数据源 -&gt; 配置指纹
     */
    private final Map<String, String> fingerprints = new ConcurrentHashMap<>();

    /**
     * 单次刷新允许删除的比例上限，超过则拒绝删除（仍在 {@link Result#isRemovalRejected()} 中标记）
     */
    @Setter
    private double removalThreshold = 0.5D;

    public DynamicDataSourceRefresher(ObjectProvider<DataSource> dataSourceProvider,
                                      DefaultDataSourceCreator dataSourceCreator,
                                      DynamicDataSourceProperties properties) {
        this.dataSourceProvider = dataSourceProvider;
        this.dataSourceCreator = dataSourceCreator;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        baseline();
    }

    /**
     * 建立基线：把启动时已加载的那份配置记为「已应用」，这样首次刷新才不会把它们当成新增而全部重建。
     * <p>
     * 自定义 provider（例如从引导库加载）场景下，启动配置不在 {@link DynamicDataSourceProperties} 里，
     * 需要在拿到首份配置后自行调用本方法建立基线。
     */
    public void baseline() {
        Map<String, DataSourceProperty> datasource = properties.getDatasource();
        if (datasource == null || datasource.isEmpty()) {
            return;
        }
        datasource.forEach((name, property) -> fingerprints.put(name, fingerprint(name, property)));
        log.debug("dynamic-datasource refresh baseline established with [{}] datasource", fingerprints.size());
    }

    /**
     * 用新配置刷新数据源
     *
     * @param newProperties 新的数据源配置，通常来自配置中心
     * @return 本次刷新的结果，便于日志与测试断言
     */
    public synchronized Result refresh(Map<String, DataSourceProperty> newProperties) {
        Result result = new Result();
        DataSource bean = dataSourceProvider.getIfAvailable();
        if (!(bean instanceof DynamicRoutingDataSource)) {
            String reason = "current DataSource is not a DynamicRoutingDataSource, refresh rejected";
            log.error("dynamic-datasource {}", reason);
            result.setRejectReason(reason);
            return result;
        }
        DynamicRoutingDataSource routingDataSource = (DynamicRoutingDataSource) bean;

        if (newProperties == null || newProperties.isEmpty()) {
            String reason = "resolved an empty datasource config, refresh rejected to avoid removing all datasource";
            log.error("dynamic-datasource {}", reason);
            result.setRejectReason(reason);
            return result;
        }

        List<String> toRemove = new ArrayList<>();
        for (String name : fingerprints.keySet()) {
            if (!newProperties.containsKey(name)) {
                toRemove.add(name);
            }
        }
        if (!toRemove.isEmpty() && (double) toRemove.size() / fingerprints.size() > removalThreshold) {
            log.error("dynamic-datasource refuse to remove [{}] of [{}] managed datasource, "
                            + "exceed threshold {},please check your config center",
                    toRemove.size(), fingerprints.size(), removalThreshold);
            result.setRemovalRejected(true);
        }

        for (Map.Entry<String, DataSourceProperty> entry : newProperties.entrySet()) {
            String name = entry.getKey();
            DataSourceProperty property = entry.getValue();
            if (property == null) {
                result.fail(name, "datasource property is null");
                continue;
            }
            String newFingerprint = fingerprint(name, property);
            if (newFingerprint.equals(fingerprints.get(name))) {
                result.getSkipped().add(name);
                continue;
            }
            boolean isNew = !fingerprints.containsKey(name);
            try {
                // 与 AbstractDataSourceProvider 保持一致的名称兜底
                if (!DsStrUtils.hasText(property.getPoolName())) {
                    property.setPoolName(name);
                }
                DataSource newDataSource = dataSourceCreator.createDataSource(property);
                // 同名覆盖：旧池按 graceDestroy 策略关闭
                routingDataSource.addDataSource(name, newDataSource);
                fingerprints.put(name, newFingerprint);
                if (isNew) {
                    result.getAdded().add(name);
                } else {
                    result.getUpdated().add(name);
                }
            } catch (Exception e) {
                // 创建失败保持旧池可用，不把整个库变成不可用状态
                result.fail(name, e.toString());
                log.error("dynamic-datasource refresh the datasource named [{}] failed, keep the old one", name, e);
            }
        }

        if (!result.isRemovalRejected()) {
            String primary = routingDataSource.getPrimary();
            for (String name : toRemove) {
                if (name.equals(primary)) {
                    result.fail(name, "could not remove primary datasource");
                    log.warn("dynamic-datasource skip removing the primary datasource named [{}]", name);
                    continue;
                }
                try {
                    routingDataSource.removeDataSource(name);
                    fingerprints.remove(name);
                    result.getRemoved().add(name);
                } catch (Exception e) {
                    result.fail(name, e.toString());
                    log.error("dynamic-datasource remove the datasource named [{}] failed", name, e);
                }
            }
        }

        if (result.isChanged()) {
            log.info("dynamic-datasource refresh finished: added={}, updated={}, removed={}, skipped={}, failed={}",
                    result.getAdded(), result.getUpdated(), result.getRemoved(), result.getSkipped(),
                    result.getFailures().keySet());
        } else {
            log.debug("dynamic-datasource refresh finished without any change, skipped={}", result.getSkipped().size());
        }
        return result;
    }

    /**
     * 当前托管的数据源名称（不含运行期手工添加的）
     *
     * @return 快照中的名称
     */
    public Set<String> getManagedDataSourceNames() {
        return new LinkedHashSet<>(fingerprints.keySet());
    }

    /**
     * 计算配置指纹。
     * <p>
     * 启动时框架会对 property 做若干兜底改写（poolName、lazy、publicKey），刷新时拿到的对象没有这些改写，
     * 因此这几个字段统一归一化后再参与比对，否则每次刷新都会被判成「变了」而全量重建。
     */
    private String fingerprint(String dsName, DataSourceProperty property) {
        String poolName = DsStrUtils.hasText(property.getPoolName()) ? property.getPoolName() : dsName;
        Boolean lazy = property.getLazy() == null ? properties.getLazy() : property.getLazy();
        String publicKey = DsStrUtils.hasText(property.getPublicKey())
                ? property.getPublicKey() : properties.getPublicKey();
        return DsPropertyFingerprint.of(property, "poolName", "lazy", "publicKey")
                + "|poolName=" + poolName
                + "|lazy=" + lazy
                + "|publicKey=" + publicKey;
    }

    /**
     * 单次刷新结果
     */
    @Data
    public static class Result {

        /**
         * 新增的数据源
         */
        private final Set<String> added = new LinkedHashSet<>();
        /**
         * 配置变更、连接池已重建的数据源
         */
        private final Set<String> updated = new LinkedHashSet<>();
        /**
         * 已移除的数据源
         */
        private final Set<String> removed = new LinkedHashSet<>();
        /**
         * 配置无变化、保持原连接池的数据源
         */
        private final Set<String> skipped = new LinkedHashSet<>();
        /**
         * 处理失败的数据源 -&gt; 失败原因
         */
        private final Map<String, String> failures = new LinkedHashMap<>();
        /**
         * 整体拒绝时的原因，非 null 表示本次刷新未执行
         */
        private String rejectReason;
        /**
         * 删除是否因超出熔断阈值而被跳过
         */
        private boolean removalRejected;

        public boolean isRejected() {
            return rejectReason != null;
        }

        public boolean isChanged() {
            return !added.isEmpty() || !updated.isEmpty() || !removed.isEmpty();
        }

        public int changedCount() {
            return added.size() + updated.size() + removed.size();
        }

        void fail(String name, String reason) {
            failures.put(name, reason);
        }
    }
}
