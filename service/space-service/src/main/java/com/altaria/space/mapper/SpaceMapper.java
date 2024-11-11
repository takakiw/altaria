package com.altaria.space.mapper;


import com.altaria.common.pojos.space.entity.Space;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SpaceMapper {

    @Select("select * from space where uid = #{uid}")
    Space getUsedSpace(Long uid);


    int updateSpace(Space dbSpace);

    @Insert("insert into space(uid) values (#{uid})")
    int insert(Space space);

    @Delete("delete from space where uid = #{uid}")
    int deleteSpace(Long uid);

    @Update("update space set note_coount = note_coount + #{count} where uid = #{uid}")
    int incrementNoteCount(@Param("uid") Long uid, @Param("count") Integer count);
}
