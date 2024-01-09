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
import io.micrometer.core.instrument.MeterRegistry;

import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class AuthenticationMetrics extends CacheMeterBinder<Cache> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String METRIC_NAME_PREFIX = "artemis.authentication.";
    private static final String CACHE_TYPE = "authentication";
    private static final String CACHE_TYPE_TAG_NAME = "type";
    private static final String NON_UNIQUE_METER_NAME = "cache.gets";
    private static final String RESULT_TAG_NAME = "result";


    private static final String DESCRIPTION_AUTHENTICATION_FAILURE = "The number of times authenticating to the broker has failed.";

    private static final String DESCRIPTION_AUTHENTICATION_SUCCESS = "The number of times authenticating to the broker has been successful.";
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

    private static final AtomicLongFieldUpdater<AuthenticationMetrics> AUTHENTICATION_SUCCESS_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthenticationMetrics.class, "authenticationSuccessCount");
    private static final AtomicLongFieldUpdater<AuthenticationMetrics> AUTHENTICATION_FAILURE_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthenticationMetrics.class, "authenticationFailureCount");
    private static final AtomicLongFieldUpdater<AuthenticationMetrics> AUTHENTICATION_CACHE_MISS_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthenticationMetrics.class, "authenticationCacheMissCount");
    private static final AtomicLongFieldUpdater<AuthenticationMetrics> AUTHENTICATION_CACHE_HIT_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthenticationMetrics.class, "authenticationCacheHitCount");
    private static final AtomicLongFieldUpdater<AuthenticationMetrics> AUTHENTICATION_CACHE_PUT_COUNT =
            AtomicLongFieldUpdater.newUpdater(AuthenticationMetrics.class, "authenticationCachePutCount");

    private volatile long authenticationCacheHitCount;

    private volatile long authenticationCacheMissCount;

    private volatile long authenticationFailureCount;

    private volatile long authenticationSuccessCount;

    private volatile long authenticationCachePutCount;

    public AuthenticationMetrics(Cache cache) {
        super(cache, "authentication", new ArrayList<>(List.of(new ImmutableTag("type", "authentication"))));
    }

    public void incrementAuthenticationCacheCount(boolean cacheHit) {
        if (cacheHit) {
            AUTHENTICATION_CACHE_HIT_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authenticationCacheHitCount to {}: {}", this, authenticationCacheHitCount);
            }
        } else {
            AUTHENTICATION_CACHE_MISS_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authenticationCacheMissCount to {}: {}", this, authenticationCacheMissCount);
            }
        }
    }

    public void incrementAuthenticationCount(boolean success) {
        if (success) {
            AUTHENTICATION_SUCCESS_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authenticationSuccessCount to {}: {}", this, authenticationSuccessCount);
            }
        } else {
            AUTHENTICATION_FAILURE_COUNT.incrementAndGet(this);
            if (logger.isDebugEnabled()) {
                logger.debug("{} increment authenticationFailureCount to {}: {}", this, authenticationFailureCount);
            }
        }
    }
    public void incrementAuthenticationCachePutCount() {
        AUTHENTICATION_CACHE_PUT_COUNT.incrementAndGet(this);
        if (logger.isDebugEnabled()) {
            logger.debug("{} increment authenticationCachePutCount to {}: {}", this, authenticationCachePutCount);
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

    protected long failureCount() { return authenticationFailureCount; }
    protected long successCount() { return authenticationSuccessCount; }

    @Override
    protected long hitCount() { return authenticationCacheHitCount; }

    @Override
    protected Long missCount() { return authenticationCacheMissCount; }

    @Override
    protected Long evictionCount() { return null; }

    @Override
    protected long putCount() { return authenticationCachePutCount; }
    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        FunctionCounter.builder(METRIC_NAME_PREFIX + "failure.count", AUTHENTICATION_FAILURE_COUNT, c -> this.failureCount())
                .tags("result", "failure")
                .description(DESCRIPTION_AUTHENTICATION_FAILURE)
                .register(registry);

        FunctionCounter.builder(METRIC_NAME_PREFIX + "success.count", AUTHENTICATION_SUCCESS_COUNT, c -> this.successCount())
                .tags("result", "success")
                .description(DESCRIPTION_AUTHENTICATION_SUCCESS)
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