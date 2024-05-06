package com.example.cachenoserver.service;

import com.example.cachenoserver.dtos.ExactlyOnceDto;

public interface IdempotentAPICaller {

	ExactlyOnceDto loadExactlyOnce(String id);

}
