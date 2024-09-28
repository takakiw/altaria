package com.altaria.user.mapper;

import com.altaria.common.annotation.AutoFill;
import com.altaria.common.enums.OperationType;
import com.altaria.common.pojos.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    User getUserById(Integer userId);

    User select(User user);

    @AutoFill(OperationType.INSERT)
    int insert(User user);

    @Select("SELECT * FROM tb_user WHERE email = #{email}")
    User getUserByEmail(String email);

    @Select("SELECT * FROM tb_user WHERE user_name = #{userName}")
    User getUserByUserName(String userName);
}
