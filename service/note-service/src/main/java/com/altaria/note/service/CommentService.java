package com.altaria.note.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.CommentInfo;

import java.util.List;

public interface CommentService {
    Result<List<CommentInfo>> list(Long nid, Long uid);

    Result<CommentInfo> add(Long uid, Long nid, Long pid, Long toId, String content);

    Result delete(Long uid, Long id);
}
