package com.leostormer.strife.user;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RelationshipResponse {
    private long total;
    private int page;
    private int size;
    private List<Relationship> relationships;
}
