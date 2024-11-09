package com.altaria.common.pojos.file.entity;


import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo implements Serializable{
    private Long id; //主键ID
    private Long uid; // 用户ID
    private String url; // 文件在minio上的路径
    private String fileName; // 文件名
    private Integer type; // 文件类型, 1图片， 2视频， 3音频, 4pdf， 5word， 6excel，7txt， 8其他
    private Long size; // 文件大小
    private String md5; // 文件的md5值
    private Long pid; // 父级目录ID
    private Integer status; // 状态，0正常，1删除, 2回收站
    private String cover; // 封面图片路径
    private Integer transformed; // 转码状态，0转码完成，1转码中，2转码失败
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
