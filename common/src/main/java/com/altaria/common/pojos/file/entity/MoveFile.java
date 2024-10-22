package com.altaria.common.pojos.file.entity;

import lombok.Data;

import java.util.List;

@Data
public class MoveFile {
    private Long oldPid;
    private Long pid;
    private List<Long> ids;
}
