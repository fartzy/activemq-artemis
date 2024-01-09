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
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;

import io.micrometer.core.instrument.MeterRegistry;

import io.micrometer.core.instrument.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

//public class ExtendedGuavaCacheMetrics<K, V, C extends Cache<K, V>> extends GuavaCacheMetrics<K, V, C> {
//    public static class MetricInfo<K, V, C extends Cache<K, V>>  {
//        String metricName;
//        String description;
//        Map<String, ToDoubleFunction<C>> metricMethodsByTag;
//
//        public MetricInfo(String metricName, String description, Map<String, ToDoubleFunction<C>> metricMethodsByTag) {
//            this.metricName = metricName;
//            this.description = description;
//            this.metricMethodsByTag = metricMethodsByTag;
//        }
//
//        public Map<String, ToDoubleFunction<C>> getMetricMethodsByTag() {
//            return metricMethodsByTag;
//        }
//        public String getDescription() {
//            return description;
//        }
//        public ToDoubleFunction<C> getSingleMeterFunction() {
//            return metricMethodsByTag.entrySet().stream().findFirst().get().getValue();
//        }
//    }
//    public String metricNamePrefix;
//    public final static String RESULT_TAG_KEY = "result";
//    public final static String NON_UNIQUE_METER_NAME = "cache.gets";
//    public ExtendedGuavaCacheMetrics(C cache, String cacheName, Iterable<Tag> tags, String metricNamePrefix) {
//        super(cache, cacheName, tags);
//        this.metricNamePrefix = metricNamePrefix;
//    }
//    public final Map<String, MetricInfo> METRIC_NAME_MAP = new HashMap<>(); {
//        METRIC_NAME_MAP.put("cache.size", new MetricInfo("artemis.authentication.cache.size",
//                "The number of entries in this cache",
//                Collections.singletonMap(null, cache -> getValueAsDouble(this::size))));
//
//        METRIC_NAME_MAP.put(NON_UNIQUE_METER_NAME, new MetricInfo("artemis.authentication.cache.gets",
//                "The number of cache gets",
//                Map.of("hit", cache -> getValueAsDouble(this::hitCount),
//                        "miss", cache -> getValueAsDouble(this::missCount))));
//    }
//    @Override
//    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
//        super.bindImplementationSpecificMetrics(registry);
//
//        // This method can be used to override the implementation-specific metrics
//        // or to add new metrics if necessary.
//    }
//
//    private void renameAndRegisterMetricsWithArtemisPrefix(MeterRegistry registry) {
//        C cache = getCache();
//        for (Map.Entry<String, MetricInfo> metricNameEntry : METRIC_NAME_MAP.entrySet()) {
//            String oldName = metricNameEntry.getKey();
//            MetricInfo metricInfo = metricNameEntry.getValue();
//
//            List<Meter> oldMeters = (List<Meter>) registry.find(oldName).meters();
//            for (Meter oldMeter : oldMeters) {
//
//                if (oldMeter instanceof FunctionCounter) {
//                    FunctionCounter oldCounter = (FunctionCounter) oldMeter;
//                    FunctionCounter.builder(metricNamePrefix + metricInfo.metricName, cache, getMeterFunction(oldMeter))
//                            .tags(oldMeter.getId().getTags())
//                            .description(metricInfo.description)
//                            .register(registry);
//                } else if (oldMeter instanceof Gauge) {
//                    Gauge oldGauge = (Gauge) oldMeter;
//                    Gauge.builder(metricNamePrefix + metricInfo.metricName, cache, c -> metricMethod.apply(c))
//                            .tags(tags)
//                            .description(metricInfo.getDescription())
//                            .register(registry);
//                }
//            }
//        }
//    }
//
//    private ToDoubleFunction<C> getMeterFunction(Meter meter) {
//        // There are two cache meters with same name (cache.gets) but they have different tags (`miss` and `hit`)
//        if (meter.getId().getName().equals(NON_UNIQUE_METER_NAME)) {
//            return (ToDoubleFunction<C>) METRIC_NAME_MAP.get(NON_UNIQUE_METER_NAME).getMetricMethodsByTag().get(meter.getId().getTag(RESULT_TAG_KEY));
//        }
//
//        return METRIC_NAME_MAP.get(meter.getId().getName()).getSingleMeterFunction();
//    }
//
//    private double getValueAsDouble(Supplier<Long> valueSupplier) {
//        Long value = valueSupplier.get();
//        return value == null ? 0.0 : (double)value;
//    }
//}
