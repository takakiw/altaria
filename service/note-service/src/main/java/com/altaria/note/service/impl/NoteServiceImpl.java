package com.altaria.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.altaria.common.constants.NoteConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Note;
import com.altaria.common.pojos.note.entity.Category;
import com.altaria.common.pojos.note.entity.NoteInfo;
import com.altaria.common.pojos.note.vo.NoteListVO;
import com.altaria.common.pojos.note.vo.NoteVO;
import com.altaria.note.cache.NoteCacheService;
import com.altaria.note.mapper.CategoryMapper;
import com.altaria.note.mapper.CommentMapper;
import com.altaria.note.mapper.NoteMapper;
import com.altaria.note.service.NoteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private NoteCacheService cacheService;



    @Override
    public Result createNote(String title, String text, Boolean isPrivate, Long cid, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        if (StringUtils.isBlank(title)){
            title = NoteConstants.DEFAULT_TITLE;
        }
        isPrivate = Boolean.TRUE.equals(isPrivate) ? Boolean.TRUE : Boolean.FALSE;
        Note dbNote = new Note();
        long nid = IdUtil.getSnowflake().nextId();
        dbNote.setId(nid);
        dbNote.setUid(uid);
        dbNote.setTitle(title);
        dbNote.setText(text);
        dbNote.setIsPrivate(isPrivate);
        dbNote.setCommentCount(0);
        if (cid != null){
            Category category = cacheService.getCategory(uid, cid);
            if (category == null){
                category = categoryMapper.getCategoryById(cid, uid);
                if (category == null){
                    return Result.error(StatusCodeEnum.CATEGORY_NOT_FOUND);
                }
                cacheService.saveCategory(category);
            }
            dbNote.setCid(category.getId());
        }
        int i = noteMapper.saveNote(dbNote);
        if (i > 0){
            rabbitTemplate.convertAndSend("update-note-count-queue", uid+ ":" + 1);
            dbNote.setUpdateTime(LocalDateTime.now());
            dbNote.setUpdateTime(LocalDateTime.now());
            cacheService.addCategoryChildren(dbNote);
            return Result.success();
        }
        return Result.error(StatusCodeEnum.CREATE_NOTE_FAILED);
    }

    @Override
    public Result updateNote(Long id, String title, String text, Boolean isPrivate, Long cid, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        Note note = cacheService.getNote(id);
        if (note == null){
            note = noteMapper.getNoteById(id);
            if (note == null){
                return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
            }
            cacheService.saveNote(note);
        }
        if (note.getId() == null || note.getUid() != uid){
            return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
        }
        boolean updateCategory = note.getCid() != cid;
        if (cid != null){
            Category category = cacheService.getCategory(uid, cid);
            if (category == null){
                category = categoryMapper.getCategoryById(cid, uid);
                if (category == null){
                    return Result.error(StatusCodeEnum.CATEGORY_NOT_FOUND);
                }
                cacheService.saveCategory(category);
            }
            note.setCid(category.getId());
        }
        if (StringUtils.isNotBlank(title)) note.setTitle(title);
        if (StringUtils.isNotBlank(text)) note.setText(text);
        if (isPrivate != null) note.setIsPrivate(isPrivate);
        note.setUpdateTime(LocalDateTime.now());
        int i = noteMapper.updateNote(note);
        if (i > 0){
            if (updateCategory){
                cacheService.removeCategoryChildren(note);
                cacheService.addCategoryChildren(note);
            }
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
        Note note = cacheService.getNote(id);
        if (note == null){
            note = noteMapper.getNoteById(id);
            if (note == null){
                return Result.success();
            }
        }
        if (note.getId() == null || note.getUid() != uid){
            return Result.success();
        }
        int i = noteMapper.deleteNote(id, uid);
        if (i > 0){
            commentMapper.deleteByNoteId(id); // 删除评论
            rabbitTemplate.convertAndSend("update-note-count-queue", uid+ ":" + -1);
            cacheService.removeCategoryChildren(note);
            return Result.success();
        }
        return Result.error(StatusCodeEnum.DELETE_NOTE_FAILED);
    }

    @Override
    public Result<PageResult<NoteListVO>> getNoteList(Long cid, Long uid, Boolean isPrivate) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        if (cacheService.isHasKeyCategoryChildren(uid, cid)){
                List<Note> categoryAllChildren = cacheService.getCategoryAllChildren(uid, cid);
                categoryAllChildren = categoryAllChildren.stream().filter(note -> isPrivate == null || note.getIsPrivate() == isPrivate).toList();
                return Result.success(new PageResult<>(categoryAllChildren.size(), BeanUtil.copyToList(categoryAllChildren, NoteListVO.class)));
        }else {
                Note dbNote = new Note();
                dbNote.setUid(uid);
                dbNote.setCid(cid);
                List<Note> notes = noteMapper.selectNote(dbNote);
                if (notes.isEmpty()){
                    return Result.success(new PageResult<>(0, BeanUtil.copyToList(notes, NoteListVO.class)));
                }
                cacheService.saveCategoryAllChildren(uid, cid, notes);
                notes = notes.stream().filter(note -> isPrivate == null || note.getIsPrivate() == isPrivate).toList();
                return Result.success(new PageResult<>(notes.size(), BeanUtil.copyToList(notes, NoteListVO.class)));
        }
    }

    @Override
    public Result<NoteVO> getNoteInfo(Long id, Long uid) {
        Note note = cacheService.getNote(id);
        if (note == null){
            note = noteMapper.getNoteById(id);
            if (note == null){
                cacheService.saveNullNote(id);
                return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
            }
            cacheService.saveNote(note);
        }
        if (note.getId() == null){
            return Result.error(StatusCodeEnum.NOTE_NOT_FOUND);
        }
        if (note.getIsPrivate()) // 私密笔记需要验证 token
        {
            if (uid == null || note.getUid() != uid){
                return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
            }
        }
        NoteVO noteInfo = BeanUtil.copyProperties(note, NoteVO.class);
        return Result.success(noteInfo);
    }

    @Override
    public Result<List<NoteInfo>> getPublicNote(Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        List<NoteInfo> notes =  noteMapper.getPublicNoteInfo(uid);
        if (notes.isEmpty()){
            return Result.success(new ArrayList<>());
        }
        return Result.success(notes);
    }
}
