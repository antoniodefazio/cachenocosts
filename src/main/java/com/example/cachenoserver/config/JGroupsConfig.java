package com.example.cachenoserver.config;

import java.io.IOException;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration

public class JGroupsConfig {

	private static final Logger log = LoggerFactory.getLogger(JGroupsConfig.class);

	@Value("${infinispan.clustered}")
	private String infinispanClustered;

	@Bean
	@Primary // there is also springEmbeddedCacheManager which can be used for test
	public CacheManager clusteredInfinispanCacheManager(
			@Qualifier("defaulClusteredInfinispantCacheManager") org.infinispan.manager.DefaultCacheManager defaultCacheManager)
			throws IOException {

		final CacheManager cacheManager = new SpringEmbeddedCacheManager(defaultCacheManager);

		return cacheManager;
	}

	@Bean
	@Qualifier("defaulClusteredInfinispantCacheManager")
	@Primary // Another one is created in InfinispanEmbeddedAutoConfiguration.class, must be
	// replaced
	EmbeddedCacheManager defaulClusteredInfinispantCacheManager() throws Exception {
		log.info("************Infinispan Config File {}*******", infinispanClustered);
		return new org.infinispan.manager.DefaultCacheManager(infinispanClustered);

	}

}
