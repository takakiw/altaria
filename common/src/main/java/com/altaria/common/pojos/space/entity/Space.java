package com.altaria.common.pojos.space.entity;

import lombok.Data;

import java.io.Serializable;



@Data
public class Space implements Serializable {
    private Integer id;
    private Long uid;
    private Long useSpace;
    private Long totalSpace;
    private Integer fileCount;

    public Space() {

    }

    public Space(Long uid, Long useSpace, Integer fileCount) {
        this.uid = uid;
        this.useSpace = useSpace;
        this.fileCount = fileCount;
    }
}
