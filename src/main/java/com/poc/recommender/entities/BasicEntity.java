package com.poc.recommender.entities;

import java.io.Serializable;
import java.util.Date;



import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(
        value = {"createdAt", "updatedAt"},
        allowGetters = true
)
public abstract class BasicEntity implements Serializable  {

    
	private static final long serialVersionUID = 1L;

    
    @LastModifiedDate
    private Date updatedAt;
    

}