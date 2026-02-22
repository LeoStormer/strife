package com.leostormer.strife.member;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, ObjectId>, CustomMemberRepository {
    
}
