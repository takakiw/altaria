package com.altaria.note.mapper;

import com.altaria.common.pojos.note.entity.Note;
import com.altaria.common.pojos.note.entity.NoteInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NoteMapper {
    int saveNote(Note note);

    @Select("SELECT * FROM note WHERE id = #{id}")
    Note getNoteById(@Param("id") Long id);

    int updateNote(Note note);

    @Delete("DELETE FROM note WHERE id = #{id} AND uid = #{uid}")
    int deleteNote(@Param("id") Long id, @Param("uid") Long uid);

    List<Note> selectNote(Note note);

    @Delete("DELETE FROM note WHERE cid = #{cid} AND uid = #{uid}")
    void deleteNoteByCategory(@Param("cid") Long id, @Param("uid") Long uid);


    List<NoteInfo> getPublicNoteInfo(@Param("uid") Long uid);

    @Update("UPDATE note SET comment_count = comment_count + #{i} WHERE id = #{nid}")
    void incrCommentCount(@Param("nid") Long nid, @Param("i") int i);
}
