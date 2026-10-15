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
package com.baomidou.dynamic.datasource.unwrapper;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.enums.SeataMode;
import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sql.DataSource;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSourceUnwrapperChainTest {

    @Test
    void unrecognizedAndNullDatasourcesAreUnchanged() throws Exception {
        DataSource pool = mock(DataSource.class);
        DataSourceUnwrapperChain chain = new DataSourceUnwrapperChain(true, true);
        assertThat(chain.unwrap(pool)).isSameAs(pool);
        assertThat(chain.unwrap(null)).isNull();
    }

    @Test
    void builtInIntegrationFlagsRemainOptIn() throws Exception {
        DataSource pool = mock(DataSource.class);
        P6DataSource p6spy = new P6DataSource(pool);
        DataSourceProxy seata = mock(DataSourceProxy.class);
        when(seata.getTargetDataSource()).thenReturn(pool);
        assertThat(new DataSourceUnwrapperChain(false, false).unwrap(p6spy)).isSameAs(p6spy);
        assertThat(new DataSourceUnwrapperChain(false, false).unwrap(seata)).isSameAs(seata);
        assertThat(new DataSourceUnwrapperChain(true, false).unwrap(p6spy)).isSameAs(pool);
        assertThat(new DataSourceUnwrapperChain(false, true).unwrap(seata)).isSameAs(pool);
    }

    @Test
    void nestedProxiesRetainSingleLayerUnwrapping() throws Exception {
        DataSource pool = mock(DataSource.class);
        P6DataSource inner = new P6DataSource(pool);
        DataSourceProxy seata = mock(DataSourceProxy.class);
        when(seata.getTargetDataSource()).thenReturn(inner);
        DataSourceUnwrapperChain chain = new DataSourceUnwrapperChain(true, true);
        assertThat(chain.unwrap(new P6DataSource(inner))).isSameAs(inner);
        assertThat(chain.unwrap(new P6DataSource(seata))).isSameAs(seata);
        assertThat(chain.unwrap(seata)).isSameAs(inner);
    }

    @Test
    void serviceProvidersAreDiscoveredAndOrdered() throws Exception {
        DataSource pool = mock(DataSource.class);
        assertThat(new DataSourceUnwrapperChain(false, false).unwrap(new TestDataSourceWrapper(pool)))
                .isSameAs(pool);
        assertThat(new TestDataSourceUnwrapper().getOrder()).isLessThan(new LateDataSourceUnwrapper().getOrder());
    }

    @Test
    void noProgressProvidersDoNotLoopOrDiscardTheDatasource() throws Exception {
        TestDataSourceWrapper wrapper = new TestDataSourceWrapper(null);
        DataSourceUnwrapperChain chain = new DataSourceUnwrapperChain(false, false);
        assertThat(chain.unwrap(wrapper)).isSameAs(wrapper);
        wrapper.setTarget(wrapper);
        assertThat(chain.unwrap(wrapper)).isSameAs(wrapper);
    }

    @Test
    void unwrapFailuresPropagate() {
        TestDataSourceWrapper wrapper = new TestDataSourceWrapper(null);
        Exception failure = new Exception("cannot unwrap");
        wrapper.setFailure(failure);
        DataSourceUnwrapperChain chain = new DataSourceUnwrapperChain(false, false);
        assertThatThrownBy(() -> chain.unwrap(wrapper)).isSameAs(failure);
    }

    @Test
    void routingLifecycleLoadsServiceProvidersOnceAndReusesThem() {
        int initialCount = TestDataSourceUnwrapper.getInstanceCount();
        DynamicRoutingDataSource router = new DynamicRoutingDataSource(Collections.emptyList());
        CloseableDataSource replaced = mock(CloseableDataSource.class);
        CloseableDataSource removed = mock(CloseableDataSource.class);
        CloseableDataSource destroyed = mock(CloseableDataSource.class);
        router.addDataSource("other", new TestDataSourceWrapper(replaced));
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount);
        router.addDataSource("other", new TestDataSourceWrapper(removed));
        verify(replaced).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 1);
        router.setP6spy(false);
        router.setSeata(false);
        router.removeDataSource("other");
        verify(removed).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 1);
        router.addDataSource("other", new TestDataSourceWrapper(destroyed));
        router.destroy();
        verify(destroyed).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 1);
    }

    @Test
    void routingLifecyclePassesTheBuiltInFlagsToTheChain() {
        DynamicRoutingDataSource router = new DynamicRoutingDataSource(Collections.emptyList());
        router.setP6spy(true);
        router.setSeata(true);
        CloseableDataSource pool = mock(CloseableDataSource.class);
        DataSourceProxy seata = mock(DataSourceProxy.class);
        CloseableDataSource seataPool = mock(CloseableDataSource.class);
        when(seata.getTargetDataSource()).thenReturn(seataPool);
        router.addDataSource("p6", new P6DataSource(pool));
        router.addDataSource("seata", seata);
        router.removeDataSource("p6");
        router.removeDataSource("seata");
        verify(pool).close();
        verify(seataPool).close();
    }

    @Test
    void changingP6spyFlagAfterFirstCloseRebuildsTheCachedChain() {
        int initialCount = TestDataSourceUnwrapper.getInstanceCount();
        DynamicRoutingDataSource router = new DynamicRoutingDataSource(Collections.emptyList());
        CloseableDataSource disabled = mock(CloseableDataSource.class);
        CloseableDataSource enabled = mock(CloseableDataSource.class);
        CloseableDataSource disabledAgain = mock(CloseableDataSource.class);
        router.addDataSource("p6", new P6DataSource(disabled));
        router.removeDataSource("p6");
        verify(disabled, never()).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 1);

        router.setP6spy(true);
        router.addDataSource("p6", new P6DataSource(enabled));
        router.removeDataSource("p6");
        verify(enabled).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 2);

        router.setP6spy(false);
        router.addDataSource("p6", new P6DataSource(disabledAgain));
        router.removeDataSource("p6");
        verify(disabledAgain, never()).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 3);
    }

    @Test
    void changingSeataFlagAfterFirstCloseRebuildsTheCachedChain() {
        int initialCount = TestDataSourceUnwrapper.getInstanceCount();
        DynamicRoutingDataSource router = new DynamicRoutingDataSource(Collections.emptyList());
        CloseableDataSource disabled = mock(CloseableDataSource.class);
        CloseableDataSource enabled = mock(CloseableDataSource.class);
        CloseableDataSource disabledAgain = mock(CloseableDataSource.class);
        router.addDataSource("seata", seataProxy(disabled));
        router.removeDataSource("seata");
        verify(disabled, never()).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 1);

        router.setSeata(true);
        router.addDataSource("seata", seataProxy(enabled));
        router.removeDataSource("seata");
        verify(enabled).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 2);

        router.setSeata(false);
        router.addDataSource("seata", seataProxy(disabledAgain));
        router.removeDataSource("seata");
        verify(disabledAgain, never()).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 3);
    }

    @Test
    void itemDatasourceStillClosesItsExplicitRealPool() {
        int initialCount = TestDataSourceUnwrapper.getInstanceCount();
        DynamicRoutingDataSource router = new DynamicRoutingDataSource(Collections.emptyList());
        CloseableDataSource pool = mock(CloseableDataSource.class);
        CloseableDataSource proxy = mock(CloseableDataSource.class);
        router.addDataSource("other", new ItemDataSource("other", pool, proxy, false, false, SeataMode.AT));
        router.removeDataSource("other");
        verify(pool).close();
        verify(proxy, never()).close();
        assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount);
    }

    @Test
    void failedUnwrapDoesNotCloseItsPoolOrInterruptDestroyingOtherDatasources() {
        DynamicRoutingDataSource router = new DynamicRoutingDataSource(Collections.emptyList());
        CloseableDataSource pool = mock(CloseableDataSource.class);
        TestDataSourceWrapper broken = new TestDataSourceWrapper(pool);
        broken.setFailure(new Exception("cannot unwrap"));
        CloseableDataSource healthy = mock(CloseableDataSource.class);
        router.addDataSource("broken", broken);
        router.addDataSource("healthy", new TestDataSourceWrapper(healthy));
        router.destroy();
        verify(pool, never()).close();
        verify(healthy).close();
    }

    @Test
    void providersRemainDiscoverableWhenTheContextClassLoaderIsNull() throws Exception {
        DataSource pool = mock(DataSource.class);
        TestDataSourceWrapper wrapper = new TestDataSourceWrapper(pool);
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);

            assertThat(new DataSourceUnwrapperChain(false, false).unwrap(wrapper)).isSameAs(pool);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Test
    void providerDescriptorsVisibleOnlyThroughTheContextClassLoaderRemainDiscoverable(@TempDir Path directory)
            throws Exception {
        Path services = Files.createDirectories(directory.resolve("META-INF/services"));
        // The provider type is parent-visible; only its service descriptor belongs to the child.
        Files.write(services.resolve(DataSourceUnwrapper.class.getName()),
                Collections.singletonList(ContextClassLoaderDataSourceUnwrapper.class.getName()),
                StandardCharsets.UTF_8);
        int initialCount = TestDataSourceUnwrapper.getInstanceCount();
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader context = new URLClassLoader(new URL[]{directory.toUri().toURL()},
                DataSourceUnwrapper.class.getClassLoader())) {
            Thread.currentThread().setContextClassLoader(context);

            new DataSourceUnwrapperChain(false, false);

            // The normal provider and the child-only provider each invoke the instrumented constructor.
            assertThat(TestDataSourceUnwrapper.getInstanceCount()).isEqualTo(initialCount + 2);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Test
    void optionalIntegrationJarsAreNotRequiredToLoadOrUseTheChain() throws Exception {
        URL[] urls = ((URLClassLoader) getClass().getClassLoader()).getURLs();
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try (WithoutIntegrationsClassLoader isolated = new WithoutIntegrationsClassLoader(urls)) {
            Thread.currentThread().setContextClassLoader(isolated);
            Class<?> chainType = isolated.loadClass(DataSourceUnwrapperChain.class.getName());
            Object chain = chainType.getConstructor(boolean.class, boolean.class).newInstance(true, true);
            DataSource pool = mock(DataSource.class);
            assertThat(chainType.getMethod("unwrap", DataSource.class).invoke(chain, pool)).isSameAs(pool);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private DataSourceProxy seataProxy(DataSource pool) {
        DataSourceProxy proxy = mock(DataSourceProxy.class);
        when(proxy.getTargetDataSource()).thenReturn(pool);
        return proxy;
    }
}
