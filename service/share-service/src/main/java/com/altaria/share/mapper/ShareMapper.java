package com.altaria.share.mapper;

import com.altaria.common.pojos.share.entity.Share;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShareMapper {
    int insert(Share dbShare);

    List<Share> select(Share share);

    @Select("SELECT * FROM share WHERE id = #{shareId} AND expire > NOW()")
    Share getShareById(@Param("shareId") Long shareId);

    int deleteByIds(@Param("ids") List<Long> ids, @Param("uid") Long uid);

    @Delete("DELETE FROM share WHERE expire < NOW()")
    void deleteByExpire();
}
