package com.leostormer.strife.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    private Update getUpdateObject(UserUpdate userUpdate) {
        Update update = new Update();

        if (userUpdate.getEmail() != null)
            update = update.set("email", userUpdate.getEmail());

        if (userUpdate.getProfilePic() != null)
            update = update.set("profilePic", userUpdate.getProfilePic());

        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isEmpty())
            update = update.set("password", userUpdate.getPassword());

        if (userUpdate.getUsername() != null && !userUpdate.getUsername().isEmpty()) {
            update = update.set("username", userUpdate.getUsername());
        }

        return update;
    }

    @Override
    public Optional<User> findOneByUsername(String username) {
        return Optional
                .ofNullable(mongoTemplate.findOne(new Query(Criteria.where("username").is(username)), User.class));
    }

    @Override
    public User updateUserDetails(ObjectId userId, UserUpdate userUpdate) {
        return mongoTemplate.findAndModify(new Query(Criteria.where("_id").is(userId)), getUpdateObject(userUpdate),
                FindAndModifyOptions.options().returnNew(true), User.class);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongoTemplate.exists(new Query(Criteria.where("username").is(username)), User.class);
    }

    public class FriendResult {
        private List<User> result = new ArrayList<>();

        public List<User> getResult() {
            return result;
        }

        public void setResult(List<User> result) {
            this.result = result;
        }
    }

    @Override
    public List<User> getFriends(ObjectId userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(userId)),
                Aggregation.lookup("users", "friends", "_id", "result"),
                Aggregation.unwind("result"),
                Aggregation.replaceRoot("result"));

        AggregationResults<User> results = mongoTemplate.aggregate(aggregation, User.class, User.class);
        return results.getMappedResults();
    }

    @Override
    public List<User> getBlockedUsers(ObjectId userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(userId)),
                Aggregation.lookup("users", "blockedUsers", "_id", "result"),
                Aggregation.unwind("result"),
                Aggregation.replaceRoot("result"));

        AggregationResults<User> results = mongoTemplate.aggregate(aggregation, User.class, User.class);
        return results.getMappedResults();
    }

    @Override
    public void acceptFriendRequest(ObjectId userId, ObjectId otherUserId) {
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(userId)),
                new Update().addToSet("friends", otherUserId), User.class);
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(otherUserId)),
                new Update().addToSet("friends", userId), User.class);
    }

    @Override
    public void removeFriendRequest(ObjectId userId, ObjectId otherUserId) {
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(userId)),
                new Update().pull("friends", otherUserId), User.class);
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(otherUserId)),
                new Update().pull("friends", userId), User.class);
    }

    @Override
    public void blockUser(ObjectId userId, ObjectId userToBlockId) {
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(userId)),
                new Update().pull("friends", userToBlockId).addToSet("blockedUsers", userToBlockId), User.class);
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(userToBlockId)),
                new Update().pull("friends", userId), User.class);
    }

    @Override
    public void unblockUser(ObjectId userId, ObjectId userToUnblockId) {
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(userId)),
                new Update().pull("blockedUsers", userToUnblockId), User.class);
    }
}
