package org.apache.activemq.artemis.core.security.impl;

import com.google.common.cache.Cache;
import io.micrometer.core.instrument.ImmutableTag;
import org.apache.activemq.artemis.core.security.SecurityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

//public class AuthorizationMetrics1 extends SecurityMetrics<AuthorizationMetrics1> {
//    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
//    private static final String METRIC_NAME_PREFIX = "artemis.authorization.";
//    private static final String CACHE_TYPE = "authorization";
//    private static final String DESCRIPTION_AUTHORIZATION_FAILURE = "The number of times authorization has failed.";
//    private static final String DESCRIPTION_AUTHORIZATION_SUCCESS = "The number of times authorization has been successful.";
//    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_SUCCESS_COUNT =
//            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "successCount");
//    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_FAILURE_COUNT =
//            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "failureCount");
//    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_CACHE_MISS_COUNT =
//            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "cacheMissCount");
//    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_CACHE_HIT_COUNT =
//            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "cacheHitCount");
//    private static final AtomicLongFieldUpdater<AuthorizationMetrics> AUTHORIZATION_CACHE_PUT_COUNT =
//            AtomicLongFieldUpdater.newUpdater(AuthorizationMetrics.class, "cachePutCount");
//
//    @Override
//    protected String getCacheType() {
//        return CACHE_TYPE;
//    }
//    @Override
//    protected String getMetricNamePrefix() {
//        return METRIC_NAME_PREFIX;
//    }
//    @Override
//    protected String getFailureDescription() {
//        return DESCRIPTION_AUTHORIZATION_FAILURE;
//    };
//    @Override
//    protected String getSuccessDescription() {
//        return DESCRIPTION_AUTHORIZATION_SUCCESS;
//    };
//    @Override
//    protected Logger getLogger() {
//        return LOGGER;
//    };
//
//    @Override
//    protected AtomicLongFieldUpdater<AuthorizationMetrics> getSuccessCountUpdater() {
//        return AUTHORIZATION_SUCCESS_COUNT;
//    }
//
//    @Override
//    protected AtomicLongFieldUpdater<AuthorizationMetrics> getFailureCountUpdater() {
//        return AUTHORIZATION_FAILURE_COUNT;
//    }
//
////    @Override
////    protected AtomicLongFieldUpdater<AuthorizationMetrics> getCacheMissCountUpdater() {
////        return AUTHORIZATION_CACHE_MISS_COUNT;
////    }
////
////    @Override
////    protected AtomicLongFieldUpdater<AuthorizationMetrics> getCacheHitCountUpdater() {
////        return AUTHORIZATION_CACHE_HIT_COUNT;
////    }
////
////    @Override
////    protected AtomicLongFieldUpdater<AuthorizationMetrics> getCachePutCountUpdater() {
////        return AUTHORIZATION_CACHE_PUT_COUNT;
////    }
//
//    protected volatile long cacheHitCount;
//    protected volatile long cacheMissCount;
//    protected volatile long failureCount;
//    protected volatile long successCount;
//    protected volatile long cachePutCount;
//    protected long failureCount() {
//        return failureCount;
//    }
//
//    protected long successCount() {
//        return successCount;
//    }
//
//    @Override
//    protected long hitCount() {
//        return cacheHitCount;
//    }
//
//    @Override
//    protected Long missCount() {
//        return cacheMissCount;
//    }
//
//    @Override
//    protected long putCount() {
//        return cachePutCount;
//    }
//    public AuthorizationMetrics(Cache cache) {
//        super(cache, CACHE_TYPE, new ArrayList<>(List.of(new ImmutableTag(CACHE_TYPE_TAG_NAME, CACHE_TYPE))));
//    }
//
//}