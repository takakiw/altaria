package com.altaria.common.pojos.note.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteVO {
    private Long id;
    private String categoryName;
    private String title;
    private String text;
    private Integer commentCount;
    private Boolean isPrivate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
