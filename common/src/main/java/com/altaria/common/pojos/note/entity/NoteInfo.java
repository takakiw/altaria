package com.altaria.common.pojos.note.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteInfo {
    private Long id;
    private Long uid;
    private String category;
    private String title;
    private String text;
    private Integer commentCount;
    private Boolean isPrivate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
