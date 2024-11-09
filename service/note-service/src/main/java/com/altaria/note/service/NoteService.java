package com.altaria.note.service;

import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.vo.NoteListVO;
import com.altaria.common.pojos.note.vo.NoteVO;

public interface NoteService {
    Result createNote(String title, String text, Boolean isPrivate, String cid, Long uid);

    Result updateNote(Long id, String title, String text, Boolean isPrivate, String cid, Long uid);

    Result deleteNote(Long id, Long uid);

    Result<PageResult<NoteListVO>> getNoteList(String category, Long uid);

    Result<NoteVO> getNoteInfo(Long id, Long uid);
}
