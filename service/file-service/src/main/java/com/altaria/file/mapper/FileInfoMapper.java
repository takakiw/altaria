package com.altaria.file.mapper;


import com.altaria.common.annotation.AutoFill;
import com.altaria.common.enums.OperationType;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FileInfoMapper {

    @AutoFill(OperationType.INSERT)
    int insert(FileInfo fileInfo);


    Page<FileInfo> select(FileInfo query);

    @AutoFill(OperationType.UPDATE)
    int update(FileInfo updateFile);


    FileInfo getFileById(@Param("id") Long id, @Param("uid") Long uid);

    List<FileInfo> getChildFiles(@Param("pid") Long pid,@Param("uid") Long uid, @Param("status") Integer status);

    int deleteBatch(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM file WHERE pid = #{pid} AND uid = #{uid} AND file_name = #{fileName} AND status = 0")
    FileInfo getFileChildName(@Param("pid") Long pid,@Param("uid") Long uid,@Param("fileName") String fileName);

    List<FileInfo> getFileByIds(@Param("ids") List<Long> ids, @Param("uid") Long uid, @Param("status") Integer status);

    List<FileInfo> getRecycleFileByIds(@Param("ids") List<Long> ids);

    int updateStatusBatch(@Param("uid") Long uid, @Param("ids") List<Long> ids, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    @Select("SELECT * FROM file WHERE md5 = #{md5}")
    List<FileInfo> getFileByMd5(@Param("md5") String md5);


    @Update("UPDATE file SET size = size + #{size}, update_time = #{updateTime} WHERE id = #{id} AND uid = #{uid}")
    int updateParentSize(@Param("uid") Long uid, @Param("id") Long id, @Param("size") Long size, @Param("updateTime") LocalDateTime updateTime);

    List<FileInfo> selectOrder(@Param("uid") Long uid, @Param("pid") Long pid, @Param("type") Integer type, @Param("fileName") String fileName, @Param("order") Integer order);


    int updateURLAndCoverByMd5(@Param("url") String url, @Param("cover") String cover, @Param("md5") String md5, @Param("transformed") Integer transformed);
    @Select("SELECT * FROM file WHERE uid = #{uid} AND status = 2")
    List<FileInfo> getRecycleFiles(Long uid);

    int insertBatch(@Param("fileInfos") List<FileInfo> fileInfos);

    int updatePidAndFileNameBatch(@Param("uid") Long uid, @Param("files") List<FileInfo> files);

    @Select("SELECT * FROM file WHERE status = 2 AND update_time < #{expiredTime}")
    List<FileInfo> getAllRecycleFiles(@Param("expiredTime") LocalDateTime expiredTime);

    @Select("SELECT * FROM file WHERE id = #{id} AND uid = #{uid} AND status = 2")
    FileInfo getRecycleFile(@Param("id") Long id, @Param("uid") Long uid);
}
