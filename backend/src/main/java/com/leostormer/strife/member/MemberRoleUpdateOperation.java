package com.leostormer.strife.member;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRoleUpdateOperation {
    private final List<ObjectId> rolesToAdd;

    private final List<ObjectId> rolesToRemove;
}
