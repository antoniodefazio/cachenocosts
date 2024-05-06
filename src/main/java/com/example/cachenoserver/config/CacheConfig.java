package com.example.cachenoserver.config;

import org.infinispan.manager.DefaultCacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching

public class CacheConfig {

	@Bean
	public org.infinispan.Cache<String, org.springframework.cache.interceptor.SimpleKey> cacheSimpleKey(
			@Qualifier("defaulClusteredInfinispantCacheManager") DefaultCacheManager defaultCacheManager) {
		return defaultCacheManager.getCache(); // nomeCache del file xml di configurazione
	}

	@Bean
	public org.infinispan.Cache<String, String> getEvictCache(
			@Qualifier("defaulClusteredInfinispantCacheManager") DefaultCacheManager defaultCacheManager) {
		return defaultCacheManager.getCache(); // nomeCache del file xml di configurazione
	}

	@Bean
	@Primary
	public org.infinispan.Cache<String, String> hitCache(
			@Qualifier("defaulClusteredInfinispantCacheManager") DefaultCacheManager defaultCacheManager) {
		return defaultCacheManager.getCache(); // nomeCache del file xml di configurazione
	}

}
