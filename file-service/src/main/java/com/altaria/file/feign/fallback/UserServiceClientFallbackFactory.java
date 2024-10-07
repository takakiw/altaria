package com.altaria.file.feign.fallback;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.file.feign.client.UserServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient>{

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public Result getShareUserById(Long uId) {
                UserVO userVO = new UserVO();
                return Result.success(userVO);
            }
        };
    }
}
