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

import java.util.function.ToLongFunction;

//public class ExposedGuavaCacheMetrics<K, V, C extends Cache<K, V>> extends GuavaCacheMetrics<K, V, C> {
//    // Assuming appropriate constructors here...
//
//    private long getOrDefault(ToLongFunction<Cache<?, ?>> function, long defaultValue) {
//        C ref = (Cache)this.getCache();
//        return ref != null ? function.applyAsLong(ref) : defaultValue;
//    }
//}
//        registry.getMeters().forEach(oldMeter -> {
//            String oldMeterName = oldMeter.getId().getName();
//            Meter.Id oldMeterID = oldMeter.getId();
//
//            for (String cacheMeterName : METER_NAMES_TO_RENAME_ARRAY) {
//                if (oldMeterName.equals(cacheMeterName) && oldMeterID.getTag(CACHE_TYPE_TAG_NAME).equals(getCacheType())) {
//
//                    metersToRename.add(oldMeterID);
//
//                    //register the meter with the prefix prepended
//                    FunctionCounter.builder(getMetricNamePrefix() + oldMeterName, oldMeterID.getDescription(), getMeterFunction(oldMeter))
//                            .tags(oldMeterID.getTags())
//                            .description(oldMeterID.getDescription())
//                            .register(registry);
//                }
//            }
//        });
//
//        metersToRename.stream().forEach(registry::remove);
//    }