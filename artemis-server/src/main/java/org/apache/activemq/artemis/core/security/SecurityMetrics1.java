//package org.apache.activemq.artemis.core.security;
//
//import com.google.common.cache.Cache;
//import io.micrometer.core.instrument.*;
//import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
//import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.lang.invoke.MethodHandles;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLongFieldUpdater;
//import java.util.function.ToDoubleFunction;
//
//public abstract class SecurityMetrics2<T extends SecurityMetrics2<T>> extends CacheMeterBinder<Cache> {
//
//    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
//    protected static final String CACHE_TYPE_TAG_NAME = "type";
//    protected static final String NON_UNIQUE_METER_NAME = "cache.gets";
//    protected static final String RESULT_TAG_NAME = "result";
//    protected static final String[] METER_NAMES_TO_RENAME_ARRAY = new String[] {"cache.size", "cache.puts", "cache.evictions"};
//
//        Map<String, ToDoubleFunction> METER_NAMES_TO_METER_FUNCTIONS_MAP = new ConcurrentHashMap<>(
//            Map.of(
//                    "cache.size", (c) -> {
//                        Long size = this.size();
//                        return size == null ? 0.0 : (double) size;
//                    },
//                    "cache.puts", (c) -> (double) this.putCount(),
//                    "cache.evictions", (c) -> {
//                        Long evictions = this.evictionCount();
//                        return evictions == null ? 0.0 : (double) evictions;
//                    }
//            )
//    );
//    protected abstract AtomicLongFieldUpdater<T> getSuccessCountUpdater();
//    protected abstract AtomicLongFieldUpdater<T> getFailureCountUpdater();
//    protected abstract String getMetricNamePrefix();
//    protected abstract String getCacheType();
//    protected abstract String getFailureDescription();
//    protected abstract String getSuccessDescription();
//    protected abstract Logger getLogger();
//    private GuavaCacheMetrics cacheMetrics;
//    protected SecurityMetrics(Cache cache, String cacheName, Iterable<Tag> tags) {
//        super(cache, cacheName, tags);
//    }
//
//    public void incrementCount(boolean success) {
//        if (success) {
//            long successCount = getSuccessCountUpdater().incrementAndGet((T) this);
//            if (getLogger().isDebugEnabled()) {
//                getLogger().debug("{} increment {} successCount to: {}", this, getCacheType(), successCount);
//            }
//        } else {
//            long failureCount = getFailureCountUpdater().incrementAndGet((T) this);
//            if (getLogger().isDebugEnabled()) {
//                getLogger().debug("{} increment {} failureCount to: {}", this, getCacheType(), failureCount);
//            }
//        }
//    }
//
//    @Override
//    protected Long size() {
//        Cache c = getCache();
//        if (c != null) {
//            return getCache().size();
//        }
//        return null;
//    }
//
//    abstract protected long failureCount();
//
//    abstract protected long successCount();
//
//    @Override
//    protected Long evictionCount() {
//        return null;
//    }
//
////    @Override
////    protected long putCount() {
////        return cachePutCount;
////    }
//    @Override
//    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
//        FunctionCounter.builder(getMetricNamePrefix() + "failure.count", getFailureCountUpdater(), c -> this.failureCount())
//                .tags("result", "failure")
//                .description(getFailureDescription())
//                .register(registry);
//
//        FunctionCounter.builder( getMetricNamePrefix() + "success.count", getSuccessCountUpdater(), c -> this.successCount())
//                .tags("result", "success")
//                .description(getSuccessDescription())
//                .register(registry);
//
//        prependMetricNameWithArtemisPrefix(registry);
//    }
//
//    private void prependMetricNameWithArtemisPrefix(MeterRegistry registry) {
//        getLogger().debug("Updating " + getCacheType() + " metric names with artemis prefix");
//        List<Meter.Id> metersToRename = new ArrayList<>();
//
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
//
//    private ToDoubleFunction<String> getMeterFunction(Meter meter) {
//
//        // There are two cache meters with same name (cache.gets) but they have different tags (`miss` and `hit`)
//        if (meter.getId().getName().equals(NON_UNIQUE_METER_NAME)) {
//            if (meter.getId().getTag(RESULT_TAG_NAME).equals("miss")) {
//                return (c) -> {
//                    Long misses = this.missCount();
//                    return misses == null ? 0.0 : (double) misses;
//                };
//            } else {
//                return (c) -> (double) this.hitCount();
//            }
//        }
//
//        return METER_NAMES_TO_METER_FUNCTIONS_MAP.get(meter.getId().getName());
//    }
//}