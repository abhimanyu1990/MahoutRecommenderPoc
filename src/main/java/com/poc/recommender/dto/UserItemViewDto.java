package com.poc.recommender.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserItemViewDto {
	
	@NotNull(message="Item Id cannot be null")
	private Long item_id;
	@NotNull(message="App user Id cannot be null")
	private Long user_id;
	private String preference;

}
