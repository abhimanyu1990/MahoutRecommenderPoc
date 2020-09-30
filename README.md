# MahoutRecommenderPoc
This project is proof-of-concept work of developing Mahout Recommendation Engine. The technology used is Spring Boot framework and MongoDB as the database.

In this project, I have overwritten the MongoDBDataModel class code mentioned in the opensource "Mahout-Integration" project to create the data model as per requirement and
also updated the code as per new MongoDB clent. I have used the source code and modify it rather than extending the MongoDBDataModel class. MongoDBDataModel class is final class and it extends the DataModel class of the Mahout. 

I have implemented Mahout Logliklihood Algorithm to predict the Item for a given User. This project can be easily extended for the preference based collaborative filtering also and we can use the custom data model that has been implemented in this project.

This application have following things implemented

1. Mahout Logliklihood Algorithm implementation
2. Custom Exception Handling
3. Swagger Implementation for API documentation
4. Lombok implementation for cleaner code
5. MapStruct implementation for easy DTO to Entity and vise-versa conversion.
