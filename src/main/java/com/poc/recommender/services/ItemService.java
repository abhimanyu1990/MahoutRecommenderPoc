package com.poc.recommender.services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poc.recommender.entities.Item;
import com.poc.recommender.repository.ItemRepository;

@Service
public class ItemService {
	
	@Autowired ItemRepository itemRepository;
	@Autowired NextItemSequenceService nextItemSequenceService;
	public Item addItem(Item item) {
		item.set_id(ObjectId.get());
		item.setItemId(item.get_id().toHexString());
		item.setSequence(nextItemSequenceService.getNextSequence("item_sequence"));
		return itemRepository.save(item);	
	}
	

	public Item findItemByAppItemId(Long appItemId) {
		return itemRepository.findByAppItemId(appItemId);
	}
	

}
