package com.altaria.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Comment;
import com.altaria.common.pojos.note.entity.CommentInfo;
import com.altaria.common.pojos.note.entity.Note;
import com.altaria.note.cache.NoteCacheService;
import com.altaria.note.mapper.CommentMapper;
import com.altaria.note.mapper.NoteMapper;
import com.altaria.note.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private NoteCacheService cacheService;

    @Autowired
    private NoteMapper noteMapper;

    @Override
    public Result<List<CommentInfo>> list(Long nid, Long uid) {
        if (nid == null){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        Note note = cacheService.getNote(nid);
        if (note == null){
            note= noteMapper.getNoteById(nid);
        }
        if (note == null || note.getUid() == null){
            return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
        }
        if (note.getUid() != uid && note.getIsPrivate()){ // 私有笔记只能查看自己
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        List<CommentInfo> list = commentMapper.list(nid);
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result<CommentInfo> add(Long uid, Long nid, Long pid, Long toId, String content) {
        if (uid == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        if (nid == null || content == null){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        Note note = cacheService.getNote(nid);
        if (note == null || note.getUid() == null){
            return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
        }
        if (note.getUid() != uid && note.getIsPrivate()){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        Comment dbComment = new Comment();
        dbComment.setId(IdUtil.getSnowflake().nextId());
        dbComment.setNid(nid);
        dbComment.setUid(uid);
        dbComment.setContent(content);
        dbComment.setPid(pid);
        dbComment.setToId(toId);
        dbComment.setCreateTime(LocalDateTime.now());
        int i = commentMapper.insert(dbComment);
        if (i == 0){
            return Result.error();
        }
        noteMapper.incrCommentCount(nid, 1);
        cacheService.incrNoteCommentCount(nid, 1);
        return Result.success(BeanUtil.copyProperties(dbComment, CommentInfo.class));
    }

    @Override
    @Transactional
    public Result<Integer> delete(Long uid, Long id) {
        if (uid == null) {
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        if (id == null) {
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        Comment comment = commentMapper.getCommentById(id);
        // 删除该评论及其所有回复
        int j = commentMapper.deleteCommentAndChild(id);
        noteMapper.incrCommentCount(comment.getNid(), -j);
        cacheService.incrNoteCommentCount(comment.getNid(), -j);
        return Result.success(j);
    }
}
