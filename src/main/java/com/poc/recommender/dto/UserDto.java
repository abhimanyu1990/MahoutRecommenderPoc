package com.poc.recommender.dto;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
	@NotNull(message="App userId cannot be null")
	private Long appUserId;
	@NotNull(message="Name cannot be null")
	private String name;
}
