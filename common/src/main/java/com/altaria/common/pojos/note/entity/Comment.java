package com.altaria.common.pojos.note.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long nid;
    private Long uid;
    private Long toId;
    private String content;
    private Long pid;
    private LocalDateTime createTime;
}
