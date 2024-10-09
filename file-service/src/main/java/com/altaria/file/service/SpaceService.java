package com.altaria.file.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.Space;
import com.altaria.common.pojos.file.vo.SpaceVO;

public interface SpaceService {
    Result<SpaceVO> getUsedSpace(Long uid);

    Result updateSpace(Long uid, Long usedSpace, Integer fileCount);

    Result createSpace(Long uid, Space space);
}
