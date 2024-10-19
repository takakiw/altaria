package com.altaria.file.cache;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.space.entity.Space;
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
import java.util.Set;
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


    private static final long SPACE_EXPIRATION_TIME = 60 * 60 * 24 * 2L; // 2 days
    private static final String SPACE_PREFIX = "space:";

    private static final long FILE_UPLOAD_EXPIRATION_TIME = 60 * 60 * 24 * 1L; // 2天
    private static final String FILE_UPLOAD_PREFIX = "upload:"; // hset upload:uid:fid values
    private static final String FILE_RECYCLE_PREFIX = "recycle:";
    private static final String FILE_RECYCLE_PARENT_PREFIX = "recycle:parent";
    private static final long FILE_RECYCLE_EXPIRATION_TIME = 60 * 60 * 24 * 1L; // 1天


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
        if (data == null || data.isEmpty()){
            return null;
        }
        return BeanUtil.copyProperties(data, FileInfo.class);
    }

    public List<FileInfo> getFiles(Long uid, List<Long> fids){
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (Long fid : fids){
            Map<Object, Object> data = redisTemplate.opsForHash().entries(FILE_PREFIX + uid + ":" + fid);
            if (data == null || data.isEmpty()){
                continue;
            }
            fileInfoList.add(BeanUtil.copyProperties(data, FileInfo.class));
        }
        return fileInfoList;
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
            redisTemplate.opsForZSet().incrementScore(FILE_PARENT_SIZE_PREFIX + uid + ":" + pid, uid, size);
            redisTemplate.opsForZSet().add(FILE_PARENT_UPDATE_PREFIX + uid + ":" + pid, fid, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        }
    }

    public void updateFileCover(Long uid, Long fid, String cover){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(FILE_PREFIX + uid + ":" + fid))){
            redisTemplate.opsForHash().put(FILE_PREFIX + uid + ":" + fid, "cover", cover);
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
        List<Long> longs = redisTemplate.opsForZSet().range(FILE_PARENT_UPDATE_PREFIX + uid + ":" + fid, 0, -1).stream().map(o -> (Long) o).toList();
        List<FileInfo> fileInfos = longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
        return fileInfos;
    }
    
    public List<FileInfo> getChildrenOrderUpdateTimeReverse(Long uid, Long fid){
        List<Long> longs = redisTemplate.opsForZSet().reverseRange(FILE_PARENT_UPDATE_PREFIX + uid + ":" + fid, 0, -1).stream().map(o -> (Long) o).toList();
        List<FileInfo> fileInfos = longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
        return fileInfos;
    }

    public List<FileInfo> getChildrenOrderSize(Long uid, Long fid){
        List<Long> longs = redisTemplate.opsForZSet().range(FILE_PARENT_SIZE_PREFIX + uid + ":" + fid, 0, -1).stream().map(o -> (Long) o).toList();
        List<FileInfo> fileInfos = longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
        return fileInfos;
    }


    public List<FileInfo> getChildrenOrderSizeReverse(Long uid, Long fid){
        List<Long> longs = redisTemplate.opsForZSet().reverseRange(FILE_PARENT_SIZE_PREFIX + uid + ":" + fid, 0, -1).stream().map(o -> (Long) o).toList();
        List<FileInfo> fileInfos = longs.stream().map(cacheId -> getFile(uid, cacheId)).toList();
        return fileInfos;
    }


    public List<String> getChildrenAllName(Long uid, Long fid){
        return redisTemplate.opsForZSet().rangeByScore(FILE_PARENT_NAME_PREFIX + uid + ":" + fid,0, Long.MAX_VALUE).stream().map(o -> (String) o).toList();
    }

    public List<Long> getChildrenOrderName(Long uid, Long fid){
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(FILE_PARENT_NAME_PREFIX + uid + ":" + fid, 0, -1);
        List<Long> longs = typedTuples.stream().sorted((o1, o2) -> {
            return o1.getValue().toString().compareTo(o2.getValue().toString());
        }).map(o -> o.getScore().longValue()).toList();
        return longs;
    }

    public List<Long> getChildrenOrderNameReverse(Long uid, Long fid){
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(FILE_PARENT_NAME_PREFIX + uid + ":" + fid, 0, -1);
        List<Long> longs = typedTuples.stream().sorted((o1, o2) -> {
            return o2.getValue().toString().compareTo(o1.getValue().toString());
        }).map(o -> o.getScore().longValue()).toList();
        return longs;
    }

    public Boolean getChildByFileName(Long uid, Long fid, String fileName){
        Long rank = redisTemplate.opsForZSet().rank(FILE_PARENT_NAME_PREFIX + uid + ":" + fid, fileName);
        return rank != null && rank >= 0;
    }

    public void saveSpace(Space space) {
        redisTemplate.opsForValue().set(SPACE_PREFIX + space.getUid(), space, SPACE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public Space getSpace(Long uid) {
        return (Space) redisTemplate.opsForValue().get(SPACE_PREFIX + uid);
    }


    public boolean ParentKeyCodeExists(Long uid, Long pid) {
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

    public void updateUploadFileSize(Long uid, Long fid, long size) {
        redisTemplate.opsForValue().increment(FILE_UPLOAD_PREFIX + uid + ":" + fid, size);
        redisTemplate.expire(FILE_UPLOAD_PREFIX + uid + ":" + fid, FILE_UPLOAD_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public long getUploadFileSize(Long uid, Long fid) {
        Object o = redisTemplate.opsForValue().get(FILE_UPLOAD_PREFIX + uid + ":" + fid);
        if (o == null){
            return 0;
        }
        return Long.parseLong(o.toString());
    }

    public void deleteUploadFile(Long uid, Long fid) {
        redisTemplate.delete(FILE_UPLOAD_PREFIX + uid + ":" + fid);
    }

    private FileInfo getRecycleFile(Long uid, Long fid) {
        Map<Object, Object> data = redisTemplate.opsForHash().entries(FILE_RECYCLE_PREFIX + uid + ":" + fid);
        if (data == null || data.isEmpty()){
            return null;
        }
        return BeanUtil.copyProperties(data, FileInfo.class);
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


    public List<FileInfo> getRecycleFiles(Long uid) {
        List<Long> ids = redisTemplate.opsForZSet().range(FILE_RECYCLE_PARENT_PREFIX + uid, 0, -1).stream().map(o -> (Long) o).toList();
        List<FileInfo> fileInfos = ids.stream().map(cacheId -> getRecycleFile(uid, cacheId)).toList();
        return fileInfos;
    }

    public boolean existsRecycleChildren(Long uid) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(FILE_RECYCLE_PARENT_PREFIX + uid));
    }
}
