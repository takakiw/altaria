package com.altaria.note.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Comment;

import java.util.List;

public interface CommentService {
    Result<List<Comment>> list(Long nid);

    Result add(Long uid, Long nid, Long pid, String content);

    Result delete(Long uid, Long id);
}
