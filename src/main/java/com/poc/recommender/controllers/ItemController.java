package com.poc.recommender.controllers;

import javax.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.poc.recommender.customexceptions.GenericCustomException;
import com.poc.recommender.customexceptions.GenericNotFoundException;
import com.poc.recommender.dto.ItemDto;
import com.poc.recommender.entities.Item;
import com.poc.recommender.mapper.ItemMapper;
import com.poc.recommender.services.ItemService;

@RestController
public class ItemController {

	@Autowired
	ItemService itemService;

	@PostMapping("/item")
	public ItemDto create(@RequestBody @Valid ItemDto itemDto) {
		
			Item item = ItemMapper.INSTANCE.itemDtoToItem(itemDto);
			item = itemService.addItem(item);
			if (item != null) {
				return ItemMapper.INSTANCE.itemToItemDto(item);
			}
			throw new GenericCustomException("Not able to fullfil the request. Kindly try later.");
		
	}



	@GetMapping("/item/itemid/{appitemid}")
	public ItemDto findByAppItemId(@PathVariable @Valid Long appitemid) {
		
			Item item = itemService.findItemByAppItemId(appitemid);
			if (item != null) {
				return ItemMapper.INSTANCE.itemToItemDto(item);
			}
			throw new GenericNotFoundException("Item doesn't exist.");
		
		
	}
}
