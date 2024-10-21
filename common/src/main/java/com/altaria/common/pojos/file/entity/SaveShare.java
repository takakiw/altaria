package com.altaria.common.pojos.file.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveShare {
    List<Long> fids;
    Long shareUid;
    Long path;
    Long userId;
}
