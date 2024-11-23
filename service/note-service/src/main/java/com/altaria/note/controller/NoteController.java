package com.altaria.note.controller;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.dto.NoteDTO;
import com.altaria.common.pojos.note.entity.Note;
import com.altaria.common.pojos.note.entity.NoteInfo;
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
                                                       @RequestParam(value = "cid", required = false) Long cid,
                                                       @RequestParam(value = "isPrivate", required = false) Boolean isPrivate) {
        return noteService.getNoteList(cid, uid, isPrivate);
    }

    // 获取笔记详情
    @GetMapping("/{id}")
    private Result<NoteVO> getNote(@PathVariable("id") Long id,
                                   @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return noteService.getNoteInfo(id, uid);
    }


    // 创建笔记
    @PostMapping("/create")
    private Result createNote(@RequestBody NoteDTO note,
                              @RequestHeader(UserConstants.USER_ID) Long uid) {
       return noteService.createNote(note.getTitle(), note.getText(), note.getIsPrivate(), note.getCid(), uid);
    }

    // 更新笔记
    @PutMapping("/update")
    private Result updateNote(@RequestBody NoteDTO note,
                              @RequestHeader(UserConstants.USER_ID) Long uid) {
        return noteService.updateNote(note.getId(), note.getTitle(), note.getText(), note.getIsPrivate(), note.getCid(), uid);
    }

    // 删除笔记
    @DeleteMapping("/delete/{id}")
    private Result deleteNote(@PathVariable("id") Long id,
                              @RequestHeader(UserConstants.USER_ID) Long uid) {
        return noteService.deleteNote(id, uid);
    }

    // 获取所有公开笔记
    @GetMapping("/public")
    private Result<List<NoteInfo>> getPublicNote(@RequestHeader(UserConstants.USER_ID) Long uid) {
        return noteService.getPublicNote(uid);
    }

    // 获取所有的公开笔记
    @GetMapping("/public/all")
    private Result<List<NoteInfo>> getAllPublicNote(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                     @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        return noteService.getAllPublicNote(page, size);
    }
}
