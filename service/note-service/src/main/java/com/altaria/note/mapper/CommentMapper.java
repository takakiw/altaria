package com.altaria.note.mapper;

import com.altaria.common.pojos.note.entity.Comment;
import com.altaria.common.pojos.note.entity.CommentInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 根据笔记ID查询评论列表
    List<CommentInfo> list(@Param("nid") Long nid);

    // 插入评论
    @Insert("insert into comments(id, nid, uid, pid, to_id, content, create_time) VALUES (#{id}, #{nid}, #{uid}, #{pid}, #{toId}, #{content}, #{createTime})")
    int insert(Comment dbComment);

    // 删除笔记下的所有评论
    @Delete("DELETE FROM comments WHERE nid = #{nid}")
    int deleteByNoteId(@Param("nid") Long nid);

    // 删除评论及其子评论
    @Delete("delete from comments where to_id = #{id} or pid = #{id} or id = #{id}")
    int deleteCommentAndChild(Long id);
}
