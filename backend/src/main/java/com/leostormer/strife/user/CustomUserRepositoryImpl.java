package com.leostormer.strife.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.VariableOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    @NonNull
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
    public Optional<User> findOneByEmail(String email) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class));
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

    @Override
    public boolean existsByEmail(String email) {
        return mongoTemplate.exists(new Query(Criteria.where("email").is(email)), User.class);
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
    public RelationshipResponse getUserRelationships(ObjectId userId, int page, int size, String filter,
            String search) {
        Function<String, ConditionalOperators.IfNull> ifNullReturnEmptyList = fieldName -> ConditionalOperators
                .ifNull(fieldName).then(Collections.emptyList());

        Criteria filterCriteria = new Criteria();
        if ("Pending".equalsIgnoreCase(filter)) {
            filterCriteria.and("allRels.type").in("PENDING", "PENDING_OTHER");
        } else if ("Blocked".equalsIgnoreCase(filter)) {
            filterCriteria.and("allRels.type").is("BLOCKED");
        } else if ("All".equalsIgnoreCase(filter) || "Online".equalsIgnoreCase(filter)) {
            // split Online and All to account for user status (when implemented)
            // will look something like: filterCriteria.and("userData.status").ne("offline").exists(true);
            filterCriteria.and("allRels.type").is("FRIEND");
        }

        Criteria searchCriteria = (search != null && !search.trim().isEmpty())
                ? Criteria.where("userData.username").regex(search, "i")
                : new Criteria();

        Criteria finalCriteria = new Criteria().andOperator(filterCriteria, searchCriteria);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(userId)),
                Aggregation.lookup("friend_requests", "_id", "receiver", "incomingRequests"),
                Aggregation.lookup("friend_requests", "_id", "sender", "outgoingRequests"),

                Aggregation.project()
                        .and(ifNullReturnEmptyList.apply("friends")).as("friendsList")
                        .and(ifNullReturnEmptyList.apply("blockedUsers")).as("blockedList")
                        .and(ArrayOperators.Filter.filter(ifNullReturnEmptyList.apply("incomingRequests"))
                                .as("req")
                                .by(ComparisonOperators.Eq.valueOf("req.accepted").equalToValue(false)))
                        .as("pendingReceived")
                        .and(ArrayOperators.Filter.filter(ifNullReturnEmptyList.apply("outgoingRequests"))
                                .as("req")
                                .by(ComparisonOperators.Eq.valueOf("req.accepted").equalToValue(false)))
                        .as("pendingSent"),

                Aggregation.project()
                        .and(VariableOperators.mapItemsOf("friendsList").as("id")
                                .andApply(ctx -> new Document("userId", "$$id").append("type", "FRIEND")))
                        .as("f")
                        .and(VariableOperators.mapItemsOf("blockedList").as("id")
                                .andApply(ctx -> new Document("userId", "$$id").append("type", "BLOCKED")))
                        .as("b")
                        .and(VariableOperators.mapItemsOf("pendingReceived").as("r")
                                .andApply(ctx -> new Document("userId", "$$r.sender").append("type", "PENDING")
                                        .append("requestId", "$$r._id")))
                        .as("pr")
                        .and(VariableOperators.mapItemsOf("pendingSent").as("r")
                                .andApply(ctx -> new Document("userId", "$$r.receiver")
                                        .append("type", "PENDING_OTHER").append("requestId", "$$r._id")))
                        .as("ps"),

                Aggregation.project()
                        .and(ArrayOperators.ConcatArrays.arrayOf("f")
                                .concat("b").concat("pr").concat("ps"))
                        .as("allRels"),

                Aggregation.facet()
                        .and(
                                Aggregation.unwind("allRels"),
                                Aggregation.lookup("users", "allRels.userId", "_id", "userData"),
                                Aggregation.unwind("userData"),
                                Aggregation.match(finalCriteria),
                                Aggregation.count().as("count"))
                        .as("metadata").and(
                                Aggregation.unwind("allRels"),
                                Aggregation.lookup("users", "allRels.userId", "_id", "userData"),
                                Aggregation.unwind("userData"),
                                Aggregation.match(finalCriteria),
                                Aggregation.sort(Sort.Direction.ASC, "userData.username"),
                                Aggregation.skip((long) page * size),
                                Aggregation.limit(size),

                                Aggregation.project()
                                        .and("allRels.type").as("type")
                                        .and("allRels.requestId").as("requestId")
                                        .and("userData._id").as("user._id")
                                        .and("userData.username").as("user.username")
                                        .and("userData.profilePic").as("user.profilePic")
                                        .and("userData.createdDate").as("user.createdDate"))
                        .as("data"));

        Document result = mongoTemplate.aggregate(aggregation, User.class, Document.class).getUniqueMappedResult();

        if (result == null) {
            return new RelationshipResponse(0, page, size, Collections.emptyList());
        }

        // 1. Extract and map Total Count from the "metadata" facet
        List<Document> metadata = result.getList("metadata", Document.class);
        long total = metadata.isEmpty() ? 0L : metadata.get(0).getInteger("count").longValue();

        // 2. Extract and map List<Relationship> from the "data" facet
        List<Document> dataDocs = result.getList("data", Document.class);
        List<Relationship> relationships = dataDocs.stream()
                .map(doc -> mongoTemplate.getConverter().read(Relationship.class, doc))
                .collect(Collectors.toList());

        return new RelationshipResponse(total, page, size, relationships);
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
