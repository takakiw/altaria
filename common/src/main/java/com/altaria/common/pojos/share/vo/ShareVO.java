package com.altaria.common.pojos.share.vo;

import com.altaria.common.pojos.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShareVO {
    private Long id;
    private Long uid;
    private String name;
    private Integer type;
    private LocalDateTime expire;
    private LocalDateTime createTime;
}