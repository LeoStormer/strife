package com.leostormer.strife.server;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "servers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Server {
    @Id
    private ObjectId id;

    private String name;

    private String description;
}
