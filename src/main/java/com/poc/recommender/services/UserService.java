package com.poc.recommender.services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poc.recommender.entities.User;
import com.poc.recommender.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired UserRepository userRepository;
	@Autowired NextUserSequenceService nextUserSequenceService;
	
	public User addUser(User user) {
		user.set_id(ObjectId.get());
		user.setUserId(user.get_id().toHexString());
		user.setSequence(nextUserSequenceService.getNextSequence("user_sequence"));
		return userRepository.save(user);
	}
	
	
	public User findUserByAppUserId(Long appUserId) {
		return userRepository.findByAppUserId(appUserId);
	}

}
