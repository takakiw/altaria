package com.altaria.common.constants;

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
}
