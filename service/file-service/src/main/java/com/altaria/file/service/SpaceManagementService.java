package com.altaria.file.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.space.entity.Space;

public interface SpaceManagementService {

    Space getUsedSpace(Long uid);

    Result updateSpace(Long uid, Long usedSpace);

    Result createSpace(Long uid, Space space);

}
