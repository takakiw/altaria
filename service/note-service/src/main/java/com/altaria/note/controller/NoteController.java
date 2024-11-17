package com.altaria.note.controller;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.dto.NoteDTO;
import com.altaria.common.pojos.note.entity.Note;
import com.altaria.common.pojos.note.vo.NoteListVO;
import com.altaria.common.pojos.note.vo.NoteVO;
import com.altaria.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/note")
@RestController
public class NoteController {

    @Autowired
    private NoteService noteService;

    // 获取笔记列表
    @GetMapping("/list")
    private Result<PageResult<NoteListVO>> getNoteList(@RequestHeader(UserConstants.USER_ID) Long uid,
                                                       @RequestParam(value = "category", required = false) String category) {
        return noteService.getNoteList(category, uid);
    }

    // 获取笔记详情
    @GetMapping("/{id}")
    private Result<NoteVO> getNote(@PathVariable("id") Long id,
                                   @RequestHeader(UserConstants.USER_ID) Long uid) {
        return noteService.getNoteInfo(id, uid);
    }


    // 创建笔记
    @PostMapping("/create")
    private Result createNote(@RequestBody NoteDTO note,
                              @RequestHeader(UserConstants.USER_ID) Long uid) {
       return noteService.createNote(note.getTitle(), note.getText(), note.getIsPrivate(), note.getCategoryName(), uid);
    }

    // 更新笔记
    @PutMapping("/update")
    private Result updateNote(@RequestBody NoteDTO note,
                              @RequestHeader(UserConstants.USER_ID) Long uid) {
        return noteService.updateNote(note.getId(), note.getTitle(), note.getText(), note.getIsPrivate(), note.getCategoryName(), uid);
    }

    // 删除笔记
    @DeleteMapping("/delete/{id}")
    private Result deleteNote(@PathVariable("id") Long id,
                              @RequestHeader(UserConstants.USER_ID) Long uid) {
        return noteService.deleteNote(id, uid);
    }

    // 获取所有公开的笔记
    @GetMapping("/shareList")
    private Result<List<NoteListVO>> getShareSign(@RequestHeader(UserConstants.USER_ID) Long uid) {
        return noteService.getShareList(uid);
    }

}