package com.poc.recommender.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import com.poc.recommender.dto.UserItemViewDto;
import com.poc.recommender.entities.UserItemView;

@Mapper( unmappedTargetPolicy = ReportingPolicy.IGNORE,componentModel = "spring")
public interface UserItemViewMapper {
	public static final UserItemViewMapper INSTANCE = Mappers.getMapper( UserItemViewMapper.class ); 
    public UserItemViewDto userItemViewToUserItemViewDto(UserItemView userItemView); 
    public UserItemView  userItemViewDtoToUserItemView(UserItemViewDto userItemView);
}
