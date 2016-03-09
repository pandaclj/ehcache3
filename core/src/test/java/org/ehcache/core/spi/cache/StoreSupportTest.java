/*
 * Copyright Terracotta, Inc.
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

package org.ehcache.core.spi.cache;

import org.ehcache.config.ResourceType;
import org.ehcache.core.spi.ServiceLocator;
import org.ehcache.spi.ServiceProvider;
import org.ehcache.spi.service.Service;
import org.ehcache.spi.service.ServiceConfiguration;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * Tests functionality of {@link StoreSupport} methods.
 *
 * @author Clifford W. Johnson
 */
public class StoreSupportTest {

  @Test
  public void testSelectStoreProvider() throws Exception {

    final TestBaseProvider expectedProvider = new PrimaryProvider1();
    final TestBaseProvider[] storeProviders = {
        new SecondaryProvider1(),
        new ZeroProvider(),
        expectedProvider,
        new SecondaryProvider2(),
        new PrimaryProvider2()
    };

    final ResourceType anyResourceType = new ResourceType() {
      @Override
      public boolean isPersistable() {
        return false;
      }
      @Override
      public boolean requiresSerialization() {
        return false;
      }
    };

    final ServiceLocator serviceLocator = new ServiceLocator(storeProviders);

    final Store.Provider selectedProvider = StoreSupport.selectStoreProvider(serviceLocator,
        Collections.singleton(anyResourceType),
        Collections.<ServiceConfiguration<?>>emptyList());

    assertThat(selectedProvider, is(Matchers.<Store.Provider>sameInstance(expectedProvider)));

    for (final TestBaseProvider provider : storeProviders) {
      assertThat(provider.rankAccessCount.get(), is(1));
    }
  }


  private final class ZeroProvider extends TestBaseProvider {
    public ZeroProvider() {
      super(0);
    }
  }

  private final class SecondaryProvider1 extends TestBaseProvider {
    public SecondaryProvider1() {
      super(50);
    }
  }

  private final class SecondaryProvider2 extends TestBaseProvider {
    public SecondaryProvider2() {
      super(50);
    }
  }

  private final class PrimaryProvider1 extends TestBaseProvider {
    public PrimaryProvider1() {
      super(100);
    }
  }

  private final class PrimaryProvider2 extends TestBaseProvider {
    public PrimaryProvider2() {
      super(100);
    }
  }

  private abstract class TestBaseProvider implements Store.Provider {
    final int rank;
    final AtomicInteger rankAccessCount = new AtomicInteger(0);

    public TestBaseProvider(final int rank) {
      this.rank = rank;
    }

    @Override
    public <K, V> Store<K, V> createStore(final Store.Configuration<K, V> storeConfig, final ServiceConfiguration<?>... serviceConfigs) {
      throw new UnsupportedOperationException("TestBaseProvider.createStore not implemented");
    }

    @Override
    public void releaseStore(final Store<?, ?> resource) {
      throw new UnsupportedOperationException("TestBaseProvider.releaseStore not implemented");
    }

    @Override
    public void initStore(final Store<?, ?> resource) {
      throw new UnsupportedOperationException("TestBaseProvider.initStore not implemented");
    }

    @Override
    public int rank(final Set<ResourceType> resourceTypes, final Collection<ServiceConfiguration<?>> serviceConfigs) {
      assertThat(resourceTypes, is(not(nullValue())));
      assertThat(serviceConfigs, is(not(nullValue())));
      rankAccessCount.incrementAndGet();

      return this.rank;
    }

    @Override
    public void start(final ServiceProvider<Service> serviceProvider) {
      throw new UnsupportedOperationException("TestBaseProvider.start not implemented");
    }

    @Override
    public void stop() {
      throw new UnsupportedOperationException("TestBaseProvider.stop not implemented");
    }
  }
}