package com.altaria.feign.fallback;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.space.entity.Space;
import com.altaria.common.pojos.space.vo.SpaceVO;
import com.altaria.feign.client.SpaceServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class SpaceServiceClientFallbackFactory implements FallbackFactory<SpaceServiceClient> {
    @Override
    public SpaceServiceClient create(Throwable cause) {
        return new SpaceServiceClient() {

            @Override
            public Result<SpaceVO> space(Long uid) {
                SpaceVO spaceVO = new SpaceVO();
                spaceVO.setUid(uid);
                spaceVO.setUseSpace(0L);
                spaceVO.setTotalSpace(0L);
                return Result.success();
            }

            @Override
            public Result updateSpace(Long uid, Space space) {
                return Result.error("更新空间失败");
            }
        };
    }
}
