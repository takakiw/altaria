package com.altaria.user.mapper;

import com.altaria.common.pojos.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User getUserById(Integer userId);

    User select(User user);
}
