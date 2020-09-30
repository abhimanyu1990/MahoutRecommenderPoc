package com.poc.recommender.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import com.poc.recommender.dto.ItemDto;
import com.poc.recommender.entities.Item;


@Mapper( unmappedTargetPolicy = ReportingPolicy.IGNORE,componentModel = "spring")
public interface ItemMapper {
	public static final ItemMapper INSTANCE = Mappers.getMapper( ItemMapper.class ); 
    public ItemDto itemToItemDto(Item item); 
    public Item  itemDtoToItem(ItemDto itemDto);
}
