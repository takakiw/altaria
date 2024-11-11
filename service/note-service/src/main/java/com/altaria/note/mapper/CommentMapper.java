package com.altaria.note.mapper;

import com.altaria.common.pojos.note.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Select("SELECT * FROM comments WHERE nid = #{nid}")
    List<Comment> list(@Param("nid") Long nid);

    @Insert("insert into comments(id, nid, uid, content, created_time, pid) VALUES (#{id}, #{nid}, #{uid}, #{content}, #{createdTime}, #{pid})")
    int insert(Comment dbComment);

    @Delete("DELETE FROM comments WHERE nid = #{nid} AND id = #{id} AND uid = #{uid}")
    int delete(@Param("uid") Long uid, @Param("id") Long id);

    @Delete("DELETE FROM comments WHERE nid = #{nid}")
    int deleteByNoteId(@Param("uid") Long uid, @Param("nid") Long nid);
}
