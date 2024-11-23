package com.altaria.note.service;

import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.NoteInfo;
import com.altaria.common.pojos.note.vo.NoteListVO;
import com.altaria.common.pojos.note.vo.NoteVO;

import java.util.List;

public interface NoteService {
    Result createNote(String title, String text, Boolean isPrivate, Long cid, Long uid);

    Result updateNote(Long id, String title, String text, Boolean isPrivate, Long cid, Long uid);

    Result deleteNote(Long id, Long uid);

    Result<PageResult<NoteListVO>> getNoteList(Long cid, Long uid, Boolean isPrivate);

    Result<NoteVO> getNoteInfo(Long id, Long uid);

    Result<List<NoteInfo>> getPublicNote(Long uid);

    Result<List<NoteInfo>> getAllPublicNote(Integer page, Integer size);
}
