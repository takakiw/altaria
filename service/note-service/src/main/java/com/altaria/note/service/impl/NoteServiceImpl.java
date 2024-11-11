package com.altaria.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.altaria.common.constants.NoteConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Note;
import com.altaria.common.pojos.note.entity.Category;
import com.altaria.common.pojos.note.vo.NoteListVO;
import com.altaria.common.pojos.note.vo.NoteVO;
import com.altaria.common.utils.SignUtil;
import com.altaria.note.mapper.CategoryMapper;
import com.altaria.note.mapper.CommentMapper;
import com.altaria.note.mapper.NoteMapper;
import com.altaria.note.service.NoteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;



    @Override
    public Result createNote(String title, String text, Boolean isPrivate, String categoryName, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        if (StringUtils.isBlank(title)){
            title = NoteConstants.DEFAULT_TITLE;
        }
        isPrivate = Boolean.TRUE.equals(isPrivate) ? Boolean.TRUE : Boolean.FALSE;
        Note dbNote = new Note();
        dbNote.setId(IdUtil.getSnowflake().nextId());
        dbNote.setUid(uid);
        dbNote.setTitle(title);
        dbNote.setText(text);
        dbNote.setIsPrivate(isPrivate);
        if (categoryName != null){
            Category category = categoryMapper.getCategoryByNames(categoryName, uid);
            if (category == null){
                return Result.error(StatusCodeEnum.CATEGORY_NOT_FOUND);
            }
            dbNote.setCid(category.getId());
        }
        int i = noteMapper.saveNote(dbNote);
        if (i > 0){
            rabbitTemplate.convertAndSend("update-note-count-queue", uid+ ":" + 1);
            return Result.success();
        }
        return Result.error(StatusCodeEnum.CREATE_NOTE_FAILED);
    }

    @Override
    public Result updateNote(Long id, String title, String text, Boolean isPrivate, String categoryName, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        Note noteById = noteMapper.getNoteById(id, uid);
        if (noteById == null){
            return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
        }
        if (categoryName != null){
            Category category = categoryMapper.getCategoryByNames(categoryName, uid);
            if (category == null){
                return Result.error(StatusCodeEnum.CATEGORY_NOT_FOUND);
            }
            noteById.setCid(category.getId());
        }
        noteById.setTitle(title);
        noteById.setText(text);
        noteById.setIsPrivate(isPrivate);
        noteById.setUpdateTime(LocalDateTime.now());
        int i = noteMapper.updateNote(noteById);
        if (i > 0){
            return Result.success();
        }
        return Result.error(StatusCodeEnum.UPDATE_NOTE_FAILED);
    }

    @Override
    @Transactional
    public Result deleteNote(Long id, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        int i = noteMapper.deleteNote(id, uid);
        if (i > 0){
            commentMapper.deleteByNoteId(uid, id);
            rabbitTemplate.convertAndSend("update-note-count-queue", uid+ ":" + -1);
            return Result.success();
        }
        return Result.error(StatusCodeEnum.DELETE_NOTE_FAILED);
    }

    @Override
    public Result<PageResult<NoteListVO>> getNoteList(String category, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        Note query = new Note();
        query.setUid(uid);
        if (StringUtils.isNotBlank(category)){ // 分组查找, 默认查找 cid is null 的笔记(未分组)
            Category categoryByName = categoryMapper.getCategoryByNames(category, uid);
            if (categoryByName == null){
                return Result.error(StatusCodeEnum.CATEGORY_NOT_FOUND);
            }
            query.setCid(categoryByName.getId());
        }
        List<Note> notes = noteMapper.selectNote(query);
        List<NoteListVO> noteListVOS = BeanUtil.copyToList(notes, NoteListVO.class);
        return Result.success(new PageResult<>(noteListVOS.size(), noteListVOS));
    }

    @Override
    public Result<NoteVO> getNoteInfo(Long id, Long uid) {
        Note noteById = noteMapper.getNoteById(id, uid);
        if (noteById == null){
            return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
        }
        if (uid == null && noteById.getIsPrivate()) // 私密笔记需要验证 token
        {
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        NoteVO noteInfo = BeanUtil.copyProperties(noteById, NoteVO.class);
        noteInfo.setCategoryName(Optional.ofNullable(noteById.getCid()).map(c -> categoryMapper.getCategoryById(c, uid).getName()).orElse(null));
        return Result.success(noteInfo);
    }

    @Override
    public Result<List<NoteListVO>> getShareList(Long uid) {
        Note query = new Note();
        query.setUid(uid);
        query.setIsPrivate(Boolean.FALSE); // 查找公开笔记
        List<Note> notes = noteMapper.selectNote(query);
        List<NoteListVO> noteListVOS = BeanUtil.copyToList(notes, NoteListVO.class);
        return Result.success(noteListVOS);
    }

}
