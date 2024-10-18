package com.altaria.common.pojos.share.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShareVO {
    private Long id;
    private Long uid;
    private List<Long> fids;
    private LocalDateTime expire;
    private String sign;
    private String url;
    private LocalDateTime createTime;
}