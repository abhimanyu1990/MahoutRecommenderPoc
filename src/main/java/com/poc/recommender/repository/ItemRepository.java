package com.poc.recommender.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.poc.recommender.entities.Item;



public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
    public Item findByAppItemId(Long appUserId);
}
