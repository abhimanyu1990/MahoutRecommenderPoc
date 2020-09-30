package com.poc.recommender.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


import com.poc.recommender.dto.UserDto;
import com.poc.recommender.entities.User;



@Mapper( componentModel = "spring")
public interface UserMapper {
		public static final UserMapper INSTANCE = Mappers.getMapper( UserMapper.class ); 
	    public UserDto userToUserDTO(User user); 
	    public User  userDTOToUser(UserDto userDto);
}
