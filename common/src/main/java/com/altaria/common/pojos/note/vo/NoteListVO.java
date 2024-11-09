package com.altaria.common.pojos.note.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteListVO {
    private Long id;
    private Long uid;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
