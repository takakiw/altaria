package com.altaria.rabbitmq.config.entity.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecycleMqType {
    private Long uid;
    private List<Long> fids;
}
