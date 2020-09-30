package com.poc.recommender.entities;


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

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BasicEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private ObjectId _id;
	@Indexed(name = "meta_userId_index_unique",unique=true)
	private String userId;
	@Indexed(name = "meta_appUserId_index_unique",unique=true)
	private Long appUserId;
	@NotNull(message="User name cannot be null")
	private String name;
	@Indexed(unique=true)
	private Long sequence;
	
	
	
}
