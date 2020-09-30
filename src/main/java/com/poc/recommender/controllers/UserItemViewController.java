package com.poc.recommender.controllers;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.poc.recommender.customexceptions.GenericCustomException;
import com.poc.recommender.dto.UserItemViewDto;
import com.poc.recommender.entities.UserItemView;
import com.poc.recommender.mapper.UserItemViewMapper;
import com.poc.recommender.services.CustomRecommenderService;
import com.poc.recommender.services.UserItemService;

@RestController
public class UserItemViewController {

	@Autowired
	UserItemService userItemService;
	@Autowired
	CustomRecommenderService customRecommenderService;

	@PostMapping("/createitemviewrecord")
	public UserItemViewDto create(@RequestBody @Valid UserItemViewDto userItemDto) {
		try {
			UserItemView userItem = UserItemViewMapper.INSTANCE.userItemViewDtoToUserItemView(userItemDto);
			userItem.setDeleted_at(null);
			UserItemView itemViews = userItemService.addViews(userItem);
			if (itemViews != null) {
				return UserItemViewMapper.INSTANCE.userItemViewToUserItemViewDto(itemViews);
			}
			throw new GenericCustomException("Not able to fullfil the request. Kindly try later or contact support");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@GetMapping("/recommendation/{appUserId}")
	public Map<String, Object> recommendItem(@PathVariable @Valid Long appUserId) {
		try {
			return customRecommenderService.recommendItem(appUserId);
		} catch (UnknownHostException | TasteException e) {
			e.printStackTrace();
			throw new GenericCustomException(e.getMessage());
		}

	}

}
