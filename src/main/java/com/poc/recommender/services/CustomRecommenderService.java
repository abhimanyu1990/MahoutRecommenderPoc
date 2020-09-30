package com.poc.recommender.services;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.poc.recommender.controllers.UserController;
import com.poc.recommender.datamodel.RecommenderDataModel;

@Service
public class CustomRecommenderService {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(CustomRecommenderService.class);
	
	@Autowired MongoOperations mongoOps;
	
	@Value("${recommender.maxItemRecommendationListSize}")
	private int maxItemRecommendationListSize;
	@Value("${recommender.maxUserNeighborLimit}")
	private int maxUserNeighborLimit;
	
	public Map<String,Object> recommendItem(Long userId) throws UnknownHostException, TasteException {
		RecommenderDataModel dataModel = new RecommenderDataModel(mongoOps);
		UserSimilarity userSim = new LogLikelihoodSimilarity(dataModel);
		UserNeighborhood neighborhood =  new NearestNUserNeighborhood(maxUserNeighborLimit, userSim, dataModel);
		Recommender recommender = new GenericUserBasedRecommender( dataModel, neighborhood, userSim);
	    List<RecommendedItem>recommendations = recommender.recommend(userId, maxItemRecommendationListSize);
	    LOGGER.debug("recommendations ==== "+recommendations.size());
	    for (RecommendedItem recommendation : recommendations) {
	          LOGGER.debug("You may like movie " + recommendation.getItemID());
	    }
	    LOGGER.debug("neighbour ==== "+neighborhood.getUserNeighborhood(userId).length);
	    HashMap<String,Object> map = new HashMap<>();
        map.put("Neighbour", neighborhood.getUserNeighborhood(userId));
        map.put("recommendedItem",recommendations);
		return map;
		
	}
	
	

}
