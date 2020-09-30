package com.poc.recommender.entities;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item extends BasicEntity  {
	
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private ObjectId _id;
	@Indexed(name = "meta_itemId_index_unique",unique=true)
	private String itemId;
	@Indexed(name = "meta_appItemId_index_unique",unique=true)
	private Long appItemId;
	@Indexed(unique=true)
	private Long sequence;
	@NotNull(message="Item name cannot be null")
	private String itemName;
	private boolean isPublished;
		

}
