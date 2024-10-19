package com.altaria.space.controller;


import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.space.entity.Space;
import com.altaria.common.pojos.space.vo.SpaceVO;
import com.altaria.space.service.SpaceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/space")
public class SpaceManagementController {

    @Autowired
    private SpaceManagementService spaceManagementService;


    /**
     * 获取空间信息
     * @param uid
     * @return
     */
    @GetMapping("/info")
    public Result<SpaceVO> space(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        if (uid == null){
            return Result.error();
        }
        return Result.success(BeanUtil.copyProperties(spaceManagementService.getUsedSpace(uid), SpaceVO.class));
    }

    /**
     *  修改空间容量
     * @param uid
     * @param space
     * @return
     */
    @PutMapping("/update")
    public Result updateSpace(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                              @RequestBody Space space) {
        return spaceManagementService.updateSpace(uid, space.getUseSpace());
    }
}
