package com.altaria.common.pojos.note.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteListVO {
    private Long id;
    private String title;
    private Integer commentCount;
    private Boolean isPrivate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
