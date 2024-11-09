package com.altaria.share.mapper;

import com.altaria.common.pojos.share.entity.Share;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShareMapper {
    int insert(Share dbShare);

    List<Share> select(Share share);

    Share getShareById(@Param("shareId") Long shareId);

    int deleteByIds(@Param("ids") List<Long> ids, @Param("uid") Long uid);

    @Delete("DELETE FROM share WHERE expire < NOW()")
    void deleteByExpire();

    List<Share> getShareByIdBatch(@Param("uid") Long uid, @Param("ids") List<Long> ids);

    @Select("SELECT * FROM share WHERE expire < NOW()")
    List<Share> getExpiredShare();

    @Update("UPDATE share SET visit = visit + 1 where id = #{shareId}")
    void incrementVisit(@Param("shareId") Long shareId);
}
