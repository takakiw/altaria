package com.altaria.file.cache;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.altaria.common.pojos.file.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FileCacheService {

    private static final Long USER_EXPIRATION_TIME = 60 * 60 * 24 * 7L; // 7 days

    private static final Long FILE_EXPIRATION_TIME = 60 * 60 * 24 * 2L; // 2 days

    private static final String FILE_PREFIX = "file:";
    private static final String FILE_PARENT_PREFIX = "parent:";


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据文件ID和用户ID获取文件信息
     * @param fid 文件ID
     * @param uid 用户ID
     * @return 文件对象
     */
    public FileInfo getFileById(Long fid, Long uid) {
        return (FileInfo) redisTemplate.opsForValue().get(FILE_PREFIX + uid + ":" + fid);
    }

    /**
     * 保存文件信息到redis
     * @param fileInfo
     */
    public void saveFile(FileInfo fileInfo) {
        redisTemplate.opsForValue().set(FILE_PREFIX + fileInfo.getUid() + ":" + fileInfo.getId(), fileInfo, FILE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    /**
     *
     * 删除文件信息
     * @param fid
     * @param uid
     */
    public void deleteFile(Long fid, Long uid) {
        redisTemplate.delete(FILE_PREFIX + uid + ":" + fid);
    }

    /**
     * 批量删除文件信息
     * @param ids
     * @param uid
     */
    public void deleteFileBatch(List<Long> ids, Long uid) {
        List<String> keys = ids.stream().map(id -> FILE_PREFIX + uid + ":" + id).toList();
        redisTemplate.delete(keys);
    }

    public void saveChildFiles(List<FileInfo> result, Long fid, Long uid) {
        redisTemplate.opsForValue().set(FILE_PARENT_PREFIX + uid + ":" + fid, result, FILE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public List<FileInfo> getChildFiles(Long fid, Long uid) {
        Object o = redisTemplate.opsForValue().get(FILE_PARENT_PREFIX + uid + ":" + fid);
        List<FileInfo> fileInfos = JSONObject.parseArray(JSONObject.toJSONString(o), FileInfo.class);
        return fileInfos;
    }

    public void deleteChildFiles(Long fid, Long uid) {
        redisTemplate.delete(FILE_PARENT_PREFIX + uid + ":" + fid);
    }

    public void deleteChildFilesBatch(List<Long> ids, Long uid) {
        List<String> keys = ids.stream().map(id -> FILE_PARENT_PREFIX + uid + ":" + id).toList();
        redisTemplate.delete(keys);
    }
}
