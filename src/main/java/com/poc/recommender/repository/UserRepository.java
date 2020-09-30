package com.poc.recommender.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.poc.recommender.entities.User;

public interface UserRepository  extends PagingAndSortingRepository<User, Long> {
	
    public User findByAppUserId(Long appUserId);
}
