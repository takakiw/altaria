package com.altaria.common.pojos.file.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class FileInfoVO {
    private Long id; //主键ID
    private Long uid; // 用户ID
    private String url; // 文件在minio上的路径
    private String fileName; // 文件名
    private Integer type; // 文件类型, 0文件夹， 1文件， 2图片， 3视频， 4音频， 5压缩包， 6pdf， 7word， 8excel，9txt， 10其他
    private Long size; // 文件大小
    private Long pid; // 父级目录ID
    private Integer status; // 状态，0正常，1删除, 转码中，2转码失败
    private String cover; // 封面图片路径
    private Integer transformed; // 转码状态，0转码完成，1转码中，2转码失败
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
