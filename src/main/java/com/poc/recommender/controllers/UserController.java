package com.poc.recommender.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.poc.recommender.customexceptions.GenericCustomException;
import com.poc.recommender.customexceptions.GenericNotFoundException;
import com.poc.recommender.dto.UserDto;
import com.poc.recommender.entities.User;
import com.poc.recommender.mapper.UserMapper;
import com.poc.recommender.services.UserService;

@RestController
public class UserController {

	public static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userService;

	@Autowired
	UserMapper userMapper;

	@PostMapping("/user")
	public UserDto create(@RequestBody @Valid UserDto userDto) {
		try {
			User user = UserMapper.INSTANCE.userDTOToUser(userDto);
			user = userService.addUser(user);
			if (user != null) {
				return UserMapper.INSTANCE.userToUserDTO(user);
			}
			throw new GenericCustomException("Not able to fullfil the request. Kindly try later.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericCustomException(e.getMessage());
		}
	}

	

	@GetMapping("/user/appid/{appUserId}")
	public UserDto findByAppUserId(@PathVariable @Valid Long appUserId) {
		
			User user = userService.findUserByAppUserId(appUserId);
			if (user != null) {
				return UserMapper.INSTANCE.userToUserDTO(user);
			}
			throw new GenericNotFoundException("User doesn't exist.");
		
		
	}

}
