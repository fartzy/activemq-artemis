/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.artemis.core.security.impl;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
import com.google.common.cache.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class SecurityStoreMetrics {

    private static final String CACHE_TYPE_TAG_NAME = "type";
    private static final String DOT = ".";
    private static final String ARTEMIS = ".";
    private static final String AUTHENTICATION = "authentication";
    private static final String AUTHORIZATION = "authorization";
    private static final AtomicLongFieldUpdater<SecurityStoreMetrics> AUTHORIZATION_SUCCESS_COUNT =
            AtomicLongFieldUpdater.newUpdater(SecurityStoreMetrics.class, "authorizationSuccessCount");
    private static final AtomicLongFieldUpdater<SecurityStoreMetrics> AUTHORIZATION_FAILURE_COUNT =
            AtomicLongFieldUpdater.newUpdater(SecurityStoreMetrics.class, "authorizationFailureCount");
    private volatile long authorizationFailureCount;
    private volatile long authorizationSuccessCount;
    private GuavaCacheMetrics authenticationCacheMetrics;
    private GuavaCacheMetrics authorizationCacheMetrics;

    private SecurityStoreMetrics(Builder builder) {
        this.authenticationCacheMetrics = builder.authenticationCacheMetrics;
        this.authorizationCacheMetrics = builder.authorizationCacheMetrics;
    }

    public static class Builder {
        private GuavaCacheMetrics authenticationCacheMetrics;
        private GuavaCacheMetrics authorizationCacheMetrics;

        public Builder authenticationCache(Cache authenticationCache) {
            this.authenticationCacheMetrics = new GuavaCacheMetrics(
                    authenticationCache,
                    AUTHENTICATION,
                    new ArrayList<>(List.of(new ImmutableTag(CACHE_TYPE_TAG_NAME, AUTHENTICATION)))
            );
            return this;
        }

        public Builder authorizationCache(Cache authorizationCache) {
            this.authorizationCacheMetrics = new GuavaCacheMetrics(
                    authorizationCache,
                    AUTHORIZATION,
                    new ArrayList<>(List.of(new ImmutableTag(CACHE_TYPE_TAG_NAME, AUTHORIZATION)))
            );
            return this;
        }

        public SecurityStoreMetrics build() {
            return new SecurityStoreMetrics(this);
        }
    }
}
