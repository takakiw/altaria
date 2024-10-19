package com.altaria.space.mapper;


import com.altaria.common.pojos.space.entity.Space;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpaceMapper {

    @Select("select * from space where uid = #{uid}")
    Space getUsedSpace(Long uid);


    int updateSpace(Space dbSpace);

    @Insert("insert into space(uid) values (#{uid})")
    int insert(Space space);

    @Delete("delete from space where uid = #{uid}")
    int deleteSpace(Long uid);
}
