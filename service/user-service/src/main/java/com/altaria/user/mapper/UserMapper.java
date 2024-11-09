package com.altaria.user.mapper;

import com.altaria.common.annotation.AutoFill;
import com.altaria.common.enums.OperationType;
import com.altaria.common.pojos.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    User getUserById(@Param("id") Long id);

    User select(User user);

    @AutoFill(OperationType.INSERT)
    int insert(User user);

    @Select("SELECT * FROM tb_user WHERE email = #{email}")
    User getUserByEmail(@Param("email") String email);

    @Select("SELECT * FROM tb_user WHERE user_name = #{userName}")
    User getUserByUserName(@Param("userName") String userName);

    @AutoFill(OperationType.UPDATE)
    void updateUser(User user);
}
