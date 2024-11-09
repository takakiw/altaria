package com.altaria.file.cache;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.pojos.file.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class FileCacheService {


    private static final Long FILE_EXPIRATION_TIME = 60 * 60 * 24 * 2L; // 2 days
    private static final long FILE_NULL_EXPIRATION_TIME = 60 * 5L; // 5分钟
    private static final String FILE_PREFIX = "file:"; // hset file:uid:fid values
    private static final String FILE_PARENT_UPDATE_PREFIX = "parent:update"; // zset
    private static final String FILE_PARENT_NAME_PREFIX = "parent:name"; // zset
    private static final String FILE_PARENT_SIZE_PREFIX = "parent:size"; // zset



    private static final long FILE_UPLOAD_EXPIRATION_TIME = 60 * 60 * 24 * 2L; // 2天
    private static final String FILE_UPLOAD_PREFIX = "upload:"; // hset upload:uid:fid values
    private static final String FILE_RECYCLE_PREFIX = "recycle:";
    private static final String FILE_RECYCLE_PARENT_PREFIX = "recycle:parent";
    private static final long FILE_RECYCLE_EXPIRATION_TIME = 60 * 60 * 24 * 2L; // 2天


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /*
    * private Long id; //主键ID
    private Long uid; // 用户ID
    private String url; // 文件在minio上的路径
    private String fileName; // 文件名
    private Integer type; // 文件类型, 1图片， 2视频， 3音频, 4pdf， 5word， 6excel，7txt， 8其他
    private Long size; // 文件大小
    private String md5; // 文件的md5值
    private Long pid; // 父级目录ID
    private Integer status; // 状态，0正常，1删除
    private String cover; // 封面图片路径
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    * */

    public void saveFile(FileInfo fileInfo){
        redisTemplate.opsForHash().putAll(FILE_PREFIX + fileInfo.getUid() + ":" + fileInfo.getId(), BeanUtil.beanToMap(fileInfo));
        redisTemplate.expire(FILE_PREFIX + fileInfo.getUid() + ":" + fileInfo.getId(), FILE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public void saveNullFile(Long uid, Long pid) {
        redisTemplate.opsForHash().putAll(FILE_PREFIX + uid + ":" + pid, Map.of("fileName", "null"));
        redisTemplate.expire(FILE_PREFIX + uid + ":" + pid, FILE_NULL_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public void saveFileBatch(List<FileInfo> fileInfoList){
        for (FileInfo fileInfo : fileInfoList){
            redisTemplate.opsForHash().putAll(FILE_PREFIX + fileInfo.getUid() + ":" + fileInfo.getId(), BeanUtil.beanToMap(fileInfo));
            redisTemplate.expire(FILE_PREFIX + fileInfo.getUid() + fileInfo.getId(), FILE_EXPIRATION_TIME, TimeUnit.SECONDS);
        }
    }

    public void deleteFile(Long uid, Long fid){
        redisTemplate.delete(FILE_PREFIX + uid + ":" + fid);
    }


    public void deleteFileBatch(Long uid, List<Long> fids){
        redisTemplate.delete(fids.stream().map(fid -> FILE_PREFIX + uid + ":" + fid).collect(Collectors.toList()));
    }

    public FileInfo getFile(Long uid, Long fid){
        Map<Object, Object> data = redisTemplate.opsForHash().entries(FILE_PREFIX + uid + ":" + fid);
        if (data.isEmpty()){
            return null;
        }
        return BeanUtil.copyProperties(data, FileInfo.class);
    }


    public void updateFileName(Long uid, Long pid, Long fid, String fileName, String oldName) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PREFIX + uid + ":" + fid))){
            redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "fileName", fileName);
            redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "updateTime", LocalDateTime.now());
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PARENT_NAME_PREFIX + uid + ":" + pid))){
            redisTemplate.opsForZSet().remove(FILE_PARENT_NAME_PREFIX + uid + ":" + pid, oldName);
            redisTemplate.opsForZSet().add(FILE_PARENT_NAME_PREFIX + uid + ":" + pid, fileName, fid);
            redisTemplate.opsForZSet().add(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid, fid, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        }
    }

    public void updateFileSize(Long uid, Long fid, Long pid, Long size){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PREFIX + uid + ":" + fid))){
            Object oSize = redisTemplate.opsForHash().get(FILE_PREFIX + uid + ":" + fid, "size");
            if (oSize != null){
                Long oldSize = Long.valueOf(oSize.toString());
                redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "size", size + oldSize);
                redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "updateTime", System.currentTimeMillis());
            }
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid))){ // 更新父目录大小
            redisTemplate.opsForZSet().incrementScore(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid, fid, size);
            redisTemplate.opsForZSet().add(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid, fid, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        }
    }

    public void updateFileCoverAndUrl(Long uid, Long fid, String cover, String url){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PREFIX + uid + ":" + fid))){
            System.out.println("cover:" + cover + " url:" + url);
            redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "cover", cover);
            redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "url", url);
        }
    }

    public void updateFileParent(Long uid, Long fid, Long pid){
       if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PREFIX + uid + ":" + fid))){
           redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "pid", pid);
           redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "updateTime", LocalDateTime.now());
       }
    }

    public void saveAllChildren(Long uid, Long pid, List<FileInfo> fileInfoList){
        redisTemplate.delete(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid);
        redisTemplate.delete(FILE_PARENT_NAME_PREFIX + uid + ":" + pid);
        redisTemplate.delete(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid);
        if(fileInfoList == null || fileInfoList.isEmpty()){
            return;
        }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            for (FileInfo fileInfo : fileInfoList) {
                double updateTimeScore = fileInfo.getUpdateTime().toEpochSecond(ZoneOffset.UTC);
                zSetOperations.add(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid, fileInfo.getId(), updateTimeScore);
                zSetOperations.add(FILE_PARENT_NAME_PREFIX + uid + ":" + pid,fileInfo.getFileName(), fileInfo.getId());
                zSetOperations.add(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid, fileInfo.getId(), fileInfo.getSize());
                saveFile(fileInfo);
            }
            return null;
        });
        redisTemplate.expire(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid, FILE_EXPIRATION_TIME, TimeUnit.SECONDS);
        redisTemplate.expire(FILE_PARENT_NAME_PREFIX + uid + ":" + pid, FILE_EXPIRATION_TIME, TimeUnit.SECONDS);
        redisTemplate.expire(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid, FILE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public void addChildren(Long uid, Long pid, FileInfo fileInfo){
        if (fileInfo == null || Boolean.FALSE.equals(redisTemplate.hasKey(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid))){
            return;
        }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            double updateTimeScore = fileInfo.getUpdateTime().toEpochSecond(ZoneOffset.UTC);
            zSetOperations.add(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid, fileInfo.getId(), updateTimeScore);
            zSetOperations.add(FILE_PARENT_NAME_PREFIX + uid + ":" + pid,fileInfo.getFileName(), fileInfo.getId());
            zSetOperations.add(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid, fileInfo.getId(), fileInfo.getSize());
            return null;
        });
    }

    public void deleteChildren(Long uid, Long pid, Long fid){
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            zSetOperations.remove(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid, fid);
            zSetOperations.removeRangeByScore(FILE_PARENT_NAME_PREFIX + uid + ":" + pid, fid, fid);
            zSetOperations.remove(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid, fid);
            return null;
        });
    }

    public List<FileInfo> getChildrenOrderUpdateTime(Long uid, Long fid){
        List<Long> longs = Objects.requireNonNull(redisTemplate.opsForZSet().range(FILE_PARENT_UPDATE_PREFIX + uid + ":" + fid, 0, -1)).stream().map(o -> (Long) o).toList();
        return longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
    }
    
    public List<FileInfo> getChildrenOrderUpdateTimeReverse(Long uid, Long fid){
        List<Long> longs = Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(FILE_PARENT_UPDATE_PREFIX + uid + ":" + fid, 0, -1)).stream().map(o -> (Long) o).toList();
        return longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
    }

    public List<FileInfo> getChildrenOrderSize(Long uid, Long fid){
        List<Long> longs = Objects.requireNonNull(redisTemplate.opsForZSet().range(FILE_PARENT_SIZE_PREFIX + uid + ":" + fid, 0, -1)).stream().map(o -> (Long) o).toList();
        List<FileInfo> fileInfos = longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
        return fileInfos;
    }


    public List<FileInfo> getChildrenOrderSizeReverse(Long uid, Long fid){
        List<Long> longs = Objects.requireNonNull(redisTemplate.opsForZSet().reverseRange(FILE_PARENT_SIZE_PREFIX + uid + ":" + fid, 0, -1)).stream().map(o -> (Long) o).toList();
        return longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
    }


    public List<String> getChildrenAllName(Long uid, Long fid){
        return redisTemplate.opsForZSet().rangeByScore(FILE_PARENT_NAME_PREFIX + uid + ":" + fid,0, Long.MAX_VALUE).stream().map(o -> (String) o).toList();
    }


    public Boolean getChildByFileName(Long uid, Long fid, String fileName){
        Long rank = redisTemplate.opsForZSet().rank(FILE_PARENT_NAME_PREFIX + uid + ":" + fid, fileName);
        return rank != null && rank >= 0;
    }



    public Boolean ParentKeyCodeExists(Long uid, Long pid) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid));
    }

    public void deleteChildrenBatch(List<FileInfo> fileInfoList) {
        for (FileInfo fileInfo : fileInfoList){
            deleteChildren(fileInfo.getUid(), fileInfo.getPid(), fileInfo.getId());
        }
    }

    public void deleteAllChildren(Long uid, Long id) {
        redisTemplate.delete(FILE_PARENT_UPDATE_PREFIX + uid + ":" + id);
        redisTemplate.delete(FILE_PARENT_NAME_PREFIX + uid + ":" + id);
        redisTemplate.delete(FILE_PARENT_SIZE_PREFIX + uid + ":" + id);
    }

    public boolean existsUploadFile(Long fid) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(FILE_UPLOAD_PREFIX + ":" + fid));
    }

    // 更新上传文件大小
    public void updateUploadFileSize(Long fid, long size) {
        redisTemplate.opsForValue().increment(FILE_UPLOAD_PREFIX  + ":" + fid, size);
        redisTemplate.expire(FILE_UPLOAD_PREFIX +  ":" + fid, FILE_UPLOAD_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public long getUploadFileSize(Long fid) {
        Object o = redisTemplate.opsForValue().get(FILE_UPLOAD_PREFIX + ":" + fid);
        if (o == null){
            return 0;
        }
        return Long.parseLong(o.toString());
    }

    public void deleteUploadFile(Long fid) {
        redisTemplate.delete(FILE_UPLOAD_PREFIX + ":" + fid);
    }

    private FileInfo getRecycleFile(Long uid, Long fid) {
        Map<Object, Object> data = redisTemplate.opsForHash().entries(FILE_RECYCLE_PREFIX + uid + ":" + fid);
        if (data.isEmpty()){
            return null;
        }
        return BeanUtil.copyProperties(data, FileInfo.class);
    }


    public void saveAllRecycleFiles(Long uid, List<FileInfo> fileInfoList) {
        if (fileInfoList == null || fileInfoList.isEmpty()){
            return;
        }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (FileInfo fileInfo : fileInfoList) {
                redisTemplate.opsForHash().putAll(FILE_RECYCLE_PREFIX + uid + ":" + fileInfo.getId(), BeanUtil.beanToMap(fileInfo));
                redisTemplate.opsForZSet().add(FILE_RECYCLE_PARENT_PREFIX + uid, fileInfo.getId(), fileInfo.getCreateTime().toEpochSecond(ZoneOffset.UTC));
                redisTemplate.expire(FILE_RECYCLE_PREFIX + uid + ":" + fileInfo.getId(), FILE_RECYCLE_EXPIRATION_TIME, TimeUnit.SECONDS);
            }
            return null;
        });
        redisTemplate.expire(FILE_RECYCLE_PARENT_PREFIX + uid, FILE_RECYCLE_EXPIRATION_TIME - 60, TimeUnit.SECONDS);
    }


    public void saveRecycleFile(Long uid, FileInfo fileInfo) {
        redisTemplate.opsForHash().putAll(FILE_RECYCLE_PREFIX + uid + ":" + fileInfo.getId(), BeanUtil.beanToMap(fileInfo));
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_RECYCLE_PARENT_PREFIX + uid))){
            redisTemplate.opsForZSet().add(FILE_RECYCLE_PARENT_PREFIX + uid, fileInfo.getId(), fileInfo.getCreateTime().toEpochSecond(ZoneOffset.UTC));
        }
        redisTemplate.expire(FILE_RECYCLE_PREFIX + uid + ":" + fileInfo.getId(), FILE_RECYCLE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public void saveRecycleFiles(Long uid, List<FileInfo> fileInfoList) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_RECYCLE_PARENT_PREFIX + uid))){
            for (FileInfo fileInfo : fileInfoList){
                redisTemplate.opsForZSet().add(FILE_RECYCLE_PARENT_PREFIX + uid, fileInfo.getId(), fileInfo.getCreateTime().toEpochSecond(ZoneOffset.UTC));
            }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (FileInfo fileInfo : fileInfoList) {
                redisTemplate.opsForHash().putAll(FILE_RECYCLE_PREFIX + uid + ":" + fileInfo.getId(), BeanUtil.beanToMap(fileInfo));
                redisTemplate.opsForZSet().add(FILE_RECYCLE_PARENT_PREFIX + uid, fileInfo.getId(), fileInfo.getCreateTime().toEpochSecond(ZoneOffset.UTC));
                redisTemplate.expire(FILE_RECYCLE_PREFIX + uid + ":" + fileInfo.getId(), FILE_RECYCLE_EXPIRATION_TIME, TimeUnit.SECONDS);
            }
            return null;
            });
    }
    }


    public void deleteRecycleFile(Long uid, Long fid) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_RECYCLE_PARENT_PREFIX + uid))){
            redisTemplate.opsForZSet().remove(FILE_RECYCLE_PARENT_PREFIX + uid, fid);
        }
        redisTemplate.delete(FILE_RECYCLE_PREFIX + uid + ":" + fid);
    }

    public void deleteRecycleFiles(Long uid, List<Long> fids) {
        if (fids == null || fids.isEmpty()){
            return;
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_RECYCLE_PARENT_PREFIX + uid))){
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Long fid : fids) {
                    redisTemplate.opsForZSet().remove(FILE_RECYCLE_PARENT_PREFIX + uid, fid);
                    redisTemplate.delete(FILE_RECYCLE_PREFIX + uid + ":" + fid);
                }
                return null;
            });
        }
    }


    public List<FileInfo> getAllRecycleFiles(Long uid) {
        List<Long> ids = Objects.requireNonNull(redisTemplate.opsForZSet().range(FILE_RECYCLE_PARENT_PREFIX + uid, 0, -1)).stream().map(o -> (Long) o).toList();
        return ids.stream().map(cacheId -> getRecycleFile(uid, cacheId)).toList();
    }

    public List<FileInfo> getRecycleFiles(Long uid, List<Long> fids){
        if (fids == null || fids.isEmpty()){
            return new ArrayList<>();
        }
        return fids.stream().map(cacheId -> getRecycleFile(uid, cacheId)).toList();
    }

    public Boolean existsRecycleChildren(Long uid) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(FILE_RECYCLE_PARENT_PREFIX + uid));
    }

    public void updateFileTransformed(Long uid, long dbId, Integer transformed) {
        redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + dbId, "transformed", transformed);
    }
}
