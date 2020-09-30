package com.poc.recommender.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import com.poc.recommender.entities.UserItemView;

public interface UserItemViewRepository extends PagingAndSortingRepository<UserItemView, Long>  {

}
