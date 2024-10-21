package com.altaria.common.pojos.share.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Share {
    private Long id;
    private Long uid;
    private String name;
    @NotNull
    private Integer type;
    @NotNull
    private List<Long> fids;
    private Long visit;
    @NotNull
    private LocalDateTime expire;
    private String sign;
    private String url;
    private LocalDateTime createTime;
}