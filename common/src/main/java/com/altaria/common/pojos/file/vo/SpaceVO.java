package com.altaria.common.pojos.file.vo;

import lombok.Data;

import java.io.Serializable;



@Data
public class SpaceVO implements Serializable {
    private Long uid;
    private Long useSpace;
    private Long totalSpace;
    private Integer fileCount;
}
