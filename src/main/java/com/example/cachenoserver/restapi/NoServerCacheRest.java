package com.example.cachenoserver.restapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.cachenoserver.service.IdempotentAPICaller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "NoServerCacheRest", description = "the No Server Cache Api")
@RestController
public class NoServerCacheRest {

	private static final Logger log = LoggerFactory.getLogger(NoServerCacheRest.class);

	@Autowired
	private Collection<EmbeddedCacheManager> cacheManagers; // Autowire all CacheManager instances

	@Autowired
	@Qualifier("getEvictCache")
	private org.infinispan.Cache<String, String> cacheToEvict;

	@Autowired
	private IdempotentAPICaller idempotentAPICaller;

	@GetMapping("/all-caches")
	@Operation(summary = "Fetch all caches available", description = " fetches all caches and their data can be run at two different nodes in a way to verify that they are aligned")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successful operation") })
	public ResponseEntity<String> getAllCaches() throws Exception {

		final Map<String, Map<Object, Object>> allCaches = new HashMap<>();
		for (final EmbeddedCacheManager cacheManager : cacheManagers) {
			for (final String cacheName : cacheManager.getCacheNames()) {
				allCaches.put(cacheName, new HashMap<>(cacheManager.getCache(cacheName)));
			}
		}

		return ResponseEntity.ok(allCaches.toString());

	}

	@GetMapping("/hitCached")
	@Operation(summary = "This API is @Cacheable", description = " it can be run at two different nodes in a way to verify that they are aligned, inside the Service the @Cacheable is used to cache database data")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successful operation") })
	public ResponseEntity<String> hitCached() {
		log.info("************Calling hitCached************");
		return ResponseEntity.ok(idempotentAPICaller.loadExactlyOnce("1").toString());
	}

	@GetMapping("/infinispanget/{key}")
	@Operation(summary = "get from cache", description = " gets value from cach with a specified key, it can be run at two different nodes in a way to verify that they are aligned")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successful operation") })
	public ResponseEntity<String> hitget(@PathVariable String key) {
		log.info("************Calling infinispanget************" + key);
		final String value = cacheToEvict.get(key);
		final String response = value == null ? "null" : cacheToEvict.get(key).toString();
		log.info("************Got ************" + response);
		return ResponseEntity.ok(response);

	}

	@Operation(summary = "Insert in cache", description = " inserts value in cache with a specified key, it can be run in a node and call get in another node, to verify that they are aligned")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "successful operation") })
	@GetMapping("/infinispanhit/{key}")
	public ResponseEntity<String> hitinf(@PathVariable String key) throws URISyntaxException {
		final String val = "" + new Date();

		cacheToEvict.put(key, val);
		log.info("************Into Cache {}************", val);

		return ResponseEntity.created(new URI("/infinispanhit"))
				.body("Element created with key=" + key + "with value=" + val);
	}

}
