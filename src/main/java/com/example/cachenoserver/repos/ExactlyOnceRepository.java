package com.example.cachenoserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cachenoserver.entities.ExactlyOnce;

public interface ExactlyOnceRepository extends JpaRepository<ExactlyOnce, String> {

}
