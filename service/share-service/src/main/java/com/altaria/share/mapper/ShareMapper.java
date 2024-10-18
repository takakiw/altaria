package com.altaria.share.mapper;

import com.altaria.common.pojos.share.entity.Share;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShareMapper {
    int insert(Share dbShare);

    List<Share> select(Share share);

    @Select("SELECT * FROM share WHERE id = #{shareId}")
    Share getShareById(Long shareId);
}
