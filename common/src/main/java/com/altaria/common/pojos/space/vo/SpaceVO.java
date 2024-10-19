package com.altaria.common.pojos.space.vo;

import lombok.Data;

import java.io.Serializable;



@Data
public class SpaceVO implements Serializable {
    private Long uid;
    private Long useSpace;
    private Long totalSpace;
}
