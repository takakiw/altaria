package com.altaria.common.pojos.note.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteDTO {
    private Long id;
    private Long cid;
    private String title;
    private String text;
    private Boolean isPrivate;
}
