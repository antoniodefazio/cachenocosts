package com.example.cachenoserver.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.cachenoserver.dtos.ExactlyOnceDto;
import com.example.cachenoserver.entities.ExactlyOnce;
import com.example.cachenoserver.repos.ExactlyOnceRepository;

@Service
public class IdempotentAPICallerImpl implements IdempotentAPICaller {

	private static final Logger logger = LoggerFactory.getLogger(IdempotentAPICallerImpl.class);

	private final ExactlyOnceRepository exactlyOnceRepository;

	public IdempotentAPICallerImpl(ExactlyOnceRepository exactlyOnceRepository) {

		this.exactlyOnceRepository = exactlyOnceRepository;
		final ExactlyOnce exactlyOnce = new ExactlyOnce();
		exactlyOnce.setId("1");
		final long nowMillis = System.currentTimeMillis();
		final java.sql.Date now = new java.sql.Date(nowMillis);
		exactlyOnce.setCreatedOn(now);
		exactlyOnceRepository.save(exactlyOnce);
	}

	@Override
	@Cacheable(value = "nomeCache")
	public ExactlyOnceDto loadExactlyOnce(String id) {
		logger.info("Reading from repo");
		final ExactlyOnce exactlyOnce = exactlyOnceRepository.findById(id).get();
		final ExactlyOnceDto exactlyOnceDto = new ExactlyOnceDto();
		exactlyOnceDto.setLoadedOn(new Date());
		exactlyOnceDto.setCreatedOn(exactlyOnce.getCreatedOn());
		return exactlyOnceDto;

	}

}
