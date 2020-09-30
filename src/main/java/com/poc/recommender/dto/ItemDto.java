package com.poc.recommender.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;

import com.poc.recommender.entities.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
	
	@NotNull(message="Item Application Id cannot be null")
	private Long appItemId;
	@NotNull(message="Item name cannot be null")
	private String itemName;
	private boolean isPublished;

}
