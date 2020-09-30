package com.poc.recommender.services;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.poc.recommender.entities.CustomItemSequence;

@Service
public class NextItemSequenceService {
	
	@Autowired private MongoOperations mongo;

    public Long getNextSequence(String seqName)
    {
    	CustomItemSequence counter = mongo.findAndModify(
            query(where("_id").is(seqName)),
            new Update().inc("seq",1),
            options().returnNew(true).upsert(true),
            CustomItemSequence.class);
        return counter.getSeq();
    }

}
