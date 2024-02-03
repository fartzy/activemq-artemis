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

import com.google.common.cache.Cache;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
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

public class AuthorizationMetrics extends CacheMeterBinder<Cache> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String METRIC_NAME_PREFIX = "artemis.authorization.";
    private static final String CACHE_TYPE = "authorization";
    private static final String CACHE_TYPE_TAG_NAME = "type";
    private static final String NON_UNIQUE_METER_NAME = "cache.gets";
    private static final String RESULT_TAG_NAME = "result";


    private static final String DESCRIPTION_AUTHORIZATION_FAILURE = "The number of times authorizing to the broker has failed.";

    private static final String DESCRIPTION_AUTHORIZATION_SUCCESS = "The number of times authorizing to the broker has been successful.";
    Map<String, ToDoubleFunction> METER_NAMES_TO_METER_FUNCTIONS_MAP = new ConcurrentHashMap<>(
            Map.of(
                    "cache.size",  (c) -> {
                        Long size = this.size();
                        return size == null ? 0.0 : (double) size;
                    },
                    "cache.puts", (c) -> (double) this.putCount(),
                    "cache.evictions",  (c) -> {
                        Long evictions = this.evictionCount();
                        return evictions == null ? 0.0 : (double) evictions;
                    }
            )
    );

    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_SUCCESS_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "authorizationSuccessCount");
    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_FAILURE_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "authorizationFailureCount");
    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_CACHE_MISS_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "authorizationCacheMissCount");
    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_CACHE_HIT_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "authorizationCacheHitCount");
    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_CACHE_PUT_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "authorizationCachePutCount");

    private volatile long authorizationCacheHitCount;

    private volatile long authorizationCacheMissCount;

    private volatile long authorizationFailureCount;

    private volatile long authorizationSuccessCount;

    private volatile long authorizationCachePutCount;

    public AuthorizationMetrics(Cache cache) {
        super(cache, "authorization", new ArrayList<>(List.of(new ImmutableTag("type", "authorization"))));
    }

    public void incrementAuthorizationCacheCount(boolean cacheHit) {
        if (cacheHit) {
            AUTHORIZATION_CACHE_HIT_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authorizationCacheHitCount to {}: {}", this, authorizationCacheHitCount);
            }
        } else {
            AUTHORIZATION_CACHE_MISS_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authorizationCacheMissCount to {}: {}", this, authorizationCacheMissCount);
            }
        }
    }

    public void incrementAuthorizationCount(boolean success) {
        if (success) {
            AUTHORIZATION_SUCCESS_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authorizationSuccessCount to {}: {}", this, authorizationSuccessCount);
            }
        } else {
            AUTHORIZATION_FAILURE_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authorizationFailureCount to {}: {}", this, authorizationFailureCount);
            }
        }
    }
    public void incrementAuthorizationCachePutCount() {
        AUTHORIZATION_CACHE_PUT_COUNT.incrementAndGet(this);
        if (logger.isDebugEnabled()) {
            logger.debug("{} increment authorizationCachePutCount to {}: {}", this, authorizationCachePutCount);
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

    protected long failureCount() { return authorizationFailureCount; }
    protected long successCount() { return authorizationSuccessCount; }

    @Override
    protected long hitCount() { return authorizationCacheHitCount; }

    @Override
    protected Long missCount() { return authorizationCacheMissCount; }

    @Override
    protected Long evictionCount() { return null; }

    @Override
    protected long putCount() { return authorizationCachePutCount; }
    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        FunctionCounter.builder(METRIC_NAME_PREFIX + "failure.count", AUTHORIZATION_FAILURE_COUNT, c -> this.failureCount())
                .tags("result", "failure")
                .description(DESCRIPTION_AUTHORIZATION_FAILURE)
                .register(registry);

        FunctionCounter.builder(METRIC_NAME_PREFIX + "success.count", AUTHORIZATION_SUCCESS_COUNT, c -> this.successCount())
                .tags("result", "success")
                .description(DESCRIPTION_AUTHORIZATION_SUCCESS)
                .register(registry);

        prependMetricNameWithArtemisPrefix(registry);
    }
    private void prependMetricNameWithArtemisPrefix(MeterRegistry registry) {

        List<Meter.Id> metersToRemove = new ArrayList<>();

        registry.getMeters().forEach(meter -> {
            String meterName = meter.getId().getName();
            Meter.Id meterID = meter.getId();

            for (String cacheMeterBinderMeterName : METER_NAMES_TO_METER_FUNCTIONS_MAP.keySet()) {
                if (meterName.equals(cacheMeterBinderMeterName) && meterID.getTag(CACHE_TYPE_TAG_NAME).equals(CACHE_TYPE)) {

                    metersToRemove.add(meterID);

                    //register the meter with the prefix prepended
                    FunctionCounter.builder(METRIC_NAME_PREFIX + meterName, meterID.getDescription(), getMeterFunction(meter))
                            .tags(meterID.getTags())
                            .description(meterID.getDescription())
                            .register(registry);
                }
            }
        });

        metersToRemove.stream().forEach(registry::remove);
    }

    private ToDoubleFunction<String> getMeterFunction(Meter meter) {

        // There are two cache meters with same name but they have different tags
        if (meter.getId().getName().equals(NON_UNIQUE_METER_NAME)) {
            if (meter.getId().getTag(RESULT_TAG_NAME).equals("miss")){
                return (c) -> {
                    Long misses = this.missCount();
                    return misses == null ? 0.0 : (double) misses;
                };
            } else {
                return (c) -> (double)this.hitCount();
            }
        }

        return METER_NAMES_TO_METER_FUNCTIONS_MAP.get(meter.getId().getName());
    }
}