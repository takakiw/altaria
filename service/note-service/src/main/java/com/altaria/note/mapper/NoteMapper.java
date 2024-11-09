package com.altaria.note.mapper;

import com.altaria.common.pojos.note.entity.Note;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteMapper {
    int saveNote(Note note);

    @Select("SELECT * FROM note WHERE id = #{id} AND uid = #{uid}")
    Note getNoteById(@Param("id") Long id, @Param("uid") Long uid);

    int updateNote(Note note);

    @Delete("DELETE FROM note WHERE id = #{id} AND uid = #{uid}")
    int deleteNote(@Param("id") Long id, @Param("uid") Long uid);

    List<Note> selectNote(Note note);

    @Delete("DELETE FROM note WHERE cid = #{cid} AND uid = #{uid}")
    void deleteNoteByCategory(@Param("cid") Long id, @Param("uid") Long uid);
}
