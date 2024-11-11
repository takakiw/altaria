package com.altaria.note.controller;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Comment;
import com.altaria.note.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/list")
    public Result<List<Comment>> list(@RequestParam(value = "nid") Long nid){
        return commentService.list(nid);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Comment comment,
                      @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid){
        return commentService.add(uid, comment.getNid(), comment.getPid(), comment.getContent());
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Long id,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid){
        return commentService.delete(uid, id);
    }
}
