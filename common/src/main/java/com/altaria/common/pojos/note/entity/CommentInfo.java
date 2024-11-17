package com.altaria.common.pojos.note.entity;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
public class CommentInfo {
    private Long id; // id
    private Long uid; // 用户id
    private Long nid; // 笔记id
    private Long pid; // 顶级父id
    private Long toId; // 回复id
    private String content; // 评论内容
    private LocalDateTime createTime; // 创建时间
    private String nickName; // 用户昵称
    private String avatar; // 用户头像
    private List<CommentInfo> childrenComment; // 子评论
}