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

package org.apache.activemq.artemis.core.security;

import com.google.common.cache.Cache;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.ToDoubleFunction;


public abstract class SecurityMetrics<T extends SecurityMetrics<T>> extends CacheMeterBinder<Cache> {

        protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        Map<String, ToDoubleFunction> METER_NAMES_TO_METER_FUNCTIONS_MAP = new ConcurrentHashMap<>(
                Map.of(
                        "cache.size", (c) -> {
                            Long size = this.size();
                            return size == null ? 0.0 : (double) size;
                        },
                        "cache.puts", (c) -> (double) this.putCount(),
                        "cache.evictions", (c) -> {
                            Long evictions = this.evictionCount();
                            return evictions == null ? 0.0 : (double) evictions;
                        }
                )
        );
        protected static final String CACHE_TYPE_TAG_NAME = "type";
        protected static final String NON_UNIQUE_METER_NAME = "cache.gets";
        protected static final String RESULT_TAG_NAME = "result";
        protected abstract AtomicLongFieldUpdater<T> getSuccessCountUpdater();
        protected abstract AtomicLongFieldUpdater<T> getFailureCountUpdater();
        protected abstract AtomicLongFieldUpdater<T> getCacheMissCountUpdater();
        protected abstract AtomicLongFieldUpdater<T> getCacheHitCountUpdater();
        protected abstract AtomicLongFieldUpdater<T> getCachePutCountUpdater();
        protected abstract String getMetricNamePrefix();
        protected abstract String getCacheType();
        protected abstract String getFailureDescription();
        protected abstract String getSuccessDescription();
        protected abstract Logger getLogger();
    public volatile long cacheHitCount;
    public volatile long cacheMissCount;
    public volatile long failureCount;
    public volatile long successCount;
    public volatile long cachePutCount;

        protected SecurityMetrics(Cache cache, String cacheName, Iterable<Tag> tags) {
            super(cache, cacheName, tags);
        }
        public void incrementCacheCount(boolean cacheHit) {
            if (cacheHit) {
                long cacheHitCount = getCacheHitCountUpdater().incrementAndGet((T) this);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("{} increment {} cacheHitCount to: {}", this, getCacheType(), cacheHitCount);
                }
            } else {
                long cacheMissCount = getCacheMissCountUpdater().incrementAndGet((T) this);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("{} increment {} cacheMissCount to: {}", this, getCacheType(), cacheMissCount);
                }
            }
        }

        public void incrementCount(boolean success) {
            if (success) {
                long successCount = getSuccessCountUpdater().incrementAndGet((T) this);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("{} increment {} successCount to: {}", this, getCacheType(), successCount);
                }
            } else {
                long failureCount = getFailureCountUpdater().incrementAndGet((T) this);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("{} increment {} failureCount to: {}", this, getCacheType(), failureCount);
                }
            }
        }
        public void incrementCachePutCount() {
            long cachePutCount = getCachePutCountUpdater().incrementAndGet((T) this);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("{} increment {} cachePutCount to: {}", this, getCacheType(), cachePutCount);
            }
        }

        @Override
        protected Long size() {
            Cache c = getCache();
            if (c != null) {
                return getCache().size();
            }
            return null;
        }

        abstract protected long failureCount();

        abstract protected long successCount();

//    @Override
//    protected abstract long hitCount();
//
//    @Override
//    protected Long missCount() {
//        return cacheMissCount;
//    }

        @Override
        protected Long evictionCount() {
            return null;
        }

        //    @Override
//    protected long putCount() {
//        return cachePutCount;
//    }
        @Override
        protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
            FunctionCounter.builder(getMetricNamePrefix() + "failure.count", getFailureCountUpdater(), c -> this.failureCount())
                    .tags("result", "failure")
                    .description(getFailureDescription())
                    .register(registry);

            FunctionCounter.builder( getMetricNamePrefix() + "success.count", getSuccessCountUpdater(), c -> this.successCount())
                    .tags("result", "success")
                    .description(getSuccessDescription())
                    .register(registry);

            prependMetricNameWithArtemisPrefix(registry);
        }

        private void prependMetricNameWithArtemisPrefix(MeterRegistry registry) {
            getLogger().debug("Updating " + getCacheType() + " metric names with artemis prefix");
            List<Meter.Id> metersToRemove = new ArrayList<>();

            registry.getMeters().forEach(meter -> {
                String meterName = meter.getId().getName();
                Meter.Id meterID = meter.getId();

                for (String cacheMeterBinderMeterName : METER_NAMES_TO_METER_FUNCTIONS_MAP.keySet()) {
                    if (meterName.equals(cacheMeterBinderMeterName) && meterID.getTag(CACHE_TYPE_TAG_NAME).equals(getCacheType())) {

                        metersToRemove.add(meterID);

                        //register the meter with the prefix prepended
                        FunctionCounter.builder(getMetricNamePrefix() + meterName, meterID.getDescription(), getMeterFunction(meter))
                                .tags(meterID.getTags())
                                .description(meterID.getDescription())
                                .register(registry);
                    }
                }
            });

            metersToRemove.stream().forEach(registry::remove);
        }

        private ToDoubleFunction<String> getMeterFunction(Meter meter) {

            // There are two cache meters with same name (cache.gets) but they have different tags (`miss` and `hit`)
            if (meter.getId().getName().equals(NON_UNIQUE_METER_NAME)) {
                if (meter.getId().getTag(RESULT_TAG_NAME).equals("miss")) {
                    return (c) -> {
                        Long misses = this.missCount();
                        return misses == null ? 0.0 : (double) misses;
                    };
                } else {
                    return (c) -> (double) this.hitCount();
                }
            }

            return METER_NAMES_TO_METER_FUNCTIONS_MAP.get(meter.getId().getName());
        }
    }
}
