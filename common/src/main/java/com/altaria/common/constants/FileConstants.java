package com.altaria.common.constants;

import java.util.List;
import java.util.Map;

public class FileConstants {
    public static final String INVALID_DIR_NAME_REGEX = "^[^/*&%$#]{1,200}$";



    public static final Integer STATUS_USE = 0;
    public static final Integer STATUS_DELETE = 1;

    public static final Integer STATUS_RECYCLE = 2;
    public static final Long ROOT_DIR_ID = 0L;
    public static final Integer THUMBNAIL_WIDTH = 100;
    public static final Integer IMAGE_WIDTH = 100;
    public static final Integer TRANSFORMED_END = 0; // 转码完成
    public static final Integer TRANSFORMED_PROCESS = 1; // 转码中
    public static final Integer TRANSFORMED_ERROR = 2; // 转码失败
    public static final long RECYCLE_EXPIRE_TIME = 60 * 60 * 24 * 30L; // 回收站过期时间 30天
    public static Integer WEB_RECYCLE_CODE = 5; // 网页回收站编号

    public static final Map<Integer, List<Integer>> FILE_TYPE_MAP = Map.of(
            0, List.of(0),
            1, List.of(1),
            2, List.of(2),
            3, List.of(3),
            4, List.of(4, 5, 6)
    );


}
