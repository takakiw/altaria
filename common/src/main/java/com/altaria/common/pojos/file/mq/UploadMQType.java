package com.altaria.common.pojos.file.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadMQType {
    private Long uid;
    private Long dbId;
    private Long fid;
    private String contentType;
    private String suffix;
    private String tempPath;
    private String md5;
}
