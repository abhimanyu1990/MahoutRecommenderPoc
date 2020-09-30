package com.poc.recommender.entities;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection="user_item_view")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
	  @CompoundIndex(def = "{'item_id':1, 'user_id':1}", name = "compound_index_1", unique=true)
	})
public class UserItemView extends BasicEntity  {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private ObjectId _id;
	@NotNull(message="Item Id cannot be null")
	private Long item_id;
	@NotNull(message="App user Id cannot be null")
	private Long user_id;
	private String preference;
	private Date deleted_at;
	private Date created_at = new Date();
	

}
