package com.altaria.file.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.Space;
import com.altaria.common.pojos.file.vo.SpaceVO;
import com.altaria.file.mapper.SpaceMapper;
import com.altaria.file.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpaceServiceImpl implements SpaceService {

    @Autowired
    private SpaceMapper spaceMapper;

    @Override
    public Result<SpaceVO> getUsedSpace(Long uid) {
        if (uid == null){
            return Result.error();
        }
        Space space = spaceMapper.getUsedSpace(uid);
        return Result.success(BeanUtil.copyProperties(space, SpaceVO.class));
    }

    @Override
    public Result updateSpace(Long uid, Long usedSpace, Integer fileCount) {
        if (uid == null || usedSpace == null || fileCount == null){
            return Result.error();
        }
        Space dbSpace = new Space();
        dbSpace.setUid(uid);
        dbSpace.setUseSpace(usedSpace);
        dbSpace.setFileCount(fileCount);
        int updateSpace = spaceMapper.updateSpace(dbSpace);
        if (updateSpace == 0){
            return Result.error();
        }
        return Result.success();
    }

    @Override
    public Result<SpaceVO> createSpace(Long uid, Space space) {
        if (uid == null || space == null){
            return Result.error();
        }
        space.setUid(uid);
        int insert = spaceMapper.insert(space);
        if (insert == 0){
            return Result.error();
        }
        return Result.success(BeanUtil.copyProperties(space, SpaceVO.class));
    }

}
