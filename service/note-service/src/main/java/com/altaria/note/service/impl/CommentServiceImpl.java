package com.altaria.note.service.impl;

import cn.hutool.core.util.IdUtil;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Comment;
import com.altaria.note.mapper.CommentMapper;
import com.altaria.note.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Result<List<Comment>> list(Long nid) {
        if (nid == null){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        List<Comment> comments = commentMapper.list(nid);
        return Result.success(comments);
    }

    @Override
    public Result add(Long uid, Long nid, Long pid, String content) {
        if (uid == null){
            return Result.success(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        if (nid == null || content == null){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        Comment dbComment = new Comment();
        dbComment.setId(IdUtil.getSnowflake().nextId());
        dbComment.setNid(nid);
        dbComment.setUid(uid);
        dbComment.setContent(content);
        dbComment.setPid(pid);
        dbComment.setCreateTime(LocalDateTime.now());
        int i = commentMapper.insert(dbComment);
        if (i == 0){
            return Result.error();
        }
        return Result.success();
    }

    @Override
    public Result delete(Long uid, Long id) {
        if (uid == null) {
            return Result.success(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        if (id == null) {
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        int i = commentMapper.delete(uid, id);
        if (i == 0) {
            return Result.error();
        }
        return Result.success();
    }
}
