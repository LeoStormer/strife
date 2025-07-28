package com.leostormer.strife.message;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageSearchOptions {
    @Builder.Default
    private int limit = 100;

    @Builder.Default
    private Date timestamp = new Date(0);

    @Builder.Default
    private MessageSearchDirection searchDirection = MessageSearchDirection.ASCENDING;

    public static MessageSearchOptions latest() {
        return MessageSearchOptions.builder().searchDirection(MessageSearchDirection.DESCENDING)
                .timestamp(new Date()).build();
    }

    public static MessageSearchOptions earliest() {
        return MessageSearchOptions.builder().build();
    }
}
