package com.altaria.space.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.space.entity.Space;
import com.altaria.common.pojos.space.vo.SpaceVO;
import com.altaria.space.cache.SpaceCacheService;
import com.altaria.space.mapper.SpaceMapper;
import com.altaria.space.service.SpaceManagementService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpaceManagementServiceImpl implements SpaceManagementService {

    @Autowired
    private SpaceMapper spaceMapper;

    @Autowired
    private SpaceCacheService cacheService;

    @Override
    public Space getUsedSpace(Long uid) {
        Space space = cacheService.getSpace(uid);
        if (space == null){
            space = spaceMapper.getUsedSpace(uid);
            if(space == null){ //第一次使用，初始化空间
                space = new Space();
                space.setUid(uid);
                space.setUseSpace(0L);
                space.setTotalSpace(1024 * 1024 * 1024L); //默认1G
                spaceMapper.insert(space);
            }
            cacheService.saveSpace(space);
        }
        return space;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateSpace(Long uid, Long usedSpace) {
        if (uid == null || usedSpace == null){
            return Result.error();
        }
        Space dbSpace = new Space();
        dbSpace.setUid(uid);
        dbSpace.setUseSpace(usedSpace);
        int updateSpace = spaceMapper.updateSpace(dbSpace);

        if (updateSpace == 0){
            return Result.error("更新空间失败");
        }
        cacheService.deleteSpace(uid);
        return Result.success();
    }

    @Override
    public Result updateNote(Long uid, Integer noteCount) {
        if (uid == null || noteCount == null){
            return Result.success();
        }
        Space dbSpace = new Space();
        dbSpace.setUid(uid);
        dbSpace.setNoteCount(noteCount);
        int updateNote = spaceMapper.updateSpace(dbSpace);
        if (updateNote == 0){
            return Result.error("更新笔记数失败");
        }
        cacheService.deleteSpace(uid);
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
