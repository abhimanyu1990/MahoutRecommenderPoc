package com.poc.recommender.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poc.recommender.customexceptions.GenericBadRequestException;
import com.poc.recommender.entities.Item;
import com.poc.recommender.entities.User;
import com.poc.recommender.entities.UserItemView;
import com.poc.recommender.repository.UserItemViewRepository;

@Service
public class UserItemService {
	public static final Logger LOGGER = LoggerFactory.getLogger(UserItemService.class);
	
	@Autowired UserItemViewRepository userItemViewRepository;
	@Autowired UserService userService;
	@Autowired ItemService itemService;
	public UserItemView addViews(UserItemView userItemView) {
		LOGGER.debug("userItemView=="+userItemView);
		Long appUserId = userItemView.getUser_id();
		LOGGER.debug("App User Id =="+appUserId);
		Long appItemId  = userItemView.getItem_id();
		LOGGER.debug("App Item Id =="+appItemId);
		User user  = userService.findUserByAppUserId(appUserId);
		Item item = itemService.findItemByAppItemId(appItemId);
		if( user == null || item == null) {
			throw new GenericBadRequestException("Either user or item doesn't exist");
		}
		return userItemViewRepository.save(userItemView);	
	}
}
