
package com.leostormer.strife.friends;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CustomFriendRequestRepositoryImpl implements CustomFriendRequestRepository {

        @Autowired
        private MongoTemplate mongoTemplate;

        @Override
        public List<FriendRequest> findAllUserRequests(ObjectId userId) {
                Query query = new Query();
                query.addCriteria(new Criteria().orOperator(
                                Criteria.where("user1").is(userId),
                                Criteria.where("user2").is(userId)));

                return mongoTemplate.find(query, FriendRequest.class);
        }

        @Override
        public List<FriendRequest> findAllUserBlockedRequests(ObjectId userId) {
                Query query = new Query();
                query.addCriteria(new Criteria().andOperator(
                                new Criteria().orOperator(
                                                Criteria.where("user1Response").is(FriendRequestResponse.BLOCKED),
                                                Criteria.where("user2Response").is(FriendRequestResponse.BLOCKED)),
                                new Criteria().orOperator(Criteria.where("user1").is(userId),
                                                Criteria.where("user2").is(userId))));

                return mongoTemplate.find(query, FriendRequest.class);
        }

        @Override
        public List<FriendRequest> findAllUserAcceptedRequests(ObjectId userId) {
                Query query = new Query();
                query.addCriteria(new Criteria().andOperator(
                                Criteria.where("user1Response").is(FriendRequestResponse.ACCEPTED),
                                Criteria.where("user2Response").is(FriendRequestResponse.ACCEPTED),
                                new Criteria().orOperator(Criteria.where("user1").is(userId),
                                                Criteria.where("user2").is(userId))));

                return mongoTemplate.find(query, FriendRequest.class);

        }

        @Override
        public List<FriendRequest> findAllUserPendingRequests(ObjectId userId) {
                Query query = new Query();
                query.addCriteria(new Criteria().andOperator(
                                new Criteria().andOperator(
                                                Criteria.where("user1Response").is(FriendRequestResponse.ACCEPTED),
                                                Criteria.where("user2Response").is(FriendRequestResponse.PENDING)),
                                new Criteria().orOperator(Criteria.where("user1").is(userId),
                                                Criteria.where("user2").is(userId))));

                return mongoTemplate.find(query, FriendRequest.class);
        }

}