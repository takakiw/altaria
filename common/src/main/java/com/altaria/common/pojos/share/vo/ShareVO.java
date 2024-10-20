package com.altaria.common.pojos.share.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareVO {
    private Long id;
    private Long uid;
    private String name;
    private Integer type;
    private LocalDateTime expire;
    private LocalDateTime createTime;
}